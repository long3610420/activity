package com.exshell.ops.activity.service;

import com.exshell.ops.activity.model.ActivityPrize;
import com.exshell.ops.activity.model.ActivityReward;
import com.exshell.ops.activity.model.ActivityRewardLock;
import com.exshell.ops.activity.repository.ActivityRewardLockMapper;
import com.exshell.ops.activity.repository.ActivityRewardMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ActivityRewardService {


    @Autowired
    private ActivityRewardMapper activityRewardMapper;

    @Autowired
    private ActivityRewardLockMapper activityRewardLock;
    @Autowired
    private ActivityPrizeService activityPrizeService;

    public Integer save(ActivityReward record) {
        return activityRewardMapper.insertSelective(record);

    }

    public Integer saveFirst(Long uid) {
        DateTime now = DateTime.now();
        long millis = now.getMillis();
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
        return activityRewardMapper.insertSelective(record);

    }

    public List<ActivityReward> searchRewards(Long uid) {
        return activityRewardMapper.queryRewards(uid);
    }

    public Integer count(Long start, Long end) {
        return activityRewardMapper.count(start, end);
    }

    public Map queryUserlockAssets(Long userId, String currency) {
        Map activityRewardLockVo = new HashMap();

        List<ActivityRewardLock> activityRewardLocks = queryUserlockByUserId(userId, currency);
        if (CollectionUtils.isEmpty(activityRewardLocks)) {
            activityRewardLocks = new ArrayList<>();
        }

        //查询用户已解锁数量
        BigDecimal unLock = activityRewardLock.getUserSumAmountByState(userId, 2, currency);
        if (unLock == null) {
            unLock = new BigDecimal(0);
        }

        //查询用户锁仓数量
        BigDecimal lock = activityRewardLock.getUserSumAmountByState(userId, 1, currency);
        if (lock == null) {
            lock = new BigDecimal(0);
        }

        //查询该用户是否中奖
        ActivityPrize prize = new ActivityPrize();
        prize.setUid(userId);
        // prize.setState(1);
        List<ActivityPrize> prizes = activityPrizeService.queryActivityPrize(prize);
        activityRewardLockVo.put("isPrize", 0);
        if (!CollectionUtils.isEmpty(prizes)) {
            activityRewardLockVo.put("isPrize", 1);
            activityRewardLockVo.put("type", prizes.get(0).getType());
            activityRewardLockVo.put("time", prizes.get(0).getPrizeTime());
        }
        activityRewardLockVo.put("activityRewardLocks", activityRewardLocks);
        activityRewardLockVo.put("unLock", unLock);
        activityRewardLockVo.put("lock", lock);
        return activityRewardLockVo;
    }


    public List<ActivityRewardLock> queryUserlockByUserId(Long userId, String currency) {

        //查询用户锁仓列表
        ActivityRewardLock findParam = new ActivityRewardLock();
        findParam.setUid(userId);
        if (StringUtils.isNotEmpty(currency)) {
            findParam.setCurrency(currency.toUpperCase());
        }
        return activityRewardLock.queryActivityReward(findParam);
    }

    public Boolean saveRewardLock(Long userId, String amount) {
        ActivityRewardLock record = new ActivityRewardLock();
        BigDecimal divide = new BigDecimal(amount).divide(new BigDecimal("12"), 18, BigDecimal.ROUND_HALF_UP);
        record.setAmount(divide);
        record.setUid(userId);
        record.setCurrency("ET");
        // DateTime dateTime = new DateTime(1541048400000L);//2018.11.1 13:00:00
        DateTime dateTime = new DateTime(1541055600000L);//2018.11.1 15:00:00
        record.setStartDate(dateTime.getMillis());
        record.setState(1);
        record.setVersion(1L);
        record.setCreatedAt(new DateTime().getMillis());
        record.setUpdatedAt(new DateTime().getMillis());
        for (int i = 1; i < 13; i++) {
            record.setEndDate(dateTime.plusMonths(i).getMillis());
            activityRewardLock.insert(record);
        }
        return true;
    }

    public Boolean updateRewardLock(Long userId, String amount) {
        ActivityRewardLock record = new ActivityRewardLock();
        BigDecimal divide = new BigDecimal(amount).divide(new BigDecimal("12"), 18, BigDecimal.ROUND_HALF_UP);
        record.setAmount(divide);
        record.setUid(userId);
        record.setUpdatedAt(new DateTime().getMillis());
        // record.setCurrency("ET");
        // DateTime dateTime = new DateTime(1541001600000L);//2018.11.1
        // record.setStartDate(dateTime.getMillis());
        // record.setState(1);

        // record.setEndDate(dateTime.plusMonths(i).getMillis());
        activityRewardLock.update(record);

        return true;
    }


    public Boolean updateRewardLockState(Long userId, Integer state) {
        ActivityRewardLock record = new ActivityRewardLock();
        // BigDecimal divide = new BigDecimal(amount).divide(new BigDecimal("12"), 18, BigDecimal.ROUND_HALF_UP);
        // record.setAmount(divide);
        record.setUid(userId);
        record.setUpdatedAt(new DateTime().getMillis());
        record.setState(state);
        // record.setCurrency("ET");
        // DateTime dateTime = new DateTime(1541001600000L);//2018.11.1
        // record.setStartDate(dateTime.getMillis());
        // record.setState(1);

        // record.setEndDate(dateTime.plusMonths(i).getMillis());
        activityRewardLock.updateRewardLockState(record);

        return true;
    }

    public Boolean saveRewardLock(List<ActivityRewardLock> list) {
        if (list != null && list.size() > 0) {

            for (ActivityRewardLock rewardLock : list) {
                rewardLock.setCurrency(rewardLock.getCurrency().toUpperCase());
                rewardLock.setState(1);
                rewardLock.setVersion(1L);
                rewardLock.setCreatedAt(new DateTime().getMillis());
                rewardLock.setUpdatedAt(new DateTime().getMillis());
                rewardLock.setIsCheck(1);
                activityRewardLock.insert(rewardLock);
            }
        }
        return true;
    }


    public Boolean updateRewardLockStateSuccess(Long userId, Integer state) {
        ActivityRewardLock record = new ActivityRewardLock();
        // BigDecimal divide = new BigDecimal(amount).divide(new BigDecimal("12"), 18, BigDecimal.ROUND_HALF_UP);
        // record.setAmount(divide);
        record.setUid(userId);
        long millis = new DateTime().getMillis();
        record.setUpdatedAt(millis);
        record.setState(state);
        record.setStartDate(millis);
        activityRewardLock.updateRewardLockStateSuccess(record);

        return true;
    }
}
