package com.exshell.ops.activity.vo;

import com.exshell.ops.activity.model.ActivityRewardLock;

import java.math.BigDecimal;
import java.util.List;

public class ActivityRewardLockVo extends AbstractStateVo {

    private String total;

    /**锁仓列表*/
    private List<ActivityRewardLock> rewardLocks;

    /**锁仓中*/
    private BigDecimal lock;

    /**已解锁*/
    private BigDecimal unlock;

    /**是否中奖*/
    private Integer isPrize;
    /**
     * 中奖类型
     */
    private Integer type;
    /**
     * 中奖时间
     */
    private String time;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<ActivityRewardLock> getRewardLocks() {
        return rewardLocks;
    }

    public void setRewardLocks(List<ActivityRewardLock> rewardLocks) {
        this.rewardLocks = rewardLocks;
    }

    public BigDecimal getLock() {
        return lock;
    }

    public void setLock(BigDecimal lock) {
        this.lock = lock;
    }

    public BigDecimal getUnlock() {
        return unlock;
    }

    public void setUnlock(BigDecimal unlock) {
        this.unlock = unlock;
    }

    public Integer getIsPrize() {
        return isPrize;
    }

    public void setIsPrize(Integer isPrize) {
        this.isPrize = isPrize;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
