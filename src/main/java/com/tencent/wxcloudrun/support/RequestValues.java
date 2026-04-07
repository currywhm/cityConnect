package com.tencent.wxcloudrun.support;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RequestValues {

  private RequestValues() {
  }

  public static String string(Map<String, ?> body, String key) {
    Object value = body == null ? null : body.get(key);
    return value == null ? null : String.valueOf(value).trim();
  }

  public static String requiredString(Map<String, ?> body, String key, String errorCode, String message) {
    String value = string(body, key);
    if (value == null || value.isEmpty()) {
      throw AppException.badRequest(errorCode, message);
    }
    return value;
  }

  public static Integer integer(Map<String, ?> body, String key) {
    Object value = body == null ? null : body.get(key);
    if (value == null || String.valueOf(value).trim().isEmpty()) {
      return null;
    }
    return Integer.parseInt(String.valueOf(value));
  }

  public static Boolean bool(Map<String, ?> body, String key) {
    Object value = body == null ? null : body.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return Boolean.parseBoolean(String.valueOf(value));
  }

  public static BigDecimal decimal(Map<String, ?> body, String key) {
    Object value = body == null ? null : body.get(key);
    if (value == null || String.valueOf(value).trim().isEmpty()) {
      return null;
    }
    return new BigDecimal(String.valueOf(value));
  }

  @SuppressWarnings("unchecked")
  public static List<String> stringList(Map<String, ?> body, String key) {
    Object value = body == null ? null : body.get(key);
    if (value == null) {
      return Collections.emptyList();
    }
    if (value instanceof List) {
      return (List<String>) value;
    }
    return Collections.singletonList(String.valueOf(value));
  }

  public static Object object(Map<String, ?> body, String key) {
    return body == null ? null : body.get(key);
  }
}
