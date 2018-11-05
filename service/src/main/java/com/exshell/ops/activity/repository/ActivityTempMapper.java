package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivitySnapshot;
import com.exshell.ops.activity.model.ActivityTemp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityTempMapper {
    List<ActivityTemp> queryUid(@Param("offset") int offset, @Param("limit") int limit);

    ActivityTemp findByUid(Long userId);
}