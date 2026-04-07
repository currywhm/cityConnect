package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserApiController {

  private final AuthSupport authSupport;
  private final UserService userService;

  public UserApiController(AuthSupport authSupport, UserService userService) {
    this.authSupport = authSupport;
    this.userService = userService;
  }

  @GetMapping("/api/users/me")
  public ApiResponse<Map<String, Object>> me() {
    return ApiResponse.ok(userService.getMyProfile(authSupport.requireUserId()));
  }

  @PatchMapping("/api/users/me/profile")
  public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(userService.updateProfile(authSupport.requireUserId(), body));
  }

  @GetMapping("/api/users/me/credit")
  public ApiResponse<Map<String, Object>> credit() {
    return ApiResponse.ok(userService.getCredit(authSupport.requireUserId()));
  }

  @PostMapping("/api/users/me/blocks")
  public ApiResponse<Map<String, Object>> block(@RequestBody Map<String, Object> body) {
    return ApiResponse.ok(userService.block(authSupport.requireUserId(), body));
  }

  @DeleteMapping("/api/users/me/blocks/{targetUserId}")
  public ApiResponse<Map<String, Object>> unblock(@PathVariable String targetUserId) {
    return ApiResponse.ok(userService.unblock(authSupport.requireUserId(), targetUserId));
  }
}
