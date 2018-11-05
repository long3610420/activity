package com.exshell.ops.activity.service;

import com.exshell.dawn.exception.ApiException;
import com.exshell.ops.activity.constants.ActivityRedisKeyConstants;
import com.exshell.ops.activity.enums.AccountErrorEnum;
import com.exshell.ops.activity.enums.ActivityErrorEnum;
import com.exshell.ops.activity.model.ActivityUser;
import com.exshell.ops.activity.repository.ActivityUserMapper;
import com.exshell.ops.activity.utils.ActivityCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class ActivityUserService {
    @Autowired
    @Qualifier("cacheRedisTemplate")
    private RedisTemplate<String, Object> cacheRedisTemplate;

    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private ActivityRewardService activityRewardService;

    public ActivityUser save(final Long uid, String inviterCode, String code, Long inviterUid, String phoneNum) {
        ActivityUser activityUser = new ActivityUser();
        activityUser.setCode(code);
        activityUser.setInviterCode(inviterCode);
        activityUser.setIsRecharge(0);
        activityUser.setIsReminded(0);
        activityUser.setPhoneNum(phoneNum);
        activityUser.setUid(uid / 10);
        activityUser.setInviterUid(inviterUid);
        activityUser.setVersion(1L);
        activityUser.setCreatedAt(DateTime.now().getMillis());
        activityUser.setUpdatedAt(DateTime.now().getMillis());
        Integer account = activityUserMapper.insertSelective(activityUser);
        if (account == null || account <= 0) {
            throw new ApiException(ActivityErrorEnum.ACTIVITY_SAVE_ERROR);
        }

        return activityUser;
    }


    public ActivityUser save(final Long uid, BigDecimal reward) {
        ActivityUser activityUser = new ActivityUser();
        activityUser.setReward(reward);
        activityUser.setIsRecharge(0);
        activityUser.setIsReminded(0);
        activityUser.setUid(uid);
        activityUser.setVersion(1L);
        activityUser.setCreatedAt(DateTime.now().getMillis());
        activityUser.setUpdatedAt(DateTime.now().getMillis());
        Integer account = activityUserMapper.insertSelective(activityUser);
        if (account == null || account <= 0) {
            throw new ApiException(ActivityErrorEnum.ACTIVITY_SAVE_ERROR);
        }

        return activityUser;
    }

    public ActivityUser findActivityUser(final Long id) {
        ActivityUser account = activityUserMapper.selectByPrimaryKey(id);
        if (account == null) {
            throw new ApiException(AccountErrorEnum.ACCOUNT_GET_ACCOUNTS_INEXISTENT_ERROR);
        }

        return account;
    }

    public Integer queryChargeTotal() {
        Integer integer = 0;

        try {
            // Object valueOperations = redisTemplate.opsForValue().get(ActivityRedisKeyConstants.ACTIVITY_USER_TOTAL);
            // System.out.println("valueOperations = " + valueOperations);

            Object integer1 = cacheRedisTemplate.opsForValue().get(ActivityRedisKeyConstants.ACTIVITY_USER_TOTAL);
            if (integer1 != null)
                integer = Integer.parseInt(integer1.toString());
        } catch (Exception e) {
            // integer = activityRewardService.count(null, null);

            log.error("queryChargeTotal error {}", e);
        }
        return integer;
    }

    public Long incrChargeTotal(Long num) {

        return cacheRedisTemplate.opsForValue().increment(ActivityRedisKeyConstants.ACTIVITY_USER_TOTAL, num);
    }

    public ActivityUser saveActUser(final Long uid, String inviterCode, Long inviterUid) {
        String code = ActivityCodeUtils.idToCode(uid);
        // Long inviterUid = 0L;
        //TODO 手机号是否需要存
        String phoneNum = "";
        // if (StringUtils.isNotEmpty(inviterCode)) {
        //     inviterUid = ActivityCodeUtils.codeToId(inviterCode);
        //      if (inviterUid<0){
        //          inviterUid=0L;
        //      }
        // }

        return save(uid, inviterCode, code, inviterUid, phoneNum);
    }

    public Integer update(ActivityUser activityUser) {
        return activityUserMapper.update(activityUser);
    }

    public ActivityUser selectByUid(final Long uid) {
        return activityUserMapper.selectByUid(uid);
    }

    public List<ActivityUser> queryRechargeRecord(Long minReward, Long maxReward) {return activityUserMapper.queryRechargeRecord(minReward, maxReward);}

    public int updateByUid(ActivityUser user) {
        return activityUserMapper.updateByUid(user);
    }
}
