package com.exshell.ops.activity.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class OtcQueryParam {
    @JsonProperty("user-id")
    @ApiModelProperty(
            value = "用户userId",
            required = true,
            dataType = "String"
    )
    @NotNull(
            message = "INVALID_USER_ID"
    )
    @DecimalMin(
            value = "1",
            message = "INVALID_USER_ID"
    )
    @DecimalMax(
            value = "9223372036854775807",
            message = "INVALID_USER_ID"
    )
    private String userId;
    @JsonProperty("transaction-id")
    @ApiModelProperty(
            value = "流水id",
            required = true,
            dataType = "String"
    )
    @NotNull(
            message = "REQUIRE_TRANSACTION_ID"
    )
    @DecimalMin(
            value = "1",
            message = "INVALID_TRANSACTION_ID"
    )
    @DecimalMax(
            value = "9223372036854775807",
            message = "INVALID_TRANSACTION_ID"
    )
    private String transactionId;

    @JsonProperty("action-type")
    @ApiModelProperty(
            value = "操作类型",
            required = true
    )
    private String actionType;

    @NotNull
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(@NotNull String transactionId) {
        this.transactionId = transactionId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    @NotNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NotNull String userId) {
        this.userId = userId;
    }
}
