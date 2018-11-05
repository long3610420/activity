package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityPrize;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivityPrizeMapper {
    int insert(ActivityPrize record);

    int insertSelective(ActivityPrize record);

    List<ActivityPrize> queryActivityPrize (ActivityPrize activityPrize);

    int update(ActivityPrize record);

}