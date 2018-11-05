package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityRewardLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ActivityRewardLockMapper {
    int insert(ActivityRewardLock record);

    int insertSelective(ActivityRewardLock record);

    List<ActivityRewardLock> queryActivityReward(ActivityRewardLock ActivityRewardLock);

    BigDecimal getUserSumAmountByState(@Param("uid") Long uid, @Param("state") Integer state, @Param("currency") String currency);

    int update(ActivityRewardLock record);

    int updateRewardLockState(ActivityRewardLock record);

    int updateRewardLockStateSuccess(ActivityRewardLock record);
}