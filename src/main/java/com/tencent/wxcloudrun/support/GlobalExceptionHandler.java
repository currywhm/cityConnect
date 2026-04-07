package com.tencent.wxcloudrun.support;

import com.tencent.wxcloudrun.config.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Object>> handleAppException(AppException exception) {
    return ResponseEntity.status(exception.getStatus())
        .body(ApiResponse.error(exception.getCode(), exception.getErrorCode(), exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException exception) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.error(4000, "INVALID_ARGUMENT", exception.getBindingResult().getAllErrors().get(0).getDefaultMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(Exception exception) {
    LOGGER.error("Unhandled exception, requestId={}", RequestContext.getRequestId(), exception);
    return ResponseEntity.internalServerError()
        .body(ApiResponse.error(5000, "INTERNAL_ERROR", "服务开小差了，请稍后再试"));
  }
}
