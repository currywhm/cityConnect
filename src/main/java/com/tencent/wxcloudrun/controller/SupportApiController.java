package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.SupportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SupportApiController {

  private final AuthSupport authSupport;
  private final SupportService supportService;

  public SupportApiController(AuthSupport authSupport, SupportService supportService) {
    this.authSupport = authSupport;
    this.supportService = supportService;
  }

  @PostMapping("/api/support/bot/ask")
  public ApiResponse<Map<String, Object>> ask(@RequestBody Map<String, Object> body) {
    authSupport.requireUserId();
    return ApiResponse.ok(supportService.askBot(body));
  }

  @PostMapping("/api/support/tickets")
  public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(supportService.createTicket(authSupport.requireUserId(), body));
  }

  @GetMapping("/api/support/tickets")
  public ApiResponse<Map<String, Object>> mine(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(supportService.listMyTickets(authSupport.requireUserId(), query));
  }

  @PostMapping("/api/support/tickets/{ticketId}/reply")
  public ApiResponse<Map<String, Object>> reply(@PathVariable String ticketId, @RequestBody Map<String, Object> body) {
    return ApiResponse.ok(supportService.reply(authSupport.requireUserId(), ticketId, body));
  }

  @GetMapping("/api/support/faqs")
  public ApiResponse<Map<String, Object>> faq(@RequestParam(required = false) String keyword) {
    return ApiResponse.ok(supportService.faqList(keyword));
  }
}
