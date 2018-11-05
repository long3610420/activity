package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityConfigMapper {
    int insert(ActivityConfig record);

    int insertSelective(ActivityConfig record);
}