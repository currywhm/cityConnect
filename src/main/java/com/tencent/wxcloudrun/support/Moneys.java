package com.tencent.wxcloudrun.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Moneys {

  private Moneys() {
  }

  public static BigDecimal of(Object value) {
    if (value == null) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    if (value instanceof BigDecimal) {
      return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
    }
    return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
  }

  public static String format(Object value) {
    return of(value).toPlainString();
  }
}
