package com.exshell.ops.activity.enums;

import com.exshell.dawn.exception.ApiException;
import com.exshell.dawn.rest.BaseErrorEnum;

public enum ActivityCurrencyType {


    ET(1, "ET"),
    USDT(2, "USDT"),

    ;

    private Integer code;
    private String value;

    ActivityCurrencyType(Integer c, String v) {
        this.code = c;
        this.value = v;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static ActivityCurrencyType find(String value) {
        for (ActivityCurrencyType e : ActivityCurrencyType.values()) {
            if (e.getValue().equals(value))
                return e;
        }
        throw new ApiException(BaseErrorEnum.BASE_ARGUMENT_UNSUPPORTED, value);
    }

    public static ActivityCurrencyType find(Integer code) {
        for (ActivityCurrencyType e : ActivityCurrencyType.values()) {
            if (e.code.equals(code))
                return e;
        }
        throw new ApiException(BaseErrorEnum.BASE_ARGUMENT_UNSUPPORTED, code);
    }

    public boolean isEt() {
        return ET.equals(this);
    }

    public boolean isUsdt() {
        return USDT.equals(this);
    }


}
