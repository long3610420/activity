package com.exshell.ops.activity.model;

import com.exshell.base.entity.AbstractEntity;

import javax.persistence.Entity;

@Entity
public class ActivityPrize extends AbstractEntity {


    private Long uid;

    private Integer type;

    private Long prizeDate;

    private String phone;

    private Integer state;

    private String beginTime;

    private String endTime;

    private String prizeTime;



    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getPrizeDate() {
        return prizeDate;
    }

    public void setPrizeDate(Long prizeDate) {
        this.prizeDate = prizeDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPrizeTime() {
        return prizeTime;
    }

    public void setPrizeTime(String prizeTime) {
        this.prizeTime = prizeTime;
    }
}