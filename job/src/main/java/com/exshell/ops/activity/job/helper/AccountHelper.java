package com.exshell.ops.activity.job.helper;// package com.exshell.ops.activity.job.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.exshell.bitex.commons.cache.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
public class AccountHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountHelper.class);

    // @Resource
    // private HttpInterHelper httpInterHelper;
    @Resource
    private ApiClient apiClient;
    @Resource
    private RedisClient redisClient;

    public List getCoinMarketcap(String symbol) {


        String s = symbol.toLowerCase();
        String detailTrade = apiClient.detail1(symbol);
        JSONObject object1 = JSON.parseObject(detailTrade);
        JSONArray data = object1.getJSONArray("data");
        // Set<String> strings = data.keySet();
        data.forEach(n -> {
            JSONObject jsonObject = (JSONObject) n;
            JSONObject quotesJson = jsonObject.getJSONObject("quotes");
            String symbol1 = jsonObject.getString("symbol");
            JSONObject usdt = quotesJson.getJSONObject(symbol.toUpperCase());
            String sym = symbol1.toLowerCase();
            String price = usdt.getString("price");
            BigDecimal rate = new BigDecimal(price);

            // UsdtExchangeRateType byName = UsdtExchangeRateType.getByName(sym + "usdt");
            // if (byName != null) {
            //     redisClient.set(byName.getCatchKey(), vo);
            // } else {

            redisClient.set(sym + "_exchange_" + s + "_rate@coinmarket", jsonObject);
            // }
            // System.out.println(sym + "<<<<<"+symbol.toLowerCase()+"<<<<<" + JSON.toJSON(redisClient.get(sym + "_exchange_" + symbol.toLowerCase() + "_rate@coinmarket")));
        });


        return null;
    }

}
