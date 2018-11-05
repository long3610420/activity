package com.exshell.ops.activity.repository;


import com.exshell.ops.activity.model.ActivityUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityUserMapper {
    int insert(ActivityUser record);

    int insertSelective(ActivityUser record);

    ActivityUser selectByPrimaryKey(Long id);

    ActivityUser selectByUid(Long id);

    int update(ActivityUser record);

    Integer count();

    List<ActivityUser> queryRechargeRecord(@Param("minReward") Long minReward, @Param("maxReward")Long maxReward);

    int updateByUid(ActivityUser record);
}