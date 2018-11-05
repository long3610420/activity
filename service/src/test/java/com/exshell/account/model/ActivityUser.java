package com.exshell.account.model;

public class ActivityUser {
    private Long id;

    private Long uid;

    private String code;

    private Long inviterUid;

    private String inviterCode;

    private Byte isRecharge;

    private Byte isReminded;

    private String phoneNum;

    private Long version;

    private Long createdAt;

    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        this.code = code == null ? null : code.trim();
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
        this.inviterCode = inviterCode == null ? null : inviterCode.trim();
    }

    public Byte getIsRecharge() {
        return isRecharge;
    }

    public void setIsRecharge(Byte isRecharge) {
        this.isRecharge = isRecharge;
    }

    public Byte getIsReminded() {
        return isReminded;
    }

    public void setIsReminded(Byte isReminded) {
        this.isReminded = isReminded;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum == null ? null : phoneNum.trim();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}