package com.tencent.wxcloudrun.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Numbers {

  private static final DateTimeFormatter ORDER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private Numbers() {
  }

  public static String orderNo(String prefix) {
    return prefix + ORDER_FORMAT.format(Times.now()) + Ids.newId().substring(20);
  }
}
