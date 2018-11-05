package com.exshell.ops.activity.api;

import com.exshell.base.AppInfo;
import com.exshell.base.utils.ScheduleUtil;
import com.exshell.dawn.exception.ApiException;
import com.exshell.dawn.rest.ApiResponse;
import com.exshell.dawn.rest.BaseErrorEnum;
import com.exshell.dawn.rest.utils.ParamUtil;
import com.exshell.gateway.GatewayErrorEnum;
import com.exshell.gateway.controller.req.ScheduleHostnameReq;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RestController
@RequestMapping("/internal")
@Validated
public class InternalApiController {
    private static final Logger log = LoggerFactory.getLogger(ActivityApiController.class);

  @Autowired
  private AppInfo appInfo;

  @Autowired
  private ScheduleUtil scheduleUtil;

  @GetMapping(value = "/hostname")
  @ApiOperation(value = "读取系统的hostname")
  public ApiResponse<String> getHostname() {
    return ApiResponse.ok(appInfo.hostname);
  }

  @GetMapping(value = "/githash")
  @ApiOperation(value = "读取系统的githash")
  public ApiResponse<String> getGitHash() {
    return ApiResponse.ok(appInfo.hash);
  }

  @PostMapping(value = "/schedule-hostname")
  @ApiOperation(value = "设置schedule-hostname")
  public ApiResponse<Object> setScheduleHostname(
      @Valid @RequestBody ScheduleHostnameReq scheduleHostnameReq) {
    scheduleUtil.setScheduleHostname(scheduleHostnameReq.getHostname());
    return ApiResponse.ok("");
  }

  @GetMapping("/dw/transaction/aggregate/{user-id}/{end-time}")
  @ApiOperation("聚合指定用户的transactions")
  public ApiResponse<String> aggregateUserTransactions(
     @ApiParam(value = "用户ID", required = true)
     @NotNull(message = "param `user-id` is required")
     @DecimalMin(value = ParamUtil.MIN_DB_LONG, message = "param `user-id` is invalid")
     @DecimalMax(value = ParamUtil.MAX_DB_LONG, message = "param `user-id` is invalid")
     @PathVariable("user-id") String userId,

     @ApiParam(value = "聚合截止时间", required = true)
     @NotNull(message = "param `end-time` is required")
     @DecimalMin(value = ParamUtil.MIN_DB_LONG, message = "param `end-time` is invalid")
     @DecimalMax(value = ParamUtil.MAX_DB_LONG, message = "param `end-time` is invalid")
     @PathVariable(value = "end-time") String endTime,

     @ApiParam(value = "聚合条数", required = true)
     @NotNull(message = "param `count` is required")
     @DecimalMin(value = ParamUtil.MIN_DB_LONG, message = "param `count` is invalid")
     @DecimalMax(value = ParamUtil.MAX_DB_LONG, message = "param `count` is invalid")
     @RequestParam("count") String count
  ) {
    LocalDate endDay = Instant.ofEpochMilli(Long.valueOf(endTime)).atZone(ZoneId.systemDefault()).toLocalDate();

    try {
//      subaccountTransactionsAggregationService.setAggregateMinCount(Long.valueOf(count));
//      subaccountTransactionsAggregationService.aggregateUserTransactions(Long.valueOf(userId), endDay);
    } catch (DuplicateKeyException e) {
      throw new ApiException(GatewayErrorEnum.TRANSACTION_AGGREGATE_DUPLICATE);
    } catch (Throwable e) {
      log.warn("Aggregate error: uid={}", userId, e);
      throw new ApiException(BaseErrorEnum.BASE_SYSTEM_ERROR);
    }

    return ApiResponse.ok(endDay.toString());
  }
}
