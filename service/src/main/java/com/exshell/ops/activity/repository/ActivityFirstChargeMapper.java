package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityFirstCharge;
import com.exshell.ops.activity.model.ActivityFirstChargeStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityFirstChargeMapper {
    int insert(ActivityFirstCharge record);

    int insertSelective(ActivityFirstCharge record);

    ActivityFirstCharge selectByPrimaryKey(Long id);
    ActivityFirstCharge selectByUid(Long uid);
    List<Map> findFirstChargeStatistics(@Param("beginTime") Long starTime, @Param("endTime")Long endTime, @Param("crrencys")List<String> crrencys);

}