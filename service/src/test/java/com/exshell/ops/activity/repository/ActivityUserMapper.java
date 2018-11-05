package com.exshell.ops.activity.repository;

import com.exshell.ops.activity.model.ActivityUser;

public interface ActivityUserMapper {
    int insert(ActivityUser record);

    int insertSelective(ActivityUser record);
}