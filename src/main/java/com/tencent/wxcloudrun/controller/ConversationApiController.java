package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.ConversationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConversationApiController {

  private final AuthSupport authSupport;
  private final ConversationService conversationService;

  public ConversationApiController(AuthSupport authSupport, ConversationService conversationService) {
    this.authSupport = authSupport;
    this.conversationService = conversationService;
  }

  @GetMapping("/api/conversations")
  public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(conversationService.list(authSupport.requireUserId(), query));
  }

  @GetMapping("/api/conversations/{conversationId}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable String conversationId) {
    return ApiResponse.ok(conversationService.detail(authSupport.requireUserId(), conversationId));
  }

  @GetMapping("/api/conversations/{conversationId}/messages")
  public ApiResponse<Map<String, Object>> messages(@PathVariable String conversationId, @RequestParam Map<String, String> query) {
    return ApiResponse.ok(conversationService.listMessages(authSupport.requireUserId(), conversationId, query));
  }

  @PostMapping("/api/conversations/{conversationId}/messages")
  public ApiResponse<Map<String, Object>> send(@PathVariable String conversationId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(conversationService.sendMessage(authSupport.requireUserId(), conversationId, body));
  }

  @PostMapping("/api/conversations/{conversationId}/read")
  public ApiResponse<Map<String, Object>> markRead(@PathVariable String conversationId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(conversationService.markRead(authSupport.requireUserId(), conversationId, body));
  }

  @GetMapping("/api/activities/{activityId}/conversation-access")
  public ApiResponse<Map<String, Object>> accessCheck(@PathVariable String activityId) {
    return ApiResponse.ok(conversationService.accessCheck(authSupport.requireUserId(), activityId));
  }

  @PostMapping("/api/chat/ws-token")
  public ApiResponse<Map<String, Object>> wsToken() {
    return ApiResponse.ok(conversationService.wsToken(authSupport.requireUserId()));
  }
}
