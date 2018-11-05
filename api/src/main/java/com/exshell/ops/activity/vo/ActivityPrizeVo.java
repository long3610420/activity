package com.exshell.ops.activity.vo;

import com.exshell.ops.activity.model.ActivityPrize;

import java.util.List;

public class ActivityPrizeVo extends AbstractStateVo{

    private List<ActivityPrize> prizes;

    public List<ActivityPrize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<ActivityPrize> prizes) {
        this.prizes = prizes;
    }
}
