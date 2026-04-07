package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminWithdrawApiController {

  private final AuthSupport authSupport;
  private final PaymentService paymentService;

  public AdminWithdrawApiController(AuthSupport authSupport, PaymentService paymentService) {
    this.authSupport = authSupport;
    this.paymentService = paymentService;
  }

  @GetMapping("/api/admin/withdrawals")
  public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
    authSupport.requireAdmin();
    return ApiResponse.ok(paymentService.listWithdrawAdmin(query));
  }

  @PostMapping("/api/admin/withdrawals/{withdrawRequestId}/review")
  public ApiResponse<Map<String, Object>> review(@PathVariable String withdrawRequestId, @RequestBody Map<String, Object> body) {
    authSupport.requireAdmin();
    return ApiResponse.ok(paymentService.adminReviewWithdraw(withdrawRequestId, body));
  }
}
