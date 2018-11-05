// package com.exshell.ops.gateway.api;
//
// import static org.junit.Assert.*;
//
// import org.junit.Before;
// import org.junit.Test;
//
// import com.exshell.usercenter.utils.GatewayUtil;
//
// public class UserCenterControllerTest {
//
//   @Before
//   public void setUp() throws Exception {}
//
//   @Test
//   public void testConvertKey() {
//     String[][] arrs = new String[][] {{"AbcXyz", "abc-xyz"}, {"a", "a"}, {"A", "a"},
//         {"ABC", "a-b-c"}, {"abcXyzHaha", "abc-xyz-haha"}};
//     for (String[] ss : arrs) {
//       String input = ss[0];
//       String output = ss[1];
//       assertEquals(output, GatewayUtil.convertKey(input));
//     }
//   }
//
// }
