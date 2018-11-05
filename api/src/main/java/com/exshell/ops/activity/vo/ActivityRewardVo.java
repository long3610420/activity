package com.exshell.ops.activity.vo;

import com.exshell.ops.activity.model.ActivityReward;

import java.util.List;

public class ActivityRewardVo extends AbstractStateVo {
    private String total;
    private List<ActivityReward> list;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<ActivityReward> getList() {
        return list;
    }

    public void setList(List<ActivityReward> list) {
        this.list = list;
    }
}
