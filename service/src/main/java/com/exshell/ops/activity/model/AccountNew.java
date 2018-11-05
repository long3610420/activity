package com.exshell.ops.activity.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.exshell.base.entity.AbstractEntity;

@Entity
@Table(name = "t_account")
public class AccountNew extends AbstractEntity {

  private static final long serialVersionUID = -2643355183145367336L;

  @Column(name = "f_user_id")
  private Long userId;

  @Column(name = "f_type")
  private Integer type;

  @Column(name = "f_subtype")
  private String subtype;

  @Column(name = "f_state")
  private Integer state;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getSubtype() {
    return subtype;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }
}
