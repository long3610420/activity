package com.exshell.ops.activity.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.exshell.base.utils.HttpUtil;
import com.exshell.dawn.exception.ApiException;
import com.exshell.gateway.helper.CurrencyHelper;
import com.exshell.ops.activity.constants.ActivityConstants;
import com.exshell.ops.activity.constants.ActivityRedisKeyConstants;
import com.exshell.ops.activity.enums.ActivityErrorEnum;
import com.exshell.ops.activity.model.ActivityFirstCharge;
import com.exshell.ops.activity.model.ActivityFirstChargeStatistics;
import com.exshell.ops.activity.model.ActivityReward;
import com.exshell.ops.activity.model.ActivityUser;
import com.exshell.ops.activity.repository.ActivityFirstChargeMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ActivityFirstChargeService {

    // private static final Logger log = LoggerFactory.getLogger(ActivityFirstChargeService.class);

    @Autowired
    private ActivityFirstChargeMapper activityFirstChargeMapper;
    @Autowired
    ActivityUserService activityUserService;
    @Autowired
    ActivityRewardService activityRewardService;
    // @Resource
    // private RedisClient redisClient;
    @Autowired
    @Qualifier("cacheRedisTemplate")
    private RedisTemplate<String, Object> cacheRedisTemplate;
    @Value("${activity.start-at}")
    private String actStartAt;
    @Autowired
    private CurrencyHelper currencyHelper;



    public Integer save(final Long uid, Long activityId, Long rechargeDate, String currency, String amount) {
        ActivityFirstCharge record = new ActivityFirstCharge();
        record.setUid(uid);
        record.setActivityId(activityId);
        record.setRechargeDate(rechargeDate);
        record.setCurrency(currency);
        record.setAmount(new BigDecimal(amount));

        Integer account = activityFirstChargeMapper.insertSelective(record);

        return account;
    }

    public ActivityFirstCharge findByUid(final Long uid) {
        ActivityFirstCharge account = activityFirstChargeMapper.selectByUid(uid);
        if (account == null) {
            throw new ApiException(ActivityErrorEnum.ACTIVITY_NOT_EXIST_ERROR);
        }

        return account;
    }

    @Transactional
    public Boolean saveFirst(final Long uid, Long activityId, Long rechargeDate, String currency, String amount) {
        log.info("saveFirst start >>>>>>uid: {},activityId:{}, rechargeDate:{},  currency:{},  amount:{}", uid, activityId, rechargeDate, currency, amount);
        // redisClient.incr(ActivityRedisKeyConstants.ACTIVITY_USER_TOTAL);
        DateTime now = DateTime.now();
        long millis = now.getMillis();
        rechargeDate = rechargeDate != null && rechargeDate > 0 ? rechargeDate : millis;

        // Collection<Currency> currencies = currencyHelper
        //         .getReturnableCurrencies(CurrencyPartitionEnum.PRO);
        // ApiResponse<Collection<String>> ok = ApiResponse.ok(Collections2.transform(currencies, Currency::getCode));
        // Collection<String> data1 = ok.getData();
        String currency1 = currency.toLowerCase();
        // AtomicReference<Boolean> boo = new AtomicReference<>(true);
        AtomicReference<Boolean> isRewardable = new AtomicReference<>(false);
        // data1.forEach(n -> {
        //     // log.info(">>>>>{}", n);
        //     if (n.equals(currency1)) {
        //         boo.set(true);
        //     }
        // });
        // if (boo.get()) {
        ActivityUser activityUser = activityUserService.selectByUid(uid);
        if (activityUser != null && activityUser.getIsRecharge() == 0) {
            activityUser.setIsRecharge(1);
            activityUser.setUpdatedAt(millis);

            activityUserService.update(activityUser);
            Integer save = save(uid, activityId, rechargeDate, currency, amount);
            if (save == 0) {
                return false;
            } else {

                // 币种转换 判断大于20USDT
                HttpUtil httpUtil = new HttpUtil();
                String json = "";
                try {
                    // String json = httpUtil.getJSON("", ActivityConstants.getBrokerHost() + "/internal/order/last-price?symbol=" + currency + "usdt");
                    json = httpUtil.getJSON("", ActivityConstants.getBitexHost() + "/general/exchange_rate/exchange_usdt/get?symbol=" + currency1);
                } catch (IOException e) {
                    log.error("ActivityFirstChargeService.saveFirst error {}", e);
                    isRewardable.set(true);
                }
                BigDecimal bigDecimal1 = new BigDecimal(amount);
                BigDecimal bigDecimal2 = new BigDecimal(amount);
                if (StringUtils.isNotEmpty(json)) {
                    JSONObject jsonObject = JSON.parseObject(json);
                    if (jsonObject != null) {
                        String status = jsonObject.getString("code");
                        if (status == null || !status.equals("ok")) {
                            log.error("获取价格失败,数据错误 {},uid: {}, rechargeDate:{},  currency:{},  amount:{}", json, uid, rechargeDate, currency, amount);
                            // return false;
                        }
                        String data = jsonObject.getString("data");
                        if (StringUtils.isBlank(data)) {
                            log.error("获取价格失败,数据错误 {},uid: {}, rechargeDate:{},  currency:{},  amount:{}", json, uid, rechargeDate, currency, amount);
                            // return false;
                        }
                        JSONObject parse = JSON.parseObject(data);
                        if (parse != null) {

                            String string = parse.getString("rate");
                            BigDecimal bigDecimal = new BigDecimal(string);
                            BigDecimal multiply = bigDecimal.multiply(bigDecimal1);
                            if (multiply.subtract(new BigDecimal(18)).doubleValue() > 0) {
                                isRewardable.set(true);
                            }
                        } else {

                        }

                    }
                }
                //单独判断usdt
                BigDecimal subtract = bigDecimal1.subtract(new BigDecimal(20));
                if ("usdt".equals(currency1) && subtract.doubleValue() >= 0) {
                    isRewardable.set(true);
                }
                BigDecimal subtract1 = bigDecimal2.subtract(new BigDecimal(200));
                if ("et".equals(currency1) && subtract1.doubleValue() >= 0) {
                    isRewardable.set(true);
                }

                if (isRewardable.get()) {
                    DateTime dt3 = new DateTime(actStartAt);
                    int days = Days.daysBetween(dt3, now).getDays();
                    // 判断活动截止时间 2018-11-1 00:00:00
                    long millis1 = dt3.getMillis();
                    if (millis1 < millis && millis < 1541001600000L) {
                        Integer integer = 0;
                        try {
                            Object integer1 = cacheRedisTemplate.opsForValue().get(ActivityRedisKeyConstants.ACTIVITY_SEND_ET_TOTAL + days + "-");
                            if (integer1 != null)
                                integer = Integer.parseInt(integer1.toString());
                        } catch (Exception e) {
                            log.error("ActivityFirstChargeService.saveFirst redisClient error {}", e);

                            // integer = activityRewardService.count(now.withMillisOfDay(0).getMillis(), now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).getMillis()) * 1000;
                        }
                        if (integer < 50000000) {
                            try {
                                // 每日最多5000万 放redis
                                cacheRedisTemplate.opsForValue().increment(ActivityRedisKeyConstants.ACTIVITY_SEND_ET_TOTAL + days + "-", 1000L);
                            } catch (Exception e) {
                                log.error("ActivityFirstChargeService.saveFirst redisClient error {}", e);

                            }
                            ActivityReward record = new ActivityReward();
                            record.setAmount(new BigDecimal(1000));
                            record.setCurrency("et");
                            record.setUid(uid);
                            record.setReason("首充充值满20USDT，奖励1000ET");
                            record.setRewardType(0);
                            record.setIsEnabled(0);
                            record.setCreatedAt(millis);
                            record.setUpdatedAt(millis);
                            record.setVersion(1L);
                            activityRewardService.save(record);

                            try {
                                cacheRedisTemplate.opsForValue().increment(ActivityRedisKeyConstants.ACTIVITY_USER_TOTAL, 1);
                            } catch (Exception e) {
                                log.error("ActivityFirstChargeService.saveFirst redisClient error {}", e);
                            }
                        }
                    }
                }
            }
            // }
        }
        return true;
    }

    public List<Map> findActivityFirstChargeStatistics(Long benginTime, Long endTime, String [] currencys) {
        List<String> findCurrencys = Arrays.asList(currencys);
        List<Map> activityFirstChargeStatistics = activityFirstChargeMapper.findFirstChargeStatistics(benginTime, endTime, findCurrencys);
        List<Map> atFirstStatisticsVo = new ArrayList<Map>();
        if(!CollectionUtils.isEmpty(activityFirstChargeStatistics)) {
            Map<String, BigDecimal> symbolMap = new HashMap<String, BigDecimal>();
            Arrays.asList(currencys)
                    .forEach(symbol ->
                            {symbolMap.put(symbol, getUsdtRate(symbol.toString())); });

            for (Map map : activityFirstChargeStatistics) {
                Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                Map entryMap = new HashMap();
                BigDecimal allMoney = new BigDecimal(0);
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value == null) {
                        value = new BigDecimal(0);
                    }
                    if (null == value) value = new BigDecimal(0);
                    entryMap.put(key,  value);
                    if (!key.equals("rechargeNum") && !key.equals("rechargeDate")) {
                        allMoney = allMoney.add(new BigDecimal(value.toString()).multiply(symbolMap.get(key.toString())));
                    }
                }
                entryMap.put("allMoney", allMoney);
                atFirstStatisticsVo.add(entryMap);
            }
        }
        return atFirstStatisticsVo;
    }

    public BigDecimal getUsdtRate(String symbol) {
        //获取USDT汇率
        BigDecimal rate = new BigDecimal(0);
        HttpUtil httpUtil = new HttpUtil();
        String json = "";
        try {
            // String json = httpUtil.getJSON("", ActivityConstants.getBrokerHost() + "/internal/order/last-price?symbol=" + currency + "usdt");
            json = httpUtil.getJSON("", ActivityConstants.getBitexHost() + "/general/exchange_rate/exchange_usdt/get?symbol=" + symbol);
        } catch (IOException e) {
        }
        if (StringUtils.isNotEmpty(json)) {
            JSONObject jsonObject = JSON.parseObject(json);
            if (jsonObject != null) {
                String status = jsonObject.getString("code");
                if (status != null || status.equals("ok")) {
                    String data = jsonObject.getString("data");
                    if (StringUtils.isNotBlank(data)) {
                        JSONObject parse = JSON.parseObject(data);
                        if (parse != null) {
                            String string = parse.getString("rate");
                            rate = new BigDecimal(string);
                        }
                    }
                }
            }
        }
        return  rate;
    }

}
