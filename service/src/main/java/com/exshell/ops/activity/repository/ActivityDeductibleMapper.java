package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityDeductible;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityDeductibleMapper {
    int insert(ActivityDeductible record);

    int insertSelective(ActivityDeductible record);
}