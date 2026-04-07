package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.ActivityService;
import com.tencent.wxcloudrun.service.AuthSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminActivityApiController {

  private final ActivityService activityService;
  private final AuthSupport authSupport;

  public AdminActivityApiController(ActivityService activityService, AuthSupport authSupport) {
    this.activityService = activityService;
    this.authSupport = authSupport;
  }

  @GetMapping("/api/admin/activities/reviews")
  public ApiResponse<Map<String, Object>> reviews(@RequestParam Map<String, String> query) {
    authSupport.requireAdmin();
    return ApiResponse.ok(activityService.adminReviewList(query));
  }

  @PostMapping("/api/admin/activities/{activityId}/review")
  public ApiResponse<Map<String, Object>> review(@PathVariable String activityId, @RequestBody Map<String, Object> body) {
    authSupport.requireAdmin();
    return ApiResponse.ok(activityService.adminReview(activityId, body));
  }
}
