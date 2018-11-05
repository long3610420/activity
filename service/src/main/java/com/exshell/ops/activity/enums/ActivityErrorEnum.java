package com.exshell.ops.activity.enums;

import com.exshell.base.ErrorCode;
import com.google.common.base.CaseFormat;

public enum ActivityErrorEnum implements ErrorCode {

  // 记录不存在
  ACTIVITY_NOT_EXIST_ERROR,
  // 保存错误
  ACTIVITY_SAVE_ERROR,
  ;


  public final String errCode;

  ActivityErrorEnum() {
    this.errCode = CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_HYPHEN).convert(name());
  }

  @Override
  public String getErrCode() {
    return this.errCode;
  }

}
