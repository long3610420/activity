//package com.exshell.ops.activity.api;
//
//
//import com.exshell.base.utils.MathUtil;
//import com.exshell.clearing.utils.JsonUtil;
//import com.exshell.common.service.CurrencyConfiguration;
//import com.exshell.dawn.rest.ApiResponse;
//import com.exshell.dw.service.AccountTransferProcessor;
//import com.exshell.gateway.context.GatewayContext;
//import com.exshell.ops.activity.param.OtcTradeTransferNewParam;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Pattern;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//
//@Slf4j
//@RestController
//@RequestMapping("/v1/otc")
//@Validated
//public class OtcPublicApiController {
//
//  @Autowired
//  private AccountTransferProcessor accountTransferProcessor;
//
////  @Autowired
////  private AccountOtcService accountOtcService;
//
//
//  @ApiOperation("OTC不同用户转账")
//  @PostMapping("/transfer/user")
//  public ApiResponse<Void> transferUser(@Valid @RequestBody OtcTradeTransferNewParam param) {
//    log.warn("POST /otc/transfer/user param:{}", JsonUtil.writeValue(param));
//    String currency = param.getCurrency();
//    BigDecimal amount = new BigDecimal(param.getAmount());
//
//    BigDecimal fee = BigDecimal.ZERO;
//    if (param.getFee() != null) {
//      fee = new BigDecimal(param.getFee());
//    }
//
//    Long fromUserId = Long.valueOf(param.getFromUserId());
//    Long toUserId = Long.valueOf(param.getToUserId());
//    Long transactionId = Long.valueOf(param.getTransactionId());
//    Long orderId = Long.valueOf(param.getOrderId());
//
////    boolean result = accountNewService.checkTransferAccountAction(orderId, fromUserId, amount);
////    if (!result) {
////      throw new ApiException(ValidationErrorEnum.INVALID_ORDER_ID);
////    }
////
////    financeHistoryService.tradeTransfer(fromUserId, toUserId, currency, amount, fee, transactionId);
//    return ApiResponse.ok(null);
//  }
//}
