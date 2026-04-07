package com.tencent.wxcloudrun.support;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class Times {

  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  private Times() {
  }

  public static String formatDateTime(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof OffsetDateTime) {
      return ((OffsetDateTime) value).format(DATE_TIME_FORMATTER);
    }
    if (value instanceof LocalDateTime) {
      return ((LocalDateTime) value).atZone(ZONE_ID).toOffsetDateTime().format(DATE_TIME_FORMATTER);
    }
    if (value instanceof Timestamp) {
      return ((Timestamp) value).toLocalDateTime().atZone(ZONE_ID).toOffsetDateTime().format(DATE_TIME_FORMATTER);
    }
    return String.valueOf(value);
  }

  public static String formatDate(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate) {
      return ((LocalDate) value).format(DATE_FORMATTER);
    }
    return String.valueOf(value);
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(ZONE_ID);
  }

  public static OffsetDateTime plusDays(int days) {
    return OffsetDateTime.now(ZONE_ID).plusDays(days).withOffsetSameInstant(ZoneOffset.ofHours(8));
  }

  public static LocalDateTime parseDateTime(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return OffsetDateTime.parse(value).atZoneSameInstant(ZONE_ID).toLocalDateTime();
  }
}
