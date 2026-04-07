package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.ApplicationService;
import com.tencent.wxcloudrun.service.AuthSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApplicationApiController {

  private final ApplicationService applicationService;
  private final AuthSupport authSupport;

  public ApplicationApiController(ApplicationService applicationService, AuthSupport authSupport) {
    this.applicationService = applicationService;
    this.authSupport = authSupport;
  }

  @PostMapping("/api/activities/{activityId}/applications")
  public ApiResponse<Map<String, Object>> submit(@PathVariable String activityId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(applicationService.submit(authSupport.requireUserId(), activityId, body));
  }

  @PostMapping("/api/applications/{applicationId}/review")
  public ApiResponse<Map<String, Object>> review(@PathVariable String applicationId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(applicationService.review(authSupport.requireUserId(), applicationId, body));
  }

  @PostMapping("/api/applications/{applicationId}/withdraw")
  public ApiResponse<Map<String, Object>> withdraw(@PathVariable String applicationId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(applicationService.withdraw(authSupport.requireUserId(), applicationId));
  }

  @PostMapping("/api/applications/{applicationId}/no-show")
  public ApiResponse<Map<String, Object>> noShow(@PathVariable String applicationId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(applicationService.noShow(applicationId));
  }

  @GetMapping("/api/applications/mine")
  public ApiResponse<Map<String, Object>> mine(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(applicationService.listMine(authSupport.requireUserId(), query));
  }

  @GetMapping("/api/applications/incoming")
  public ApiResponse<Map<String, Object>> incoming(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(applicationService.listIncoming(authSupport.requireUserId(), query));
  }
}
