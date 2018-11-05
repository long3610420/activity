package com.exshell.ops.activity.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * 新增用户
 */
@Data
public class AddActivityUserReq {

    @ApiParam(value = "用户id", required = true)
    @NotNull(message = "REQUIRE_USER_ID")
    @DecimalMin(
            value = "1",
            message = "USER_ID"
    )
    @DecimalMax(
            value = "9223372036854775807",
            message = "USER_ID"
    )
    @JsonProperty("user-id")
    private String uid;


    @ApiParam(value = "邀请码", required = false)
    @JsonProperty(value = "inviter-code", required = false)
    private String inviterCode;
    @ApiParam(value = "邀请人", required = false)
    @JsonProperty(value = "inviter-id", required = false)
    private String inviterUid;

}
