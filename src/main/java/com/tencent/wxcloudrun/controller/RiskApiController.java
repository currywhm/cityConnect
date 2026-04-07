package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.RiskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RiskApiController {

  private final AuthSupport authSupport;
  private final RiskService riskService;

  public RiskApiController(AuthSupport authSupport, RiskService riskService) {
    this.authSupport = authSupport;
    this.riskService = riskService;
  }

  @PostMapping("/api/reports")
  public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(riskService.createReport(authSupport.requireUserId(), body));
  }

  @GetMapping("/api/risk/restriction")
  public ApiResponse<Map<String, Object>> restriction() {
    return ApiResponse.ok(riskService.getRestriction(authSupport.requireUserId()));
  }

  @PostMapping("/api/internal/risk/moderate-message")
  public ApiResponse<Map<String, Object>> moderate(@RequestBody Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(riskService.moderateMessage(body));
  }

  @GetMapping("/api/admin/reports")
  public ApiResponse<Map<String, Object>> adminReports(@RequestParam Map<String, String> query) {
    authSupport.requireAdmin();
    return ApiResponse.ok(riskService.adminReportList(query));
  }

  @PostMapping("/api/admin/reports/{reportId}/review")
  public ApiResponse<Map<String, Object>> review(@PathVariable String reportId, @RequestBody Map<String, Object> body) {
    authSupport.requireAdmin();
    return ApiResponse.ok(riskService.adminReviewReport(reportId, body));
  }

  @PostMapping("/api/admin/users/{userId}/restrict")
  public ApiResponse<Map<String, Object>> restrict(@PathVariable String userId, @RequestBody Map<String, Object> body) {
    authSupport.requireAdmin();
    return ApiResponse.ok(riskService.adminRestrictUser(userId, body));
  }
}
