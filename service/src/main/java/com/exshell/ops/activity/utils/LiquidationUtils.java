package com.exshell.ops.activity.utils;

import com.exshell.common.model.Symbol;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LiquidationUtils {

  private static final int SCALE = 18;

  private static final BigDecimal FEE = new BigDecimal("0.005");

  private static final BigDecimal RATIO = BigDecimal.ONE.subtract(FEE);

  private static final BigDecimal ABNORMAL_FL_PRICE = BigDecimal.valueOf(1, SCALE + 1);

  /**
   * 净资产 = 可用资产 - 借贷资产(含利息) * 系数
   *
   * 可调整系数分别计算 预警价格、临界价格、下单价格
   *
   * @param base  基础货币净资产 {@link Symbol#baseCurrency}
   * @param quote 计价货币净资产 {@link Symbol#quoteCurrency}
   */
  public static BigDecimal calculatePrice(BigDecimal base, BigDecimal quote) {
    if (base.signum() >= 0) {
      if (quote.signum() >= 0) {
        return BigDecimal.ZERO; // 无风险
      }

      if (base.signum() == 0) {
        return ABNORMAL_FL_PRICE;
      }

      // 卖出，返回负值，当市场价小于其绝对值时触发
      return quote.divide(base.multiply(RATIO), SCALE, RoundingMode.UP);
    }

    if (quote.signum() > 0) {
      // 买入，返回正值，当市场价大于其绝对值时触发
      return quote.multiply(RATIO).divide(base, SCALE, RoundingMode.DOWN).negate();
    }

    return ABNORMAL_FL_PRICE;  // 异常状态，已爆仓
  }

  public static String getPriceType(BigDecimal price) {
    if (price.signum() == 0) {
      return "safe";
    }
    if (price.signum() < 0) {
      return "sell";
    }
    if (price != ABNORMAL_FL_PRICE) {
      return "buy";
    }
    return "error";
  }
}
