package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityRewardMapper {
    int insert(ActivityReward record);

    int insertSelective(ActivityReward record);

    ActivityReward selectByPrimaryKey(Long id);

    List<ActivityReward> queryRewards(Long uid);

    Integer count(@Param("start") Long start, @Param("end") Long end);
}