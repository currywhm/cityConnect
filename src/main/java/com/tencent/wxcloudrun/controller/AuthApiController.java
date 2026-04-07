package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthApiController {

  private final AuthService authService;

  public AuthApiController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/api/auth/wechat/login-session")
  public ApiResponse<Map<String, Object>> createLoginSession(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(authService.createLoginSession(body));
  }
}
