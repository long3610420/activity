package com.exshell.ops.activity.model;

import com.exshell.base.entity.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "activity_user")
public class ActivityUser extends AbstractEntity {

    private static final long serialVersionUID = -2643355183145367336L;

    @Column(name = "uid")
    private Long uid;

    @Column(name = "code")
    private String code;

    @Column(name = "inviter_uid")
    private Long inviterUid;

    @Column(name = "inviter_code")
    private String inviterCode;

    @Column(name = "is_recharge")
    private Integer isRecharge;

    @Column(name = "is_reminded")
    private Integer isReminded;

    @Column(name = "phone_num")
    private String phoneNum;

    @Column(name = "reward")
    private BigDecimal reward;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getInviterUid() {
        return inviterUid;
    }

    public void setInviterUid(Long inviterUid) {
        this.inviterUid = inviterUid;
    }

    public String getInviterCode() {
        return inviterCode;
    }

    public void setInviterCode(String inviterCode) {
        this.inviterCode = inviterCode;
    }

    public Integer getIsRecharge() {
        return isRecharge;
    }

    public void setIsRecharge(Integer isRecharge) {
        this.isRecharge = isRecharge;
    }

    public Integer getIsReminded() {
        return isReminded;
    }

    public void setIsReminded(Integer isReminded) {
        this.isReminded = isReminded;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }
}
