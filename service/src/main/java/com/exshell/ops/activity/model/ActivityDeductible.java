package com.exshell.ops.activity.model;

import com.exshell.base.entity.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "activity_deductible")
public class ActivityDeductible extends AbstractEntity {

    private Long uid;

    private Long fee;

    private String feeCurrency;

    private Long feeAmount;

    private Long feeDate;

    private Long orderId;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency == null ? null : feeCurrency.trim();
    }

    public Long getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(Long feeAmount) {
        this.feeAmount = feeAmount;
    }

    public Long getFeeDate() {
        return feeDate;
    }

    public void setFeeDate(Long feeDate) {
        this.feeDate = feeDate;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

}