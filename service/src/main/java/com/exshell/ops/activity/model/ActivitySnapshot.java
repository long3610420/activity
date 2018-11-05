package com.exshell.ops.activity.model;

import com.exshell.base.entity.AbstractEntity;

import javax.persistence.Entity;
import java.math.BigDecimal;
@Entity
public class ActivitySnapshot  extends AbstractEntity {


    private Long uid;

    private Byte frequency;

    private Long rechargeDate;

    private BigDecimal amount;

    private BigDecimal reward;


    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Byte getFrequency() {
        return frequency;
    }

    public void setFrequency(Byte frequency) {
        this.frequency = frequency;
    }

    public Long getRechargeDate() {
        return rechargeDate;
    }

    public void setRechargeDate(Long rechargeDate) {
        this.rechargeDate = rechargeDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }


}