package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentApiController {

  private final AuthSupport authSupport;
  private final PaymentService paymentService;

  public PaymentApiController(AuthSupport authSupport, PaymentService paymentService) {
    this.authSupport = authSupport;
    this.paymentService = paymentService;
  }

  @PostMapping("/api/payments/recharge-orders")
  public ApiResponse<Map<String, Object>> createRechargeOrder(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(paymentService.createRechargeOrder(authSupport.requireUserId(), body));
  }

  @PostMapping("/api/payments/accept-orders")
  public ApiResponse<Map<String, Object>> createAcceptOrder(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(paymentService.createAcceptOrder(authSupport.requireUserId(), body));
  }

  @GetMapping("/api/payments/orders/{orderNo}")
  public ApiResponse<Map<String, Object>> queryOrder(@PathVariable String orderNo) {
    return ApiResponse.ok(paymentService.queryOrder(authSupport.requireUserId(), orderNo));
  }

  @PostMapping("/api/payments/wechat/callback")
  public ApiResponse<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(paymentService.paymentCallback(body));
  }

  @PostMapping("/api/withdrawals")
  public ApiResponse<Map<String, Object>> applyWithdraw(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(paymentService.applyWithdraw(authSupport.requireUserId(), body));
  }

  @GetMapping("/api/withdrawals/mine")
  public ApiResponse<Map<String, Object>> myWithdrawals(@org.springframework.web.bind.annotation.RequestParam Map<String, String> query) {
    return ApiResponse.ok(paymentService.listWithdrawMine(authSupport.requireUserId(), query));
  }
}
