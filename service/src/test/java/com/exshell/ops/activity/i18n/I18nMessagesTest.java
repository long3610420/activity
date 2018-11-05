package com.exshell.ops.activity.i18n;

import com.exshell.ops.activity.enums.AccountErrorEnum;
import com.exshell.base.ErrorCode;
import java.util.Arrays;
import java.util.Locale;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

public class I18nMessagesTest {

  private AccountErrorEnum[] errorEnums = AccountErrorEnum.values();

  @Test
  public void testI18nMessages() {
    LocaleContextHolder.setLocale(Locale.CHINESE);
    Arrays.asList(errorEnums).forEach(ErrorCode::getErrMsg);
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    Arrays.asList(errorEnums).forEach(ErrorCode::getErrMsg);
  }

}
