package com.exshell.ops.activity.model;

import com.exshell.base.entity.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "activity_temp")
public class ActivityTemp extends AbstractEntity {


    @Column(name = "f_user_id")
    private Long userId;
    private Long accountId;
    private String currency;
    private int accountType;
    private BigDecimal balance;
    private BigDecimal suspense;
    private BigDecimal clearing;

    public Long getUid() {
        return userId;
    }

    public void setUid(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getSuspense() {
        return suspense;
    }

    public void setSuspense(BigDecimal suspense) {
        this.suspense = suspense;
    }

    public BigDecimal getClearing() {
        return clearing;
    }

    public void setClearing(BigDecimal clearing) {
        this.clearing = clearing;
    }
}
