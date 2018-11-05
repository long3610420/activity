package com.exshell.ops.activity.job.task.index;

import static com.exshell.bitex.commons.Log.format;
import static com.exshell.bitex.commons.Log.kv;

import com.exshell.bitex.commons.util.Text;
import com.exshell.bitex.general.model.IndexSymbolDataVO;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取k线实时数据
 */
public class KLineWS extends Thread implements QuotationReceive {

    private static final long HOUR_MILLS = 3600000L;
    private static Logger LOG = LoggerFactory.getLogger(KLineWS.class);

    private OkHttpClient wsClient;

    private volatile boolean running = false;

    private String blackNames;

    private String proxyHost;
    private Integer proxyPort;
    private String wsUrl = "wss://api.exshellpro.com/ws";
    private String env = "";
    private int refreshQuotationSize;

    private Map<String, Long> dataTimeMap;
    private Map<String, Long> dataSubTimeMap = new ConcurrentHashMap<>();
    private KLineWSClient client;
    private QuotationReceive quotationReceive;

    public KLineWS(String wsUrl, String proxyHost, Integer proxyPort, Map<String, Long> dataTimeMap,
            QuotationReceive quotationReceive, String blackNames, String env, int refreshQuotationSize) {
        this.wsUrl = wsUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.dataTimeMap = dataTimeMap;
        this.quotationReceive = quotationReceive;
        this.blackNames = blackNames;
        this.env = env;
        this.refreshQuotationSize = refreshQuotationSize;
    }

    public void start() {
        if (wsClient == null) {
            ConnectionPool pool = new ConnectionPool(3, 5L, TimeUnit.MINUTES);
            OkHttpClient.Builder builder = (new OkHttpClient.Builder())
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectionPool(pool);
            if (Text.isNotBlank(proxyHost) && proxyPort != null) {
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            }
            wsClient = builder.build();
        }
        running = true;
        super.start();
    }

    public void close() {
        running = false;
        if (wsClient != null) {
            wsClient.dispatcher().executorService().shutdown();
            wsClient = null;
        }
        if (client != null) {
            client.closeWebSocket();
        }
    }

    /**
     * 检查并且开启客户端
     */
    private boolean checkAndStartClient() {
        // 确认websocket是否正常
        if (client == null) {
            client = new KLineWSClient(wsUrl, this, wsClient);
            client.start();
            return false;
        }

        // 如果已经关闭或获取信息超时则重新开始
        if (client.isClose() || System.currentTimeMillis() - client.getPongTime() > 10000L) {
            client.closeWebSocket();
            client = null;
            LOG.warn(format("websocket连接关闭或返回数据超时", kv("pongInterval", System.currentTimeMillis() - client.getPongTime())));
            return false;
        }

        if (!client.isOpen()) {
            LOG.warn(format("websocket连接未建立成功"));
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        long printStatisticTime = System.currentTimeMillis();
        while (running) {
            try {
                Thread.sleep(1000);
                if (!checkAndStartClient()) {
                    continue;
                }

                // 每5秒执行一次ping操作
                if (System.currentTimeMillis() - client.getPongTime() > 5000) {
                    client.ping();
                }

                if (dataTimeMap == null || dataTimeMap.isEmpty()) {
                    LOG.warn(format("没有交易对不能发起WS请求"));
                    continue;
                }

                // 判断是否执行上一天数据请求, 每小时请求一次，每次知道成功为止，每次请求小时开始时间增加10秒以处理k线交替时间差
                long currentHourMills = (System.currentTimeMillis() - 10000) / (HOUR_MILLS) * (HOUR_MILLS);
//                int reqCount = 0;
                for (String symbol : dataTimeMap.keySet()) {
                    // 测试中的无行情交易对处理
                    if (blackNames != null && blackNames.contains(symbol)) {
                        continue;
                    }

                    // 处理订阅请求
                    if (System.currentTimeMillis() - dataSubTimeMap.getOrDefault(symbol, 0L) > 30000) {
                        client.subQuotation(symbol);
                        dataSubTimeMap.put(symbol, System.currentTimeMillis());
                    }

                    // 确保发送量不会超频
                    if (client.getLastReqTime(symbol) > currentHourMills) {
                        continue;
                    }
//                    reqCount++;
                    client.sendDayReqMessage(symbol);
//                    if (reqCount >= refreshQuotationSize) {
//                        break;
//                    }
                }

                if (System.currentTimeMillis() - printStatisticTime < 60000L) {
                    continue;
                }
                printStatisticTime = System.currentTimeMillis();
                // 打印交易对实时数据更新时间
                Map<String,Long> statistic = new HashMap<>();
                for (String symbol : dataTimeMap.keySet()) {
                    statistic.put(symbol, System.currentTimeMillis() -dataSubTimeMap.getOrDefault(symbol, 0L));
                }
                LOG.info(format("打印交易对实时数据更新状态", kv("statistic", statistic)));
            } catch (Exception e) {
                LOG.error(format("循环处理websocket状态时发生异常"), e);
            }
        }
    }

    @Override
    public void quotationReceive(String symbol, List<IndexSymbolDataVO> list) {
        this.dataTimeMap.put(symbol, System.currentTimeMillis());
        if (quotationReceive != null) {
            quotationReceive.quotationReceive(symbol, list);
        }
    }

    @Override
    public void subDataReceive(String symbol, IndexSymbolDataVO indexSymbolDataVO) {
        // 收到订阅消息时，距离当前更新时间小于半秒，则不发送redis
        dataSubTimeMap.put(symbol, System.currentTimeMillis());
        if (System.currentTimeMillis() - dataTimeMap.getOrDefault(symbol, 0L) < 500) {
            return;
        }
        this.dataTimeMap.put(symbol, System.currentTimeMillis());
        if (quotationReceive != null) {
            quotationReceive.subDataReceive(symbol, indexSymbolDataVO);
        }
    }

    @Override
    public void pongReceive() {

    }
}


