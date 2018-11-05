package com.exshell.ops.activity.vo;

import com.exshell.ops.activity.model.ActivityUser;

import java.util.List;

public class ActivityUserTotalVo extends AbstractStateVo {
    private Integer total;

    private List<ActivityUser> users;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<ActivityUser> getActivityUsers() {
        return users;
    }

    public void setActivityUsers(List<ActivityUser> users) {
        this.users = users;
    }
}
