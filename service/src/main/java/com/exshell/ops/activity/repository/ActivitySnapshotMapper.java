package com.exshell.ops.activity.repository;

import com.exshell.base.entity.AbstractEntity;
import com.exshell.ops.activity.model.ActivitySnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivitySnapshotMapper   {
    int insert(ActivitySnapshot record);

    int insertSelective(ActivitySnapshot record);
}