package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotificationApiController {

  private final AuthSupport authSupport;
  private final NotificationService notificationService;

  public NotificationApiController(AuthSupport authSupport, NotificationService notificationService) {
    this.authSupport = authSupport;
    this.notificationService = notificationService;
  }

  @GetMapping("/api/notifications")
  public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(notificationService.list(authSupport.requireUserId(), query));
  }

  @GetMapping("/api/notifications/unread-summary")
  public ApiResponse<Map<String, Object>> unreadSummary() {
    return ApiResponse.ok(notificationService.unreadSummary(authSupport.requireUserId()));
  }

  @PostMapping("/api/notifications/mark-read")
  public ApiResponse<Map<String, Object>> markRead(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(notificationService.markRead(authSupport.requireUserId(), body));
  }

  @PostMapping("/api/internal/notifications/subscribe/send")
  public ApiResponse<Map<String, Object>> subscribeSend(@RequestBody Map<String, Object> body) {
    authSupport.requireInternal();
    notificationService.createNotification(
        String.valueOf(body.get("userId")),
        String.valueOf(body.get("templateType")),
        "系统通知",
        "已触发订阅消息推送",
        "internal_notify",
        body.get("bizId") == null ? null : String.valueOf(body.get("bizId")),
        body
    );
    return ApiResponse.ok(java.util.Collections.singletonMap("success", true));
  }
}
