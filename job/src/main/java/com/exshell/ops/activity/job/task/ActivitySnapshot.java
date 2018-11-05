package com.exshell.ops.activity.job.task;

import com.exshell.bit.ops.api.dubbo.service.pro.IProUserService;
import com.exshell.bitex.commons.cache.RedisClient;
import com.exshell.ops.activity.job.helper.TradeApiClient;
import com.exshell.ops.activity.model.ActivityTemp;
import com.exshell.ops.activity.service.ActivityTempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.List;

/**
 * 更新活动快照
 */
public class ActivitySnapshot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivitySnapshot.class);

    @Resource
    private RedisClient redisClient;
    @Resource
    private ActivityTempService activityTempService;
    /**
     * 根据userId查询用户的资产信息
     */
    IProUserService userAssetInfoRes;// queryUserAssetInfo(Integer userId);

    @Value("${symol_exchange_rate_huobi}")
    private String symbol;
    @Resource
    private  TradeApiClient tradeApiClient;
    public void execute() {
        int offset = 0;
        int limit = 100;
        int size = 0;
        do {
            List<ActivityTemp> query = activityTempService.query(offset, limit);
            size = query.size();
            for (ActivityTemp activityTemp : query) {

                ActivityTemp byUserId = activityTempService.findByUserId(activityTemp.getUid());
                String s = "" + byUserId.getUid();

                String accountId = tradeApiClient.getAccountId();
                // tradeApiClient.getBalance();
            }
        }
        while (size > 0);


    }
}
