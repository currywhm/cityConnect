package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.CronService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class InternalCronController {

  private final AuthSupport authSupport;
  private final CronService cronService;

  public InternalCronController(AuthSupport authSupport, CronService cronService) {
    this.authSupport = authSupport;
    this.cronService = cronService;
  }

  @PostMapping("/api/internal/cron/settle-ended-activities")
  public ApiResponse<Map<String, Object>> settle(@RequestBody(required = false) Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(cronService.settleEndedActivities(body == null ? Collections.emptyMap() : body));
  }

  @PostMapping("/api/internal/cron/expire-activity-chats")
  public ApiResponse<Map<String, Object>> expireChats(@RequestBody(required = false) Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(cronService.expireActivityChats(body == null ? Collections.emptyMap() : body));
  }

  @PostMapping("/api/internal/cron/sync-credit-score")
  public ApiResponse<Map<String, Object>> syncCredit(@RequestBody(required = false) Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(cronService.syncCreditScore(body == null ? Collections.emptyMap() : body));
  }

  @PostMapping("/api/internal/cron/push-unread-notifications")
  public ApiResponse<Map<String, Object>> pushUnread(@RequestBody(required = false) Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(cronService.pushUnreadNotifications(body == null ? Collections.emptyMap() : body));
  }

  @PostMapping("/api/internal/cron/archive-activities")
  public ApiResponse<Map<String, Object>> archive(@RequestBody(required = false) Map<String, Object> body) {
    authSupport.requireInternal();
    return ApiResponse.ok(cronService.archiveActivities(body == null ? Collections.emptyMap() : body));
  }
}
