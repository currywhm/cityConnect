package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.support.RequestContext;

import java.util.Collections;

public final class ApiResponse<T> {

  private final Integer code;
  private final String message;
  private final String requestId;
  private final T data;
  private final String errorCode;

  private ApiResponse(Integer code, String message, String requestId, T data, String errorCode) {
    this.code = code;
    this.message = message;
    this.requestId = requestId;
    this.data = data;
    this.errorCode = errorCode;
  }

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(0, "ok", RequestContext.getRequestId(), data, null);
  }

  public static ApiResponse<Object> ok() {
    return ok(Collections.emptyMap());
  }

  public static ApiResponse<Object> error(String message) {
    return error(4000, "BUSINESS_ERROR", message);
  }

  public static ApiResponse<Object> error(Integer code, String errorCode, String message) {
    return new ApiResponse<>(code, message, RequestContext.getRequestId(), null, errorCode);
  }

  public Integer getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String getRequestId() {
    return requestId;
  }

  public T getData() {
    return data;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
