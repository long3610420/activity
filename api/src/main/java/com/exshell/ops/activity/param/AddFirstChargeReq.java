package com.exshell.ops.activity.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * 首充入参
 */
@Data
public class AddFirstChargeReq {
    @ApiParam(value = "用户id", required = true)
    @NotNull(message = "REQUIRE_USER_ID")
    @DecimalMin(
            value = "1",
            message = "INVALID_USER_ID"
    )
    @DecimalMax(
            value = "9223372036854775807",
            message = "INVALID_USER_ID"
    )
    @JsonProperty("user-id")
    private String uid;

    @ApiParam(value = "活动id", required = false)
    @JsonProperty(value = "activity-id", required = false)
    private Long activityId;

    @ApiParam(value = "充值时间", required = false)
    @JsonProperty(value = "recharge-date", required = false)
    private Long rechargeDate;

    @ApiParam(value = "充值币种", required = true)
    @NotNull(message = "REQUIRE_CURRENCY")
    @JsonProperty("currency")
    private String currency;

    @ApiParam(value = "充值数量", required = true)
    @NotNull(message = "REQUIRE_AMOUNT")
    @JsonProperty("amount")
    private String amount;
}
