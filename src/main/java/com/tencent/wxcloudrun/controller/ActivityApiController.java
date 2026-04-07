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
public class ActivityApiController {

  private final ActivityService activityService;
  private final AuthSupport authSupport;

  public ActivityApiController(ActivityService activityService, AuthSupport authSupport) {
    this.activityService = activityService;
    this.authSupport = authSupport;
  }

  @PostMapping("/api/activities")
  public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(activityService.create(authSupport.requireUserId(), body));
  }

  @GetMapping("/api/activities")
  public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(activityService.list(query));
  }

  @GetMapping("/api/activities/{activityId}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable String activityId) {
    return ApiResponse.ok(activityService.detail(activityId, authSupport.currentUserId().orElse(null)));
  }

  @GetMapping("/api/activities/mine")
  public ApiResponse<Map<String, Object>> mine(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(activityService.listMine(authSupport.requireUserId(), query));
  }

  @PostMapping("/api/activities/{activityId}/withdraw")
  public ApiResponse<Map<String, Object>> withdraw(@PathVariable String activityId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(activityService.withdraw(authSupport.requireUserId(), activityId));
  }

  @PostMapping("/api/activities/{activityId}/finish")
  public ApiResponse<Map<String, Object>> finish(@PathVariable String activityId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(activityService.finish(authSupport.requireUserId(), activityId));
  }

  @GetMapping("/api/activities/nearby")
  public ApiResponse<Map<String, Object>> nearby(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(activityService.nearby(query));
  }
}
