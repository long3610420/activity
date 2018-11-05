package com.exshell.ops.activity.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Longs;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class OtcUnfrozenParam {
    @JsonProperty("user-id")
    @ApiModelProperty(
            value = "锁仓用户userId",
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
    private Long userId;
    @JsonProperty("order-id")
    @ApiModelProperty(
            value = "订单id",
            required = true,
            dataType = "String"
    )
    @NotNull(
            message = "REQUIRE_ORDER_ID"
    )
    @DecimalMin(
            value = "1",
            message = "INVALID_ORDER_ID"
    )
    @DecimalMax(
            value = "9223372036854775807",
            message = "INVALID_ORDER_ID"
    )
    private Long orderId;
    @JsonProperty("related-id")
    @ApiModelProperty(
            value = "关联订单id",
            required = true,
            dataType = "String"
    )
    @NotNull(
            message = "INVALID_RELATED_ID"
    )
    @DecimalMin(
            value = "1",
            message = "INVALID_RELATED_ID"
    )
    @DecimalMax(
            value = "9223372036854775807",
            message = "INVALID_RELATED_ID"
    )
    private Long relatedId;
    @JsonProperty("currency")
    @ApiModelProperty(
            value = "币种",
            required = true
    )
    @NotNull(
            message = "INVALID_CURRENCY"
    )
    @Pattern(
            regexp = "^[a-z0-9]{2,10}$",
            message = "INVALID_CURRENCY"
    )
    private String currency;
    @JsonProperty("amount")
    @ApiModelProperty(
            value = "金额",
            required = true,
            dataType = "String"
    )
    @NotNull(
            message = "INVALID_AMOUNT"
    )
    @DecimalMin(
            value = "0.0000000001",
            message = "INVALID_AMOUNT"
    )
    @DecimalMax(
            value = "99999999999999999.9999999999",
            message = "INVALID_AMOUNT"
    )
    private BigDecimal amount;
    @JsonProperty("is-task")
    @ApiModelProperty(
            value = "是否定时任务",
            required = false
    )
    private boolean task;
    @JsonProperty("type")
    @ApiModelProperty(
            value = "类型",
            required = true
    )
    private String type;
    @ApiModelProperty(
            value = "extra",
            required = false
    )
    private String extra = "";

    public OtcUnfrozenParam() {
    }

    public void setUserId(String userId) {
        this.userId = Longs.tryParse(userId);
    }

    public void setOrderId(String orderId) {
        this.orderId = Longs.tryParse(orderId);
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = Longs.tryParse(relatedId);
    }

    public void setAmount(String amount) {
        this.amount = new BigDecimal(amount);
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public Long getRelatedId() {
        return this.relatedId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setExtra(final String extra) {
        this.extra = extra;
    }

    public String getExtra() {
        return this.extra;
    }

    @NotNull
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull String currency) {
        this.currency = currency;
    }

    public boolean isTask() {
        return task;
    }

    public void setTask(boolean task) {
        this.task = task;
    }
}
