package com.exshell.ops.activity.model;

import com.exshell.base.entity.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "activity_first_charge")
public class ActivityFirstCharge extends AbstractEntity {

    private Long uid;

    private Long activityId;

    private Long rechargeDate;

    private String currency;

    private BigDecimal amount;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getRechargeDate() {
        return rechargeDate;
    }

    public void setRechargeDate(Long rechargeDate) {
        this.rechargeDate = rechargeDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}