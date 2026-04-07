package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Numbers;
import com.tencent.wxcloudrun.support.RequestValues;
import com.tencent.wxcloudrun.support.Times;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

  private final DatabaseSupport databaseSupport;
  private final AppProperties appProperties;
  private final UserService userService;
  private final WalletService walletService;

  public AuthService(DatabaseSupport databaseSupport, AppProperties appProperties, UserService userService, WalletService walletService) {
    this.databaseSupport = databaseSupport;
    this.appProperties = appProperties;
    this.userService = userService;
    this.walletService = walletService;
  }

  @Transactional
  public Map<String, Object> createLoginSession(Map<String, Object> body) {
    String code = RequestValues.requiredString(body, "code", "WECHAT_CODE_REQUIRED", "缺少微信登录 code");
    String nickname = Optional.ofNullable(RequestValues.string(body, "nickname")).filter(value -> !value.isEmpty()).orElse("微信用户");
    String avatarUrl = Optional.ofNullable(RequestValues.string(body, "avatarUrl")).orElse("");
    String openid = "wx_" + sha256(code).substring(0, 24);
    Map<String, Object> user = databaseSupport.findOne("selectUserByOpenid", params("openid", openid)).orElse(null);
    boolean isNewUser = false;
    boolean bonusGranted = false;
    String userId;
    if (user == null) {
      isNewUser = true;
      bonusGranted = true;
      userId = Ids.newId();
      databaseSupport.update(
          "insertUser",
          params("id", userId, "openid", openid, "unionid", null, "appid", "miniprogram-city-connect", "nickname", nickname, "avatarUrl", avatarUrl)
      );
      databaseSupport.update("insertUserProfile", params("userId", userId));
      databaseSupport.update("insertUserCredit", params("userId", userId));
      walletService.grantSignupBonus(userId);
    } else {
      userId = String.valueOf(user.get("id"));
      databaseSupport.update(
          "updateUserLoginInfo",
          params("userId", userId, "nickname", nickname, "avatarUrl", avatarUrl)
      );
    }
    String sessionToken = Ids.newToken();
    LocalDateTime expiresAt = Times.plusDays(appProperties.getAuth().getSessionDays()).toLocalDateTime();
    databaseSupport.update("insertAppSession", params("id", Ids.newId(), "userId", userId, "sessionToken", sessionToken, "expiresAt", expiresAt));
    Map<String, Object> bundle = userService.loadUserBundle(userId);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("accountId", userId);
    data.put("sessionToken", sessionToken);
    data.put("sessionExpiresAt", Times.formatDateTime(expiresAt));
    data.put("isNewUser", isNewUser);
    data.put("bonusGranted", bonusGranted);
    data.put("user", userService.getMyProfile(userId).get("user"));
    data.put("profile", userService.getMyProfile(userId).get("profile"));
    data.put("wallet", userService.getMyProfile(userId).get("wallet"));
    data.put("credit", userService.getMyProfile(userId).get("credit"));
    return data;
  }

  public Optional<String> resolveUserIdBySessionToken(String sessionToken) {
    return databaseSupport.findOne("selectUserIdByValidSessionToken", params("sessionToken", sessionToken))
        .map(row -> String.valueOf(row.get("user_id")));
  }

  public String createPaymentOrderNo(String prefix) {
    return Numbers.orderNo(prefix);
  }

  private String sha256(String value) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte current : bytes) {
        builder.append(String.format("%02x", current));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("sha256 unavailable", exception);
    }
  }

  private Map<String, Object> params(Object... values) {
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      params.put(String.valueOf(values[i]), values[i + 1]);
    }
    return params;
  }
}
