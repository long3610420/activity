package com.exshell.ops.activity.job.task.index;

import static com.exshell.bitex.commons.Log.format;
import static com.exshell.bitex.commons.Log.kv;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.exshell.bitex.general.model.IndexSymbolDataVO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.BufferedSource;
import okio.ByteString;
import okio.GzipSource;
import okio.Okio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 针对每个symbol的kline请求
 */
public class KLineWSClient extends WebSocketListener {

    public static final Logger LOG = LoggerFactory.getLogger(KLineWSClient.class);

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private volatile boolean close = false;
    private volatile long pongTime = System.currentTimeMillis();
    private Map<String, Long> lastReqTimeMap = new ConcurrentHashMap<>(100);
    private volatile boolean open = false;
    private Map<String, Integer> reqDayCountMap = new ConcurrentHashMap<>(100);
    private String wsUrl;
    private WebSocket sWebSocket;
    private QuotationReceive quotationReceive;
    private OkHttpClient wsClient;

    public KLineWSClient(String wsUrl, QuotationReceive quotationReceive, OkHttpClient wsClient) {
        this.wsUrl = wsUrl;
        this.quotationReceive = quotationReceive;
        this.wsClient = wsClient;
    }

    public void start() {
        close = false;
        open = false;
        Request request = new Request.Builder().url(wsUrl).build();
        sWebSocket = wsClient.newWebSocket(request, this);
    }

    private void sendMessage(String message) {
        if (!open) {
            LOG.warn(format("websocket还没有连接，不能执行发送消息操作"));
            return;
        }
        sWebSocket.send(message);
    }

    public Integer getDayReqCount(String symbol) {
        return reqDayCountMap.getOrDefault(symbol, 200);
    }

    /**
     * 发送请求
     */
    public void sendDayReqMessage(String symbol) {
        JSONObject obj = new JSONObject();
        obj.put("req", "market." + symbol + ".kline.1day");
        obj.put("from",
                LocalDate.now().atStartOfDay().minusDays(getDayReqCount(symbol)).atZone(ZoneId.systemDefault())
                        .toEpochSecond());
        obj.put("to", LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond());
        obj.put("id", "req_" + symbol + "_" + System.currentTimeMillis());
        sendMessage(obj.toJSONString());
    }

    public void subQuotation(String symbol) {
        JSONObject obj = new JSONObject();
        obj.put("sub", "market." + symbol + ".kline.1day");
        obj.put("id", "sub_" + symbol + "_" + System.currentTimeMillis());
        sendMessage(obj.toJSONString());
    }

    private void pong(Long id) {
        sendMessage(String.format("{\"pong\": %d}", id));
    }

    public void ping() {
        sendMessage(String.format("{\"ping\": %d}", System.currentTimeMillis()));
    }

    public void closeWebSocket() {
        LOG.warn(format("关闭websocket连接"));
        try {
            close = true;
            open = false;
            if (sWebSocket != null) {
                sWebSocket.close(NORMAL_CLOSURE_STATUS, "关闭sokect连接!");
            }
        } catch (Exception e) {
            LOG.error(format("关闭websocket连接异常"));
        }
        sWebSocket = null;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        LOG.info(format("Kline websocket 启动"));
        open = true;
        pongTime = System.currentTimeMillis();
    }

    private String getSymbol(String topic) {
        return topic.split("\\.")[1];
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        pongTime = System.currentTimeMillis();
        JSONObject obj = JSON.parseObject(text);

        // ping 数据
        if (obj.containsKey("ping")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(format("KLineWS收到ping消息，反馈ping消息"));
            }
            pong(obj.getLong("ping"));
            return;
        }

        // pong 数据
        if (obj.containsKey("pong")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(format("KLineWS收到pong消息:" + obj.getLong("pong")));
            }
            if (quotationReceive != null) {
                quotationReceive.pongReceive();
            }
            return;
        }

        // 请求返回数据
        if (obj.containsKey("rep")) {
            if (!"ok".equals(obj.getString("status"))) {
                LOG.error(format("KLineWS收到错误req消息", kv("text", text)));
                return;
            }
            String symbol = getSymbol(obj.getString("rep"));
            List<IndexSymbolDataVO> list = parseKlineForReq(symbol, obj);
            if (quotationReceive != null) {
                quotationReceive.quotationReceive(symbol, list);
            }

            // 如果已经请求到大于2天数据，则以后获取数据都为2天
            if (list.size() > 2) {
                reqDayCountMap.put(symbol, 2);
            }

            // 设置最后请求结果时间，避免重复请求
            lastReqTimeMap.put(symbol, System.currentTimeMillis());
            return;
        }

        // 订阅推送数据
        if (obj.containsKey("ch")) {
            String symbol = getSymbol(obj.getString("ch"));
            IndexSymbolDataVO indexSymbolDataVO = parseKlineForSub(symbol, obj);
            if (quotationReceive != null) {
                quotationReceive.subDataReceive(symbol, indexSymbolDataVO);
            }
            return;
        }

        if (obj.containsKey("subbed")) {
            if (!"ok".equals(obj.getString("status"))) {
                LOG.error(format("KLineWS收到错误subbed消息", kv("text", text)));
            }
            return;
        }

        LOG.error(format("KLineWS收到未知消息", kv("text", text)));
    }

    /**
     * symbol 数据解析
     */
    private List<IndexSymbolDataVO> parseKlineForReq(String symbol, JSONObject obj) {
        List<IndexSymbolDataVO> result = new ArrayList<>();
        JSONArray array = obj.getJSONArray("data");
        for (int k = 0; k < array.size(); k++) {
            JSONObject item = array.getJSONObject(k);
            result.add(new IndexSymbolDataVO(symbol, item.getLong("id"), item.getBigDecimal("vol"),
                    item.getBigDecimal("close"), item.getBigDecimal("amount"),
                    item.getBigDecimal("open")));
        }
        return result;
    }

    /**
     * symbol 数据解析
     */
    private IndexSymbolDataVO parseKlineForSub(String symbol, JSONObject obj) {
        JSONObject item = obj.getJSONObject("tick");
        return new IndexSymbolDataVO(symbol, item.getLong("id"), item.getBigDecimal("vol"),
                item.getBigDecimal("close"), item.getBigDecimal("amount"),
                item.getBigDecimal("open"));
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        InputStream is = new ByteArrayInputStream(bytes.toByteArray());
        GzipSource source = new GzipSource(Okio.source(is));
        BufferedSource bufferedSource = Okio.buffer(source);
        String message;
        try {
            message = bufferedSource.readUtf8();
            onMessage(webSocket, message);
        } catch (IOException e) {
            LOG.error("KLineWS收到数据处理异常", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LOG.error("KLineWS收到数据关闭ByteArrayInputStream失败", e);
            }
            try {
                bufferedSource.close();
            } catch (IOException e) {
                LOG.error("KLineWS收到数据关闭bufferedSource失败", e);
            }
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        closeWebSocket();
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        closeWebSocket();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        closeWebSocket();
    }

    public boolean isClose() {
        return close;
    }

    public boolean isOpen() {
        return open;
    }

    public long getPongTime() {
        return pongTime;
    }

    public long getLastReqTime(String symbol) {
        return lastReqTimeMap.getOrDefault(symbol, 0L);
    }
}
