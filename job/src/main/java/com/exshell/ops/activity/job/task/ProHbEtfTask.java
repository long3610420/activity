// package com.exshell.ops.activity.job.task;
//
// import static com.exshell.bitex.commons.Log.format;
// import static com.exshell.bitex.commons.Log.kv;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
// import com.exshell.bitex.commons.util.Text;
// import com.exshell.bitex.general.constant.GeneralConstant;
// import com.exshell.bitex.general.domain.IndexRealtimeDO;
// import com.exshell.bitex.general.enums.FundName;
// import com.exshell.bitex.general.enums.IndexConfigType;
// import com.exshell.bitex.general.enums.IndexKlineType;
// import com.exshell.bitex.general.helper.HttpInterHelper;
// import com.exshell.bitex.general.helper.ProIndexHelper;
// import com.exshell.bitex.general.helper.RabbitMqHelper;
// import com.exshell.bitex.general.model.EtfInfoVO;
// import com.exshell.bitex.general.model.EtfSymbolVO;
// import com.exshell.bitex.general.model.IndexPointVO;
// import com.exshell.bitex.general.model.IndexSymbolDataVO;
// import com.exshell.bitex.general.service.ProIndexService;
// import java.math.BigDecimal;
// import java.time.Duration;
// import java.time.Instant;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.Executor;
// import java.util.concurrent.Executors;
// import javax.annotation.PostConstruct;
// import javax.annotation.Resource;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.util.CollectionUtils;
//
// /**
//  * ETF基金净值计算
//  */
// public class ProHbEtfTask {
//
//     private static final Logger LOG = LoggerFactory.getLogger(ProHbEtfTask.class);
//
//     @Value("${etf.thread.count}")
//     private int executeThreadCount = 50;
//     @Value("${etf.host.url}")
//     private String etfHost;
//     @Value("${etf.info.uri}")
//     private String etfInfoUri;
//     @Value("${etf.account.uri}")
//     private String etfAccountUri;
//     @Value("#{new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ssz\").parse(\"${etf.start.time}\")}")
//     private Date startTime;
//
//     @Resource
//     private HttpInterHelper httpInterHelper;
//     @Resource
//     private ProIndexService proIndexService;
//     @Resource
//     private ProIndexHelper proIndexHelper;
//     @Resource
//     private RabbitMqHelper rabbitMqHelper;
//
//     // 用于启动时判断补充数据是否补充完成
//     private Map<FundName, Boolean> lostComplete = new ConcurrentHashMap<>();
//
//     private Executor executor;
//
//     /**
//      * 初始化
//      */
//     @PostConstruct
//     public void init() {
//         executor = Executors.newFixedThreadPool(executeThreadCount);
//         // 补偿数据
//         for (FundName fundName : FundName.values()) {
//             repairLostData(fundName);
//         }
//     }
//
//     /**
//      * 提供job函数
//      */
//     public void calEtfValue() {
//         // 如果日期小于开始时间则退出
//         if (startTime.getTime() > System.currentTimeMillis()) {
//             LOG.info(format("etf净值计算-当前时间小于开始时间，不能执行基金净值计算"));
//             return;
//         }
//
//         long now = Instant.now().getEpochSecond();
//         for (FundName fundName : FundName.values()) {
//             EtfInfoVO etfInfo = proIndexHelper.getEtfCache(fundName);
//             if (etfInfo == null) {
//                 LOG.warn(format("etf净值计算-未找到缓存中的基金信息数据，不能执行基金净值计算"));
//                 continue;
//             }
//             List<EtfSymbolVO> etfSymbolList = etfInfo.symbols;
//             if (etfSymbolList == null) {
//                 LOG.warn(format("etf净值计算-未找到缓存中的基金配置数据，不能执行基金净值计算"));
//                 continue;
//             }
//
//             // 获取实时数据
//             Set<String> symbolSet = new HashSet<>();
//             etfSymbolList.forEach(data -> symbolSet.add(data.symbol));
//             Map<String, BigDecimal> data = proIndexHelper.getRealTimeData(symbolSet);
//             BigDecimal etfValue = calEtfValue(etfInfo, data);
//
//             if (etfValue == null) {
//                 LOG.warn(format("etf净值计算-实时计算时时未能计算出有效数据"));
//                 continue;
//             }
//             // 计算并保存
//             IndexPointVO pointVO = new IndexPointVO(fundName.getName(), now, now, etfValue);
//             proIndexService.saveIndexPoint(pointVO, 0, fundName.getName());
//         }
//     }
//
//     /**
//      * 提供job使用入口，更新etf信息
//      */
//     public void updateEtfInfo() {
//         for (FundName fundName : FundName.values()) {
//             try {
//                 updateEtfInfo(fundName);
//             } catch (Exception e) {
//                 LOG.error(format("etf净值计算-获取基金信息发生异常", kv("fundName", fundName)), e);
//             }
//         }
//     }
//
//     /**
//      * 对外发送etf
//      */
//     public void sendEtf() {
//
//         // 如果日期小于开始时间则退出
//         if (startTime.getTime() > System.currentTimeMillis()) {
//             LOG.info(format("etf净值计算-当前时间小于开始时间，不能执行基金净值发送"));
//             return;
//         }
//
//         for (FundName fundName : FundName.values()) {
//             if (lostComplete.containsKey(fundName)) {
//                 sendIndexData(fundName);
//             } else {
//                 LOG.warn(format("etf净值计算-发送etf未获取的丢失数据补充状态", kv("fundName", fundName)));
//             }
//         }
//     }
//
//     /**
//      * 发送实时数据
//      */
//     private void sendIndexData(FundName fundName) {
//         rabbitMqHelper.sendMessage(channel -> {
//             // 读取最后数据，如果没取到，则发送1小时数据
//             Long dataTime = proIndexService.getLastSendData(fundName.getName()).getData();
//             // 根据数据时间读取数据库，每次读取1000条
//             List<IndexRealtimeDO> realtimeList = proIndexService
//                     .findIndexAsc(fundName.getName(), dataTime, GeneralConstant.MAX_SEND_COUNT).getData();
//             if (CollectionUtils.isEmpty(realtimeList)) {
//                 if (LOG.isDebugEnabled()) {
//                     LOG.debug(
//                             format("etf净值计算-发送实时数据时未能从数据库获取实时数据", kv("fundName", fundName), kv("dataTime", dataTime)));
//                 }
//                 return;
//             }
//             for (IndexRealtimeDO realtimeDO : realtimeList) {
//                 boolean success = rabbitMqHelper.sendMessage(channel, convertIndex(fundName, realtimeDO));
//                 if (success) {
//                     LOG.info(format("etf净值计算-发送etf实时数据成功", kv("time", realtimeDO.getIndexTime()),
//                             kv("value", realtimeDO.getIndexValue())));
//                 } else {
//                     // 这种情况不再执行，等待下次
//                     LOG.warn(format("etf净值计算-发送etf实时数据失败", kv("time", realtimeDO.getIndexTime()),
//                             kv("value", realtimeDO.getIndexValue())));
//                     return;
//                 }
//             }
//             proIndexService.saveConfig(IndexConfigType.LAST_SEND_INDEX,
//                     realtimeList.get(realtimeList.size() - 1).getIndexTime().toString(), fundName.getName());
//         });
//     }
//
//     /**
//      * 数据转换，符合k线处理程序邀请
//      * json:
//      * {
//      * id: long,
//      * ts: long,
//      * order-type: 'buy-market'|'sell-market'|'buy-limit'|'sell-limit'|'index',
//      * symbol: 'btc/usdt',
//      * items: [{
//      * role: 'maker'|'taker',
//      * price: 11111.111111,
//      * amount: 0.1111
//      * }]
//      * }
//      */
//     private String convertIndex(FundName fundName, IndexRealtimeDO index) {
//         JSONObject item = new JSONObject();
//         item.put("role", "maker");
//         item.put("price", index.getIndexValue());
//         item.put("amount", 0);
//
//         JSONArray items = new JSONArray();
//         items.add(item);
//
//         Long ts = index.getIndexTime() * 1000;
//         JSONObject jsonObject = new JSONObject();
//         jsonObject.put("id", ts);
//         jsonObject.put("ts", ts);
//         jsonObject.put("order-type", "index");
//         jsonObject.put("symbol", fundName.getName());
//         jsonObject.put("items", items);
//         return jsonObject.toJSONString();
//     }
//
//
//     /**
//      * 更新etf
//      */
//     private void updateEtfInfo(FundName fundName) {
//         // 获取基金信息
//         String url = etfHost + etfInfoUri;
//         String infoStr = httpInterHelper.getForNoProxy(url, HttpInterHelper.param("etf_name", fundName.name()));
//         if (Text.isBlank(infoStr) || !infoStr.trim().startsWith("{")) {
//             LOG.error(format("etf净值计算-获取基金信息失败", kv("url", url), kv("etf_name", fundName), kv("result", infoStr)));
//             return;
//         }
//
//         JSONObject obj = JSON.parseObject(infoStr);
//         if (obj == null || !obj.getBoolean("success")) {
//             LOG.error(format("etf净值计算-获取基金信息失败", kv("url", url), kv("etf_name", fundName), kv("result", infoStr)));
//         }
//
//         EtfInfoVO etfInfo = obj.getObject("data", EtfInfoVO.class);
//         if (etfInfo == null || etfInfo.etfAmount == 0) {
//             LOG.error(format("etf净值计算-获取基金信息解析失败", kv("url", url), kv("etf_name", fundName), kv("result", infoStr)));
//             return;
//         }
//
//         // 获取基金币种占比信息
//         url = etfHost + etfAccountUri;
//         String accountStr = httpInterHelper.getForNoProxy(url, HttpInterHelper.param("etf_name", fundName.name()));
//         if (Text.isBlank(accountStr) || !accountStr.trim().startsWith("{")) {
//             LOG.error(format("etf净值计算-获取基金账户比例失败", kv("url", url), kv("etf_name", fundName), kv("result", infoStr)));
//             return;
//         }
//
//         obj = JSON.parseObject(accountStr);
//         if (obj == null || !obj.getBoolean("success")) {
//             LOG.error(format("etf净值计算-获取基金信息比例失败", kv("url", url), kv("etf_name", fundName), kv("result", accountStr)));
//             return;
//         }
//
//         JSONArray array = obj.getJSONArray("data");
//         if (CollectionUtils.isEmpty(array)) {
//             LOG.error(
//                     format("etf净值计算-获取基金账户比例解析失败", kv("url", url), kv("etf_name", fundName), kv("result", accountStr)));
//             return;
//         }
//         // 增加交易对名便于使用
//         List<EtfSymbolVO> etfSymbolList = new ArrayList<>();
//         for (int i = 0; i < array.size(); i++) {
//             EtfSymbolVO data = array.getObject(i, EtfSymbolVO.class);
//             data.symbol = data.currency.toLowerCase() + "usdt";
//             etfSymbolList.add(data);
//         }
//         etfInfo.symbols = etfSymbolList;
//
//         // 数据存入缓存
//         proIndexHelper.setEtfCache(fundName, etfInfo);
//         LOG.info(format("etf净值计算-获取建仓数据成功放入缓存", kv("etfInfo", JSON.toJSONString(etfInfo))));
//     }
//
//     /**
//      * 计算etf净值
//      */
//     private BigDecimal calEtfValue(EtfInfoVO etfInfo, Map<String, BigDecimal> data) {
//         List<EtfSymbolVO> list = etfInfo.symbols;
//         BigDecimal total = BigDecimal.ZERO;
//         for (EtfSymbolVO symbolVO : list) {
//             BigDecimal price = data.get(symbolVO.symbol);
//             if (price == null) {
//                 if ("usdt".equals(symbolVO.currency)) {
//                     LOG.warn(format("etf净值计算-没有找到symbol价格考虑usdt情况，使用1", kv("symbol", symbolVO.symbol)));
//                     price = BigDecimal.ONE;
//                 } else {
//                     LOG.warn(format("etf净值计算-没有找到symbol价格", kv("symbol", symbolVO.symbol)));
//                     return null;
//                 }
//             }
//             total = total.add(symbolVO.amount.multiply(price));
//         }
//         BigDecimal value = total.divide(BigDecimal.valueOf(etfInfo.etfAmount), 4, BigDecimal.ROUND_HALF_UP);
//         LOG.info(format("etf净值计算-实时计算", kv("time", Instant.now().getEpochSecond()),
//                 kv("value", value), kv("data", JSON.toJSONString(data))));
//         return value;
//     }
//
//     /**
//      * 执行时先从缓存中获取未完成数据
//      */
//     private void repairLostData(FundName fundName) {
//         // 获取最后计算点时间，以补充丢失
//         IndexRealtimeDO indexRealtimeDO = proIndexService.getlastProIndex(fundName.getName()).getData();
//         // 没有或间隔小于1分钟，则不处理流失数据，因为获取行情数据都是1分钟数据
//         if (indexRealtimeDO == null || Instant.now().getEpochSecond() - indexRealtimeDO.getIndexTime() < 60) {
//             lostComplete.put(fundName, true);
//             return;
//         }
//         // 补充丢失的指数数据
//         executor.execute(() -> {
//             try {
//                 fullLostStatistics(fundName, indexRealtimeDO.getIndexTime());
//             } catch (Exception e) {
//                 LOG.error("etf净值计算-重启指数计算程序后补充丢失数据时异常", e);
//             }
//             lostComplete.put(fundName, true);
//         });
//     }
//
//     /**
//      * 填充统计数据
//      * 传入数据以秒为单位
//      */
//     private void fullLostStatistics(FundName fundName, long indexTime) {
//         long now = Instant.now().getEpochSecond();
//         long startTime = IndexKlineType.MINUTE1.getStartTime(indexTime);
//
//         EtfInfoVO etfInfo = proIndexHelper.getEtfCache(fundName);
//         if (etfInfo == null) {
//             LOG.warn(format("etf净值计算-未找到缓存中的基金信息数据，无法补偿"));
//             return;
//         }
//
//         List<EtfSymbolVO> etfSymbolList = etfInfo.symbols;
//         if (etfSymbolList == null) {
//             LOG.warn(format("etf净值计算-未找到缓存中的基金配置数据，无法补偿"));
//             return;
//         }
//
//         // 获取数据
//         Map<String, Map<Long, BigDecimal>> datas = new HashMap<>();
//         for (EtfSymbolVO etfSymbolVO : etfSymbolList) {
//             List<IndexSymbolDataVO> list = readLostData(etfSymbolVO.symbol, startTime);
//             Map<Long, BigDecimal> map = new HashMap<>();
//             list.forEach(symbolDataVO -> map.put(symbolDataVO.time, symbolDataVO.close));
//             datas.put(etfSymbolVO.symbol, map);
//             LOG.info(format("etf净值计算-补充数据时查询到数据",
//                     kv("symbol", etfSymbolVO.symbol), kv("data", JSON.toJSONString(list))));
//         }
//
//         // 补充缺失数据
//         for (long time = startTime; time < now; time += 60) {
//             Map<String, BigDecimal> data = new HashMap<>();
//             for (EtfSymbolVO symbol : etfSymbolList) {
//                 Map<Long, BigDecimal> closeTimeMap = datas.get(symbol.symbol);
//                 BigDecimal close = closeTimeMap.get(time);
//                 if (close == null) {
//                     break;
//                 }
//                 data.put(symbol.symbol, close);
//             }
//             // 未取到足够的数据
//             if (data.size() != etfSymbolList.size()) {
//                 LOG.warn(format("etf净值计算-补充数据时数据不全", kv("startTime", time),
//                         kv("symbol", JSON.toJSONString(etfSymbolList)), kv("data", JSON.toJSONString(data))));
//                 continue;
//             }
//
//             // 开始计算,将一分钟拆成15秒数据，进行处理
//             for (long interval = 0; interval < 60; interval += 15) {
//                 BigDecimal etfValue = calEtfValue(etfInfo, data);
//                 if (etfValue == null) {
//                     LOG.warn(format("etf净值计算-补充数据时未能计算出有效数据"));
//                     continue;
//                 }
//                 try {
//                     // 排除重复数据
//                     long dataTime = time + interval;
//                     if (dataTime <= indexTime || dataTime > now) {
//                         continue;
//                     }
//                     IndexPointVO pointVO = new IndexPointVO(fundName.getName(), now, dataTime, etfValue);
//                     proIndexService.saveIndexPoint(pointVO, 0, fundName.getName());
//                 } catch (Exception e) {
//                     LOG.error(format("etf净值计算-保存补充指数数据失败"), e);
//                 }
//             }
//         }
//     }
//
//     /**
//      * 读取symbol的分钟数据
//      */
//     private List<IndexSymbolDataVO> readLostData(String symbol, long startTime) {
//         List<IndexSymbolDataVO> list = null;
//         while (true) {
//             long count = Duration.between(Instant.ofEpochSecond(startTime), Instant.now()).toMinutes() + 10;
//             if (count > 2000) {
//                 count = 2000;
//             }
//
//             try {
//                 list = proIndexHelper.readData(symbol, "1min", (int) count);
//             } catch (Exception e) {
//                 LOG.error(format("etf净值计算-填充基金丢失数据获取过程发生异常", kv("symbol", symbol),
//                                 kv("period", "1min"), kv("symbolCount", count)), e);
//             }
//
//             if (!CollectionUtils.isEmpty(list)) {
//                 break;
//             }
//             try {
//                 Thread.sleep(1000);
//             } catch (InterruptedException e) {
//                 LOG.error(
//                         format("etf净值计算-填充基金丢失数据过程发生异常", kv("symbol", symbol), kv("period", "1min"),
//                                 kv("symbolCount", count)),
//                         e);
//             }
//         }
//         return list;
//     }
// }
