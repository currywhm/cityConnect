package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
import com.tencent.wxcloudrun.support.AppException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class AuthSupport {

  private static final String ATTR_USER_ID = "city.connect.userId";
  private final ObjectProvider<HttpServletRequest> requestProvider;
  private final AuthService authService;
  private final AppProperties appProperties;

  public AuthSupport(ObjectProvider<HttpServletRequest> requestProvider, AuthService authService, AppProperties appProperties) {
    this.requestProvider = requestProvider;
    this.authService = authService;
    this.appProperties = appProperties;
  }

  public Optional<String> currentUserId() {
    HttpServletRequest request = request();
    Object cached = request.getAttribute(ATTR_USER_ID);
    if (cached instanceof String) {
      return Optional.of((String) cached);
    }
    String token = sessionToken(request);
    if (token == null || token.isEmpty()) {
      return Optional.empty();
    }
    Optional<String> userId = authService.resolveUserIdBySessionToken(token);
    userId.ifPresent(value -> request.setAttribute(ATTR_USER_ID, value));
    return userId;
  }

  public String requireUserId() {
    return currentUserId().orElseThrow(() -> AppException.unauthorized("SESSION_REQUIRED", "请先登录"));
  }

  public void requireAdmin() {
    String token = header("X-Admin-Token");
    if (!appProperties.getAuth().getAdminToken().equals(token)) {
      throw AppException.forbidden("ADMIN_TOKEN_INVALID", "管理员权限校验失败");
    }
  }

  public void requireInternal() {
    String token = header("X-Internal-Token");
    if (!appProperties.getAuth().getInternalToken().equals(token)) {
      throw AppException.forbidden("INTERNAL_TOKEN_INVALID", "内部调用权限校验失败");
    }
  }

  private String header(String name) {
    return request().getHeader(name);
  }

  private HttpServletRequest request() {
    HttpServletRequest request = requestProvider.getIfAvailable();
    if (request == null) {
      throw AppException.unauthorized("REQUEST_MISSING", "请求上下文缺失");
    }
    return request;
  }

  private String sessionToken(HttpServletRequest request) {
    String token = request.getHeader("X-Session-Token");
    if (token != null && !token.trim().isEmpty()) {
      return token.trim();
    }
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring("Bearer ".length()).trim();
    }
    return null;
  }
}
