// package com.exshell.ops.activity.job.helper;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
// import com.exshell.ops.activity.job.task.CrawlCnyUsdExchangeRate;
// import com.exshell.bitex.commons.cache.RedisClient;
// import com.exshell.bitex.general.model.ExchangeRateDataVO;
// import com.exshell.bitex.general.model.ExchangeRateDetailVO;
// import com.exshell.bitex.general.model.ExchangeRateVO;
// import com.exshell.ops.activity.job.util.DateUtil;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Component;
//
// import javax.annotation.Resource;
// import java.math.BigDecimal;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;
//
// import static com.exshell.bitex.commons.Log.format;
// import static com.exshell.bitex.commons.Log.kv;
//
// @Component
// public class ExchangeRateHelper {
//
//     private static final Logger LOGGER = LoggerFactory.getLogger(CrawlCnyUsdExchangeRate.class);
//
//     // @Resource
//     // private HttpInterHelper httpInterHelper;
//     @Resource
//     private ApiClient apiClient;
//     @Resource
//     private RedisClient redisClient;
//
//     public List getCoinMarketcap(String symbol) {
//
//         ExchangeRateVO vo = new ExchangeRateVO();
//                 List<ExchangeRateDetailVO> list=new ArrayList();
//         try {
//
//             String s = symbol.toLowerCase();
//             String detailTrade = apiClient.detail1(symbol);
//             JSONObject object1 = JSON.parseObject(detailTrade);
//             JSONArray data = object1.getJSONArray("data");
//             // Set<String> strings = data.keySet();
//             data.forEach(n -> {
//                 JSONObject jsonObject = (JSONObject) n;
//                 JSONObject quotesJson = jsonObject.getJSONObject("quotes");
//                 String symbol1 = jsonObject.getString("symbol");
//                 JSONObject usdt = quotesJson.getJSONObject(symbol.toUpperCase());
//                 String sym = symbol1.toLowerCase();
//                 String price = usdt.getString("price");
//                 BigDecimal rate = new BigDecimal(price);
//                 vo.setRate(rate);
//                 Date last_updated = new Date(jsonObject.getLong("last_updated"));
//                 vo.setDataTime(last_updated);
//
//                 vo.setTime(new Date());
//                 // if ("usdt".equals(s)) {
//                 //     ExchangeRateDetailVO rateDetailVO = new ExchangeRateDetailVO();
//                 //     rateDetailVO.setName(sym);
//                 //     rateDetailVO.setRate(rate);
//                 //     rateDetailVO.setDataTime(last_updated);
//                 //     list.add(rateDetailVO);
//                 // }
//
//                 ExchangeRateDetailVO rateDetailVO = new ExchangeRateDetailVO();
//                 rateDetailVO.setName(sym);
//                 rateDetailVO.setRate(rate);
//                 rateDetailVO.setDataTime(last_updated);
//                 list.add(rateDetailVO);
//                 // UsdtExchangeRateType byName = UsdtExchangeRateType.getByName(sym + "usdt");
//                 // if (byName != null) {
//                 //     redisClient.set(byName.getCatchKey(), vo);
//                 // } else {
//
//                 redisClient.set(sym + "_exchange_" + s + "_rate@coinmarket", vo);
//                 // }
//                 // System.out.println(sym + "<<<<<"+symbol.toLowerCase()+"<<<<<" + JSON.toJSON(redisClient.get(sym + "_exchange_" + symbol.toLowerCase() + "_rate@coinmarket")));
//             });
//             // if ("usdt".equals(s)) {
//             //     redisClient.set("all_exchange_usdt@coinmarket", JSON.toJSON(list));
//             // }
//
//             redisClient.set("all_exchange_"+s+"@coinmarket", JSON.toJSON(list));
//         } catch (Exception e) {
//
//             LOGGER.error(" getCoinMarketcap error {}",e);
//         }
//         // Map map = new HashMap();
//         // for (String object : strings) {
//         //     JSONObject string = data.getJSONObject(object);
//         //     String symbol1 = string.getString("symbol");
//         //     JSONObject quotesJson = string.getJSONObject("quotes");
//         //     JSONObject usdt = quotesJson.getJSONObject("USDT");
//         //     String price = usdt.getString("price");
//         //     String sym = symbol1.toLowerCase();
//         //     // map.put(sym + "usdt", price);
//         //
//         //     // System.out.println(sym + "usdt===========" + price);
//         //
//         //     ExchangeRateVO vo = new ExchangeRateVO();
//         //     vo.setRate(new BigDecimal(price));
//         //     vo.setDataTime(new Date(string.getLong("last_updated")));
//         //
//         //     vo.setTime(new Date());
//         //
//         //     // UsdtExchangeRateType byName = UsdtExchangeRateType.getByName(sym + "usdt");
//         //     // if (byName != null) {
//         //     //     redisClient.set(byName.getCatchKey(), vo);
//         //     // } else {
//         //
//         //         redisClient.set(sym + "_exchange_"+symbol.toLowerCase()+"_rate@coinmarket", vo);
//         //     // }
//         //     System.out.println(sym + "<<<<<<<<" + JSON.toJSON(redisClient.get(sym + "_exchange_usdt_rate@coinmarket")));
//         // }
//         return null;
//     }
//
// }
