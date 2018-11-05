package com.exshell.ops.activity.enums;

import com.exshell.dawn.exception.ApiException;
import com.exshell.dawn.rest.BaseErrorEnum;

public enum AccountType {

  /**
   * 系统账户
   */
  SYSTEM(1, "system"),

  /**
   * 现货账户
   */
  SPOT(2, "spot"),

  /**
   * 杠杆账户
   */
  MARGIN(3, "margin"),

  OTC(4, "otc"),

  POINT(5, "point"),

  MINEPOOL(6, "minepool"),

  ETF(7, "etf"),

  ;

  private Integer code;
  private String value;

  AccountType(Integer c, String v) {
    this.code = c;
    this.value = v;
  }

  public Integer getCode() {
    return code;
  }

  public String getValue() {
    return value;
  }

  public static AccountType find(String value) {
    for (AccountType e : AccountType.values()) {
      if (e.getValue().equals(value))
        return e;
    }
    throw new ApiException(BaseErrorEnum.BASE_ARGUMENT_UNSUPPORTED, value);
  }

  public static AccountType find(Integer code) {
    for (AccountType e : AccountType.values()) {
      if (e.code.equals(code))
        return e;
    }
    throw new ApiException(BaseErrorEnum.BASE_ARGUMENT_UNSUPPORTED, code);
  }

  public boolean isSystem() {
    return SYSTEM.equals(this);
  }

  public boolean isSpot() {
    return SPOT.equals(this);
  }

  public boolean isMargin() {
    return MARGIN.equals(this);
  }

}
