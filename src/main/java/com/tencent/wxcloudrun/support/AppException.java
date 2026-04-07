package com.tencent.wxcloudrun.support;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

  private final int code;
  private final String errorCode;
  private final HttpStatus status;

  public AppException(int code, String errorCode, String message, HttpStatus status) {
    super(message);
    this.code = code;
    this.errorCode = errorCode;
    this.status = status;
  }

  public static AppException badRequest(String errorCode, String message) {
    return new AppException(4000, errorCode, message, HttpStatus.BAD_REQUEST);
  }

  public static AppException unauthorized(String errorCode, String message) {
    return new AppException(4001, errorCode, message, HttpStatus.UNAUTHORIZED);
  }

  public static AppException forbidden(String errorCode, String message) {
    return new AppException(4003, errorCode, message, HttpStatus.FORBIDDEN);
  }

  public static AppException notFound(String errorCode, String message) {
    return new AppException(4004, errorCode, message, HttpStatus.NOT_FOUND);
  }

  public static AppException conflict(String errorCode, String message) {
    return new AppException(4009, errorCode, message, HttpStatus.CONFLICT);
  }

  public int getCode() {
    return code;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
