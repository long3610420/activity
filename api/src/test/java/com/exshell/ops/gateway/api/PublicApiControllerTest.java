// package com.exshell.ops.gateway.api;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// import com.exshell.base.utils.MathUtil;
// import com.exshell.gateway.GatewayErrorEnum;
// import com.exshell.usercenter.UserCenterClient;
// import java.util.Arrays;
// import java.util.Random;
// import org.junit.Test;
//
// public class PublicApiControllerTest {
//
//   UserCenterClient userCenterClient = new UserCenterClient();
//
//   @Test
//   public void testGetErrorEnumByStatusCode() {
//     assertStatusCodeGetRightErrorEnum(10015, GatewayErrorEnum.SECURITY_USER_LOCKED);
//     assertStatusCodeGetRightErrorEnum(11001, GatewayErrorEnum.SECURITY_REQUIRE_ASSETS_PASSWORD);
//     assertStatusCodeGetRightErrorEnum(10032, GatewayErrorEnum.PHONE_AUTH_CODE_ERROR);
//     assertStatusCodeGetRightErrorEnum(10036, GatewayErrorEnum.AUTH_CODE_EXPIRE);
//     assertStatusCodeGetRightErrorEnum(10044, GatewayErrorEnum.SECURITY_ASSET_PWD_ERROR);
//     assertStatusCodeGetRightErrorEnum(10401, GatewayErrorEnum.SECURITY_ASSET_PWD_ERROR_1);
//     assertStatusCodeGetRightErrorEnum(10402, GatewayErrorEnum.SECURITY_ASSET_PWD_ERROR_2);
//     assertStatusCodeGetRightErrorEnum(10403, GatewayErrorEnum.SECURITY_ASSET_PWD_ERROR_3);
//     assertStatusCodeGetRightErrorEnum(10404, GatewayErrorEnum.SECURITY_ASSET_PWD_ERROR_4);
//     assertStatusCodeGetRightErrorEnum(10405, GatewayErrorEnum.SECURITY_ASSET_PWD_ERROR_5);
//     assertStatusCodeGetRightErrorEnum(10049, GatewayErrorEnum.ASSET_PWD_UNABLE);
//     assertStatusCodeGetRightErrorEnum(10101, GatewayErrorEnum.ASSET_PWD_FREEZE);
//
//     Arrays.asList(10083, 10084, 10085, 10086, 10087, 10088).forEach(statusCode -> {
//       assertStatusCodeGetRightErrorEnum(statusCode, GatewayErrorEnum.GA_FAIL);
//     });
//
//     assertStatusCodeGetRightErrorEnum(12000 + new Random().nextInt(1000), GatewayErrorEnum.UNKNOWN_SERVICE_CALL_ERROR);
//   }
//
//   private void assertStatusCodeGetRightErrorEnum(int statusCode, GatewayErrorEnum targetErrorEnum) {
//     GatewayErrorEnum errorEnum = userCenterClient.getErrorEnumByStatusCode(statusCode);
//     assertThat(errorEnum).isEqualTo(targetErrorEnum);
//   }
//
// }
