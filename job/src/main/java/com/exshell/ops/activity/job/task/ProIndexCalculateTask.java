// package com.exshell.ops.activity.job.task;
//
// import static com.exshell.bitex.commons.Log.format;
// import static com.exshell.bitex.commons.Log.kv;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
// import com.exshell.bitex.commons.Result;
// import com.exshell.bitex.commons.cache.RedisClient;
// import com.exshell.bitex.commons.util.Text;
// import com.exshell.bitex.general.constant.GeneralConstant;
// import com.exshell.bitex.general.domain.IndexRealtimeDO;
// import com.exshell.bitex.general.domain.IndexSampleSymbolDO;
// import com.exshell.bitex.general.domain.IndexSymbolDayKLineDO;
// import com.exshell.bitex.general.enums.IndexConfigType;
// import com.exshell.bitex.general.enums.IndexKlineType;
// import com.exshell.bitex.general.enums.IndexName;
// import com.exshell.bitex.general.enums.IndexPeriodGenerateType;
// import com.exshell.bitex.general.enums.IndexSymbolState;
// import com.exshell.bitex.general.helper.HttpInterHelper;
// import com.exshell.bitex.general.helper.ProIndexHelper;
// import com.exshell.bitex.general.helper.RabbitMqHelper;
// import com.exshell.bitex.general.model.IndexCalculateConfigVO;
// import com.exshell.bitex.general.model.IndexParamVO;
// import com.exshell.bitex.general.model.IndexPointVO;
// import com.exshell.bitex.general.model.IndexSymbolDataVO;
// import com.exshell.bitex.general.model.IndexSymbolInfoVO;
// import com.exshell.bitex.general.service.ProIndexService;
// import com.exshell.ops.activity.job.task.index.CalKlineExecuter;
// import com.exshell.ops.activity.job.task.index.CalKlineHandler;
// import com.exshell.ops.activity.job.task.index.CalKlineRealtimeHandler;
// import com.exshell.ops.activity.job.task.index.KLineWS;
// import com.exshell.ops.activity.job.task.index.QuotationReceive;
// import com.exshell.bitex.general.utils.DateUtil;
// import java.io.IOException;
// import java.io.UnsupportedEncodingException;
// import java.math.BigDecimal;
// import java.time.Duration;
// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Base64;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.SortedMap;
// import java.util.TreeMap;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.Executor;
// import java.util.concurrent.Executors;
// import javax.annotation.PostConstruct;
// import javax.annotation.PreDestroy;
// import javax.annotation.Resource;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.util.CollectionUtils;
//
// /**
//  * 每日行情更新任务
//  */
// public class ProIndexCalculateTask implements QuotationReceive {
//
//     private static final Logger LOG = LoggerFactory.getLogger(ProIndexCalculateTask.class);
//     private static final String QUOTES_BY_DAYS_KEY = "quotesByDays";
//     @Value("${index.thread.count}")
//     private int executeThreadCount = 50;
//     @Resource
//     private ProIndexService proIndexService;
//     @Resource
//     private ProIndexHelper proIndexHelper;
//     @Resource
//     private HttpInterHelper httpInterHelper;
//     @Resource
//     private RedisClient redisClient;
//     @Resource
//     private RabbitMqHelper rabbitMqHelper;
//
//     @Value("${index.pro.symbol.url}")
//     private String proSymbolUrl;
//     @Value("${uc.base.url}")
//     private String ucUrl;
//     @Value("${index.warn.access.key}")
//     private String ucAccessKey;
//     @Value("${index.warn.secret.key}")
//     private String ucSecretKey;
//     @Value("${index.warn.email.path}")
//     private String warnEmailPath;
//     @Value("${index.warn.phone.path}")
//     private String warnPhonePath;
//     @Value("#{${index.warn.phone}}")
//     private Map<String, String> warnPhones;
//     @Value("#{'${index.warn.email}'.split(',')}")
//     private List<String> warnEmails;
//     @Value("#{${index.warn.maximum.time.interval}}")
//     private Map<String, Integer> maxTimeInterval;
//     @Value("${index.warn.message.max.num}")
//     private Integer maxWarnNum;
//     @Value("${index.warn.message.max.timeout}")
//     private Integer maxWarnTimeout;
//     @Value("${index.refresh.quotation.size}")
//     private Integer refreshQuotationSize;
//     @Value("${index.refresh.quotation.interval.mills}")
//     private Long refreshQuotationInterval;
//     @Value("${proxy.host}")
//     private String proxyHost;
//     @Value("${proxy.port}")
//     private Integer proxyPort;
//     @Value("${index.kline.ws.url}")
//     private String klineWsUrl;
//     @Value("${index.pro.symbol.black.name}")
//     private String blackNames;
//     @Value("${deploy.env}")
//     private String env;
//
//     // 用于启动时判断补充数据是否补充完成
//     private Map<IndexName, Boolean> lostComplete = new ConcurrentHashMap<>();
//
//     // 用于k线数据ws更新
//     private KLineWS kLineWS;
//
//     private Map<String, String> symbolNameMap = new ConcurrentHashMap<>();
//     private Map<String, Long> symbolDataTimeMap = new ConcurrentHashMap<>();
//     private Executor executor;
//     private Map<String, CalKlineExecuter> calKlineExecuterMap = new ConcurrentHashMap<>();
//
//     /**
//      * 初始化时判断基期数据是否存在，如果不存在则计算基期数据
//      */
//     @PostConstruct
//     public void init() {
//         try {
//             executor = Executors.newFixedThreadPool(executeThreadCount);
//
//             // 初始化symbol和名的映射
//             List<IndexSampleSymbolDO> symbolInfos = proIndexService.findAllSymbol().getData();
//             if (!CollectionUtils.isEmpty(symbolInfos)) {
//                 symbolInfos.forEach(info -> {
//                     if (IndexSymbolState.valid(info.getState())) {
//                         symbolNameMap.put(info.getSymbolName(), info.getCoinName());
//                     }
//                 });
//             }
//
//             // 更新实时数据,更新2个季度数据
// //            int seconds = Integer.parseInt(maxTimeInterval.get(QUOTES_BY_DAYS_KEY));
// //            int count = 0;
// //            for (String symbol : symbolNameMap.keySet()) {
// //                count++;
// //                if (count % refreshQuotationSize == 0) {
// //                    Thread.sleep(1100);
// //                }
// //                updateQuotesByDaysForSymbol(symbol, 200, seconds);
// //            }
//             for (String symbol : symbolNameMap.keySet()) {
//                 symbolDataTimeMap.put(symbol, 0L);
//             }
//
//             // 执行ws数据更新
//             kLineWS = new KLineWS(klineWsUrl, proxyHost, proxyPort, symbolDataTimeMap, this, blackNames, env,
//                     refreshQuotationSize);
//             kLineWS.start();
//
//             // 执行时先从缓存中获取未完成数据
//             for (IndexName indexName : IndexName.values()) {
//                 continueFinish(indexName);
//             }
//
//         } catch (Exception e) {
//             LOG.error("指数计算-初始化时计算基期样本和除数异常", e);
//         }
//     }
//
//     /**
//      * 关闭
//      */
//     @PreDestroy
//     public void destroy() {
//         kLineWS.close();
//     }
//
//     /**
//      * 执行时先从缓存中获取未完成数据
//      */
//     private void continueFinish(IndexName indexName) {
//         // 执行时先从缓存中获取未完成数据
//         calKlineExecuterMap.put(indexName.getName(), createKlineExecuter(indexName));
//
//         // 获取最后计算点时间，以补充丢失
//         IndexRealtimeDO indexRealtimeDO = proIndexService.getlastProIndex(indexName.getName()).getData();
//         // 没有或间隔小于1分钟，则不处理流失数据，因为获取行情数据都是1分钟数据
//         if (indexRealtimeDO == null || Instant.now().getEpochSecond() - indexRealtimeDO.getIndexTime() < 60) {
//             lostComplete.put(indexName, true);
//             return;
//         }
//         // 补充丢失的指数数据
//         executor.execute(() -> {
//             try {
//                 fullLostStatistics(indexName.getName(), indexRealtimeDO.getIndexTime());
//             } catch (Exception e) {
//                 LOG.error("指数计算-重启指数计算程序后补充丢失数据时异常", e);
//             }
//             lostComplete.put(indexName, true);
//         });
//     }
//
//     /**
//      * 创建统计计算实例
//      */
//     private CalKlineExecuter createKlineExecuter(IndexName indexName) {
//         CalKlineExecuter klineExecuter = new CalKlineExecuter();
//         klineExecuter.setProIndexService(proIndexService);
//         klineExecuter.setProIndexHelper(proIndexHelper);
//
//         // 初始化统计数据计算实例
//         IndexKlineType type = IndexKlineType.DAY1;
//         CalKlineHandler handler = new CalKlineRealtimeHandler(type, indexName);
//         klineExecuter.add(handler);
//         return klineExecuter;
//     }
//
//     /**
//      * 填充统计数据
//      * 传入数据以秒为单位
//      */
//     private void fullLostStatistics(String indexName, long indexTime) {
//         long now = Instant.now().getEpochSecond();
//         long startTime = IndexKlineType.MINUTE1.getStartTime(indexTime);
//         IndexParamVO paramVO = proIndexService.getLastProIndexPeriod(indexName).getData();
//         if (paramVO == null) {
//             LOG.warn(format("指数计算-补充指数数据时，没有获取到权重和除数数据"));
//             sendWarnMessage("补充指数数据未找到权重数据", "补充指数数据时，没有获取到权重和除数数据");
//             return;
//         }
//
//         // 获取数据
//         Map<String, Map<Long, BigDecimal>> datas = new HashMap<>();
//         for (IndexSymbolDataVO symbol : paramVO.symbols) {
//             List<IndexSymbolDataVO> list = readLostData(symbol.name, startTime);
//             if (CollectionUtils.isEmpty(list)) {
//                 LOG.warn(format("指数计算-补充指数数据时，没有足够的分钟数据"));
//                 sendWarnMessage("补充指数数据未获取到分钟数据", "补充指数数据时，没有足够的分钟数据");
//                 return;
//             }
//             Map<Long, BigDecimal> map = new HashMap<>();
//             list.forEach(symbolDataVO -> map.put(symbolDataVO.time, symbolDataVO.close));
//             datas.put(symbol.name, map);
//         }
//
//         // 补充缺失数据
//         for (long time = startTime; time < now; time += 60) {
//             Map<String, BigDecimal> data = new HashMap<>();
//             for (IndexSymbolDataVO symbol : paramVO.symbols) {
//                 Map<Long, BigDecimal> closeTimeMap = datas.get(symbol.name);
//                 BigDecimal close = closeTimeMap.get(time);
//                 if (close == null) {
//                     break;
//                 }
//                 data.put(symbol.name, close);
//             }
//             // 未取到足够的数据
//             if (data.size() != paramVO.symbols.size()) {
//                 continue;
//             }
//
//             // 开始计算,将一分钟拆成15秒数据，进行处理
//             for (long interval = 0; interval < 60; interval += 15) {
//                 BigDecimal indexPoint = proIndexHelper.calProIndex(paramVO.divide, paramVO.symbols, data);
//                 try {
//                     // 排除重复数据
//                     long dataTime = time + interval;
//                     if (dataTime <= indexTime || dataTime > now) {
//                         continue;
//                     }
//                     IndexPointVO pointVO = new IndexPointVO(now, dataTime, indexPoint);
//                     proIndexService.saveIndexPoint(pointVO, paramVO.id, indexName);
//                 } catch (Exception e) {
//                     LOG.error(format("指数计算-保存补充指数数据失败"), e);
//                     sendWarnMessage("补充指数数据异常写数据库失败", "补充指数数据时，向数据库写指数数据失败");
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
//             long count = Duration.between(Instant.ofEpochSecond(startTime), Instant.now()).toMinutes() + 1;
//             if (count > 2000) {
//                 count = 2000;
//             }
//
//             // 读取数据
//             try {
//                 list = proIndexHelper.readData(symbol, "1min", (int) count);
//             } catch (Exception e) {
//                 LOG.error(format("指数计算-填充丢失数据过程读取数据发生异常", kv("symbol", symbol), kv("period", "1min"),
//                         kv("symbolCount", count)), e);
//             }
//
//             // 读取到数据则跳出
//             if (!CollectionUtils.isEmpty(list)) {
//                 break;
//             }
//
//             // 否则等待100毫秒再次读取
//             try {
//                 Thread.sleep(100);
//             } catch (InterruptedException e) {
//                 LOG.error(format("指数计算-填充丢失数据过程休眠100毫秒发生异常", kv("symbol", symbol), kv("period", "1min"),
//                         kv("symbolCount", count)), e);
//             }
//         }
//         return list;
//     }
//
//     /**
//      * 计算Pro指数,每15秒执行一次
//      */
//     public void calProIndex() {
//         try {
//             for (IndexName indexName : IndexName.values()) {
//                 calProIndexUpdate(indexName);
//             }
//         } catch (Exception e) {
//             LOG.error(format("指数计算-计算指数异常"), e);
//         }
//     }
//
//     /**
//      * 更新实时点数据
//      */
//     private void calProIndexUpdate(IndexName indexName) {
//
//         // 获取除数和样本权重
//         IndexParamVO vo = proIndexService.getLastProIndexPeriod(indexName.getName()).getData();
//         if (vo == null) {
//             LOG.warn(format("指数计算-没有获取到权重和除数数据"));
//             return;
//         }
//
//         BigDecimal divide = vo.divide;
//         List<IndexSymbolDataVO> symbolDatas = vo.symbols;
//
//         Map<String, BigDecimal> closeMap = proIndexHelper.getRealtimeClose(vo.symbols);
//         BigDecimal indexPoint = proIndexHelper.calProIndex(divide, symbolDatas, closeMap);
//         long now = Instant.now().getEpochSecond();
//         IndexPointVO indexPointVO = new IndexPointVO(now, now, indexPoint);
//         indexPointVO.indexName = indexName.getName();
//
//         // 更新实时点数据
//         executor.execute(() -> {
//             // 更新实时指数
//             proIndexHelper.saveRealtimeIndexPoint(indexPointVO);
//             proIndexService.saveIndexPoint(indexPointVO, vo.id, indexName.getName());
//         });
//
//         // 数据统计处理
//         executor.execute(() -> calKlineExecuterMap.get(indexName.getName()).execute(indexPointVO));
//     }
//
//     /**
//      * 每秒钟执行一次
//      * 发送实时数据
//      */
//     public void sendIndex() {
//         for (IndexName indexName : IndexName.values()) {
//             if (lostComplete.containsKey(indexName)) {
//                 // TODO 为测试注释掉发送代码
//                 sendIndexData(indexName);
//             } else {
//                 LOG.warn(format("指数计算-发送数据未获取的丢失数据补充状态", kv("indexName", indexName)));
//             }
//         }
//     }
//
//     /**
//      * 发送实时数据
//      */
//     private void sendIndexData(IndexName indexName) {
//         // 读取最后数据，如果没取到，则发送1小时数据
//         Long dataTime = proIndexService.getLastSendData(indexName).getData();
//         // 根据数据时间读取数据库，每次读取1000条
//         List<IndexRealtimeDO> realtimeList = proIndexService
//                 .findIndexAsc(indexName, dataTime, GeneralConstant.MAX_SEND_COUNT).getData();
//         if (CollectionUtils.isEmpty(realtimeList)) {
//             if (LOG.isDebugEnabled()) {
//                 LOG.debug(format("指数计算-发送实时数据时未能从数据库获取实时数据", kv("indexName", indexName), kv("dataTime", dataTime)));
//             }
//             return;
//         }
//         for (IndexRealtimeDO realtimeDO : realtimeList) {
//             boolean success = rabbitMqHelper.sendMessage(convertIndex(indexName, realtimeDO));
//             if (success) {
//                 LOG.info(format("指数计算-发送实时数据成功", kv("time", realtimeDO.getIndexTime()),
//                         kv("value", realtimeDO.getIndexValue())));
//             } else {
//                 // 这种情况不再执行，等待下次
//                 LOG.warn(format("指数计算-发送实时数据失败", kv("time", realtimeDO.getIndexTime()),
//                         kv("value", realtimeDO.getIndexValue())));
//                 return;
//             }
//         }
//         proIndexService.saveConfig(IndexConfigType.LAST_SEND_INDEX,
//                 realtimeList.get(realtimeList.size() - 1).getIndexTime().toString(), indexName.getName());
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
//     private String convertIndex(IndexName indexName, IndexRealtimeDO index) {
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
//         jsonObject.put("symbol", indexName.getName());
//         jsonObject.put("items", items);
//         return jsonObject.toJSONString();
//     }
//
//     /**
//      * 每1秒执行一次,更新最新行情
//      */
//     public void updateDayVol() {
//         // 更新近两天数据
//         updateQuotesByDays(2);
//     }
//
//     /**
//      * 更新行情数据，每秒执行一次
//      */
//     private void updateQuotesByDays(Integer days) {
//         try {
//             SortedMap<String, String> sortedMap = new TreeMap<>();
//             // 填充样本数据
//             for (Map.Entry<String, String> symbolDO : symbolNameMap.entrySet()) {
//                 String symbol = symbolDO.getKey();
//                 if (!symbolDataTimeMap.containsKey(symbol)) {
//                     symbolDataTimeMap.put(symbol, 0L);
//                 }
//                 sortedMap.put(symbolDataTimeMap.getOrDefault(symbol, 0L) + "@" + symbol, symbol);
//             }
//
//             // 对每个样本获取实时数据
//             int count = 0;
//             for (String symbol : sortedMap.values()) {
//                 count++;
//                 if (count > refreshQuotationSize) {
//                     return;
//                 }
//
//                 // 如果更新时间相差较少，则不执行更新，避免频繁访问
//                 if (symbolDataTimeMap.get(symbol) + refreshQuotationInterval > System.currentTimeMillis()) {
//                     continue;
//                 }
//
//                 int seconds = maxTimeInterval.get(QUOTES_BY_DAYS_KEY);
//                 executor.execute(() -> updateQuotesByDaysForSymbol(symbol, days, seconds));
//             }
//         } catch (Exception e) {
//             LOG.error(format("指数计算-更新实时行情、上一天成交额、上一天收盘价格异常"), e);
//         }
//     }
//
//     private boolean updateQuotesByDaysForSymbol(String symbol, int days, int seconds) {
//         // 记录更新时间
//         symbolDataTimeMap.put(symbol, System.currentTimeMillis());
//         if (Text.isNotBlank(blackNames) && blackNames.contains(symbol)) {
//             return true;
//         }
//         try {
//             List<IndexSymbolDataVO> symbolDatas = proIndexHelper.updateQuotes(symbol, days);
//             if (CollectionUtils.isEmpty(symbolDatas)) {
//                 LOG.warn(format("指数计算-更新实时数据数量不正确", kv("symbol", symbol), kv("days", days)));
//                 sendWarnMessageByLimit(QUOTES_BY_DAYS_KEY + symbol, "更新实时数据和前一天交易数量异常", seconds,
//                         "更新实时数据时错误:symbol-" + symbol + ",days-" + days);
//                 return false;
//             }
//             if (symbolDatas.size() > 1) {
//                 symbolDatas.remove(0);
//                 saveDayKlineData(symbolDatas);
//             }
//             updateWarnMessageLimt(QUOTES_BY_DAYS_KEY + symbol, seconds);
//             return true;
//         } catch (Exception e) {
//             LOG.error(format("指数计算-更新实时数据失败", kv("symbol", symbol)), e);
//         }
//         return false;
//     }
//
//     /**
//      * 每年1、4、7、10月0点每分钟执行一次
//      * 执行时检查数据库中是否存在，如果已经存在则不再计算
//      * 执行时检查是否是基期，如果是基期则按基期计算
//      * 如果计算不成功重试，每次等待5秒，超过3后忽略最后一天日成交额计算
//      */
//     public void calQuarterParamData() {
//         for (IndexName indexName : IndexName.values()) {
//             calQuarterParamDataExecutor(indexName);
//         }
//     }
//
//     private void calQuarterParamDataExecutor(IndexName indexName) {
//         try {
//             // 计算季度开始月
//             LocalDateTime localDateTime = LocalDateTime.now();
//             int month = ((localDateTime.getMonth().getValue() - 1) / 3) * 3 + 1;
//             String quarterStartDate = DateUtil.getDateString(localDateTime.withMonth(month).withDayOfMonth(1));
//
//             // 判断是否已经计算过，计算过则不再计算
//             if (proIndexService
//                     .existPeriod(quarterStartDate, IndexPeriodGenerateType.AUTO.getValue(), indexName.getName())
//                     .getData()) {
//                 return;
//             }
//
//             // 获取选取的样本范围
//             List<IndexSymbolInfoVO> symbolInfos = proIndexService.getValidSymbol(indexName.getName()).getData();
//             if (CollectionUtils.isEmpty(symbolInfos)) {
//                 LOG.warn(format("指数计算-未取到本样数据，无法计算月除数"));
//                 return;
//             }
//
//             // 控制只有一个节点执行
//             String key = proIndexHelper.getCacheKey("auto_cal_processing");
//             if (!redisClient.setnx(key, Long.toString(System.currentTimeMillis()))) {
//                 Long time = redisClient.getLong(key);
//                 LOG.info(format("指数计算-自动计算权重时发现有程序正在执行", kv("time", time)));
//                 return;
//             }
//             redisClient.expire(key, 60);
//
//             // 获取自动计算配置
//             IndexCalculateConfigVO calculateConfigVO = proIndexService.getCalculateConfig(indexName.getName())
//                     .getData();
//
//             // 获取最后一次样本数据
//             IndexParamVO param = proIndexService.getLastProIndexPeriod(indexName.getName()).getData();
//
//             IndexParamVO newParam = calSymbolParam(indexName.getName(), quarterStartDate, symbolInfos, param);
//             if (newParam == null) {
//                 LOG.warn(format("指数计算-自动计算权重失败"));
//                 return;
//             }
//             boolean valid = calculateConfigVO.autoCalculate;
//             if (!valid) {
//                 valid = calculateConfigVO.autoNextPeriod;
//             }
//
//             Result<IndexParamVO> indexParamVO = proIndexService
//                     .savePeriod(quarterStartDate, newParam, IndexPeriodGenerateType.AUTO, valid, indexName.getName());
//
//             if (param == null && indexParamVO.isSuccess()) {
//                 long now = Instant.now().getEpochSecond();
//                 IndexPointVO vo = new IndexPointVO(now, now, new BigDecimal(1000));
//                 proIndexService.saveIndexPoint(vo, indexParamVO.getData().id, indexName.getName());
//                 executor.execute(() -> calKlineExecuterMap.get(indexName.getName()).execute(vo));
//             }
//
//         } catch (Exception e) {
//             LOG.error("指数计算-计算上月样本和除数异常", e);
//         }
//     }
//
//     /**
//      * 计算样本权重
//      */
//     private IndexParamVO calSymbolParam(String indexName, String date, List<IndexSymbolInfoVO> symbolInfos,
//             IndexParamVO param)
//             throws InterruptedException {
//         LocalDateTime end = proIndexHelper.getQuarterStartDay(date);
//         LocalDateTime start = end.minusMonths(3);
//
//         Map<String, List<IndexSymbolDayKLineDO>> volMap = new HashMap<>();
//         Date startDate = Date.from(start.atZone(GeneralConstant.PRO_INDEX_ZONE).toInstant());
//         Date endDate = Date.from(end.minusDays(1).atZone(GeneralConstant.PRO_INDEX_ZONE).toInstant());
//
//         // 标志是否需要等待
//         List<IndexSymbolInfoVO> delList = new ArrayList<>();
//         boolean wait = true;
//         for (IndexSymbolInfoVO symbolInfoVO : symbolInfos) {
//             List<IndexSymbolDayKLineDO> dayVolumeList = null;
//
//             // 尝试获取三次
//             for (int i = 0; i < 3; i++) {
//                 dayVolumeList = proIndexService.findSymbolKLine(symbolInfoVO.name, startDate, endDate).getData();
//                 if (CollectionUtils.isEmpty(dayVolumeList) || dayVolumeList.get(0).getDayDate().before(endDate)) {
//                     if (!wait) {
//                         break;
//                     }
//                     Thread.sleep(60000);
//                 } else {
//                     break;
//                 }
//                 wait = false;
//             }
//
//             if (CollectionUtils.isEmpty(dayVolumeList)) {
//                 LOG.warn(format("指数计算-季度更替，触发更新权重，未获取到最后一天数据！", kv("symbol", symbolInfoVO.name),
//                         kv("获取到的数据天数", dayVolumeList.size())));
//                 sendWarnMessage("季度更替变更权重数据不足", "季度更替，触发更新权重，" + symbolInfoVO.name + "未获取到最后一天数据！");
//                 delList.add(symbolInfoVO);
//                 continue;
//             }
//             volMap.put(symbolInfoVO.name, dayVolumeList);
//         }
//         symbolInfos.removeAll(delList);
//
//         Integer selectSymbolCount = proIndexService.getSelectConfig(indexName).getData().symbolCount;
//         IndexParamVO newParam;
//         long startSecond = start.atZone(GeneralConstant.PRO_INDEX_ZONE).toEpochSecond();
//         long endSecond = end.atZone(GeneralConstant.PRO_INDEX_ZONE).toEpochSecond();
//         if (param == null) {
//             newParam = proIndexHelper.calBaseParam(startSecond, endSecond, symbolInfos, volMap, selectSymbolCount);
//         } else {
//             // 计算月样本数据
//             newParam = proIndexHelper.calQuarterParam(startSecond, endSecond, symbolInfos, param.divide,
//                     param.symbols, volMap, selectSymbolCount);
//         }
//         return newParam;
//     }
//
//     /**
//      * 每分钟执行一次，检查是否有新的交易对加入
//      * 如果有新的交易对加入，则将新加入交易对放入交易对列表
//      */
//     public void synchronizeSymbol() {
//
//         for (IndexName indexName : IndexName.values()) {
//             synchronizeAllSymbol(indexName);
//         }
//     }
//
//     private void synchronizeAllSymbol(IndexName indexName) {
//         String temp = httpInterHelper.getForNoProxy(proSymbolUrl);
//         String key = "synchronizeSymbol";
//         int seconds = maxTimeInterval.get(key);
//         if (Text.isBlank(temp)) {
//             LOG.warn(format("指数计算-同步symbol失败", kv("url", proSymbolUrl)));
//             sendWarnMessageByLimit(key, "同步交易对调用接口异常", seconds,
//                     "同步交易对调用接口异常，可能网络原因");
//             return;
//         }
//         try {
//             JSONObject obj = JSON.parseObject(temp);
//             if (obj == null || !obj.getBoolean("success")) {
//                 LOG.warn(format("指数计算-同步symbol失败", kv("url", proSymbolUrl), kv("result", temp)));
//                 sendWarnMessageByLimit(key, "同步交易对调用接口失败", seconds,
//                         "同步交易对调用接口失败，可能服务原因");
//                 return;
//             }
//
//             // 获取现有交易对
//             List<IndexSampleSymbolDO> symbols = proIndexService.findAllSymbol().getData();
//             Map<String, IndexSampleSymbolDO> map = new HashMap<>();
//             symbols.forEach(symbol -> map.put(symbol.getIndexName() + "@" + symbol.getSymbolName(), symbol));
//
//             // 解析获取数据，并保存新交易对
//             JSONArray array = obj.getJSONArray("data");
//             for (int k = 0; k < array.size(); k++) {
//                 JSONObject item = array.getJSONObject(k);
//
//                 // 获取币名和交易对名
//                 String coinName = item.getString("baseCurrency");
//                 String quoteCurrency = item.getString("quoteCurrency");
//                 String symbolName = item.getString("symbolCode");
//                 if (Text.isBlank(coinName) || Text.isBlank(symbolName) || symbolName.length() > 20) {
//                     LOG.warn(format("指数计算-更新交易对获取币名或交易对名错误", kv("coin", coinName), kv("symbol", symbolName)));
//                     sendWarnMessageByLimit(key, "同步交易对调用接口失败", seconds,
//                             "更新交易对获取币名或交易对名错误，name:" + coinName + ",symbol:" + symbolName);
//                     continue;
//                 }
//
//                 // 获取状态
//                 Integer state = IndexSymbolState.getValueByCode(item.getString("state"));
//                 String tradeMarket = item.getString("tradeMarket");
//                 if ("usdt".equals(quoteCurrency.toLowerCase()) && "pro".equals(tradeMarket)) {
//                     saveExshell10Symbol(indexName, map, symbolName, coinName, state, key, seconds);
//                 }
//             }
//             updateWarnMessageLimt(key, seconds);
//         } catch (Exception e) {
//             sendWarnMessageByLimit(key, "同步交易对数据异常", seconds, "同步交易对数据异常");
//             LOG.error(format("指数计算-同步交易对数据失败", kv("url", proSymbolUrl), kv("result", temp)), e);
//         }
//     }
//
//     /**
//      * 保存火币10指数交易对
//      */
//     private void saveExshell10Symbol(IndexName indexName, Map<String, IndexSampleSymbolDO> map, String symbolName,
//             String coinName,
//             Integer state, String key, int seconds) {
//         String name = indexName.getName() + "@" + symbolName;
//         IndexSampleSymbolDO symbolInfo = map.get(name);
//         if (state == null) {
//             sendWarnMessageByLimit(key, "同步交易对调用接口失败", seconds,
//                     "更新交易对获取币名或交易对名错误，state:null");
//             LOG.warn(format("指数计算-更新交易对错误", kv("symbolName", symbolName)));
//             return;
//         }
//
//         // 状态和名称没有变化，不变更
//         if (symbolInfo != null && symbolInfo.getState().equals(state) && coinName
//                 .equals(symbolInfo.getCoinName())) {
//             return;
//         }
//
//         // 如果没有这个交易对则创建
//         Date date = new Date();
//         if (symbolInfo == null) {
//             symbolInfo = new IndexSampleSymbolDO();
//             symbolInfo.setGmtCreated(date);
//             symbolInfo.setSymbolName(symbolName);
//             symbolInfo.setCategoryCode("");
//             symbolInfo.setIndexName(IndexName.EXSHELL10.getName());
//         }
//         symbolInfo.setGmtModified(date);
//         symbolInfo.setCoinName(coinName);
//         symbolInfo.setState(state);
//
//         Result result = proIndexService.saveSymbol(symbolInfo);
//         if (!result.isSuccess()) {
//             sendWarnMessageByLimit(key, "同步交易对存储失败", seconds, "保存交易对失败");
//             LOG.warn(format("指数计算-保存交易对失败", kv("symbol", symbolName)));
//         }
//
//         // 更新symbol
//         if (IndexSymbolState.valid(state)) {
//             symbolNameMap.put(symbolName, coinName);
//         }
//     }
//
//     /**
//      * 发送通知提醒短信
//      */
//     private void sendWarnMessage(String title, String message) {
//         String dateString = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.Z");
//         executor.execute(() -> {
//             String key = null;
//             try {
//                 key = proIndexHelper
//                         .getCacheKey(Base64.getEncoder().encodeToString(title.getBytes("UTF-8")) + "@warn_limit");
//             } catch (UnsupportedEncodingException e) {
//                 LOG.error(format("指数计算-发送提醒短信Base64加密异常"), e);
//             }
//             if (redisClient.incr(key) > maxWarnNum) {
//                 LOG.warn(format("指数计算-同种类型警告每小时超过" + maxWarnNum + "次，不发送", kv("title", title), kv("message", message)));
//                 return;
//             } else {
//                 redisClient.expire(key, maxWarnTimeout);
//             }
//
//             // 发送短信通知
//             for (Map.Entry<String, String> phone : warnPhones.entrySet()) {
//                 try {
//                     HashMap<String, Object> data = new HashMap<>();
//                     data.put("phone", phone.getKey());
//                     data.put("country_code", phone.getValue());
//                     data.put("content", dateString + "," + message);
//                     data.put("business_type", "UC");
//                     data.put("use_type", "指数计算警告");
//                     data.put("content_type", "NOTICE");
//                     HttpInterHelper.SignatureObject obj = httpInterHelper.post(ucUrl + warnPhonePath);
//                     obj.data(data);
//                     obj.withSignature(ucAccessKey, ucSecretKey);
//                     String text = httpInterHelper.call(obj);
//                     JSONObject result = JSON.parseObject(text);
//                     if (!checkMessageResult(result)) {
//                         LOG.warn(format("指数计算-发送警告消息短信失败", kv("result", result), kv("param", JSON.toJSONString(data))));
//                     }
//                 } catch (IOException e) {
//                     LOG.error(format("指数计算-发送警告短信失败", kv("title", title), kv("content", message),
//                             kv("phone", phone.getKey()), kv("contry_code", phone.getValue()),
//                             kv("message", e.getMessage())), e);
//                 }
//             }
//
//             // 发送邮件通知
//             for (String email : warnEmails) {
//                 try {
//                     HashMap<String, Object> data = new HashMap<>();
//                     data.put("email", email);
//                     data.put("title", title);
//                     data.put("content", dateString + "," + message);
//                     data.put("business_type", "UC");
//                     data.put("use_type", "指数计算警告");
//                     data.put("content_type", "NOTICE");
//                     HttpInterHelper.SignatureObject obj = httpInterHelper.post(ucUrl + warnEmailPath);
//                     obj.data(data);
//                     obj.withSignature(ucAccessKey, ucSecretKey);
//                     String text = httpInterHelper.call(obj);
//                     JSONObject result = JSON.parseObject(text);
//                     if (!checkMessageResult(result)) {
//                         LOG.warn(format("指数计算-发送警告邮件消息失败", kv("result", result), kv("param", JSON.toJSONString(data))));
//                     }
//                 } catch (IOException e) {
//                     LOG.error(format("指数计算-发送警告短信失败", kv("title", title), kv("content", message),
//                             kv("email", email), kv("message", e.getMessage())), e);
//                 }
//             }
//         });
//     }
//
//     private void sendWarnMessageByLimit(String key, String title, int seconds, String message) {
//         key = proIndexHelper.getCacheKey(key);
//         if (redisClient.setnx(key, "1")) {
//             redisClient.expire(key, seconds);
//             sendWarnMessage(title, message);
//         }
//     }
//
//     private void updateWarnMessageLimt(String key, int seconds) {
//         key = proIndexHelper.getCacheKey(key);
//         redisClient.setex(key, "1", seconds);
//     }
//
//     /**
//      * 判断调用是否成功
//      */
//     private boolean checkMessageResult(JSONObject obj) {
//         return obj != null && obj.containsKey("success") && obj.getBoolean("success");
//     }
//
//     @Override
//     public void quotationReceive(String symbol, List<IndexSymbolDataVO> symbolDatas) {
//         if (CollectionUtils.isEmpty(symbolDatas)) {
//             LOG.warn(format("指数计算-从ws获取到的数据为空"));
//             return;
//         }
//         // 更新币种实时数据
//         symbolDataTimeMap.put(symbol, System.currentTimeMillis());
//         int seconds = maxTimeInterval.get(QUOTES_BY_DAYS_KEY);
//         updateWarnMessageLimt(QUOTES_BY_DAYS_KEY + symbol, seconds);
//         IndexSymbolDataVO realtimeData = symbolDatas.get(symbolDatas.size() - 1);
//         if (realtimeData.close != null && !realtimeData.close.equals(BigDecimal.ZERO)) {
//             proIndexHelper.updateTodayDataCache(symbol, realtimeData.close, realtimeData.open);
//         }
//
//         if (symbolDatas.size() == 1) {
//             return;
//         }
//         symbolDatas.remove(symbolDatas.size() - 1);
//         saveDayKlineData(symbolDatas);
//     }
//
//     @Override
//     public void subDataReceive(String symbol, IndexSymbolDataVO realtimeData) {
//         symbolDataTimeMap.put(symbol, System.currentTimeMillis());
//         if (realtimeData.close != null && !realtimeData.close.equals(BigDecimal.ZERO)) {
//             proIndexHelper.updateTodayDataCache(symbol, realtimeData.close, realtimeData.open);
//             int seconds = maxTimeInterval.get(QUOTES_BY_DAYS_KEY);
//             updateWarnMessageLimt(QUOTES_BY_DAYS_KEY + symbol, seconds);
//         }
//     }
//
//     @Override
//     public void pongReceive() {
//
//     }
//
//     private void saveDayKlineData(List<IndexSymbolDataVO> symbolDatas) {
//         for (int i = 0; i < symbolDatas.size(); i++) {
//             IndexSymbolDataVO dataVO = symbolDatas.get(i);
//             Date date = new Date(dataVO.time * 1000);
//             BigDecimal value = dataVO.vol;
//             if (value != null && !value.equals(BigDecimal.ZERO)) {
//                 proIndexService.saveSymbolDayKLine(dataVO.name, date, dataVO.vol, dataVO.amount);
//             }
//         }
//     }
// }
