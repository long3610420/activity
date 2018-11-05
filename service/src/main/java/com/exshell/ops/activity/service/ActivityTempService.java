package com.exshell.ops.activity.service;

import com.exshell.ops.activity.model.ActivityTemp;
import com.exshell.ops.activity.repository.ActivityTempMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ActivityTempService {


    @Autowired
    private ActivityTempMapper activityTempMapper;

    public List<ActivityTemp> query(int offset, int limit) {

        return activityTempMapper.queryUid(offset, limit);
    }

    public ActivityTemp findByUserId(Long uid) {

        return activityTempMapper.findByUid(uid);
    }
}
