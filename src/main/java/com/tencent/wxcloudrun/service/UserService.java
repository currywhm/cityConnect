package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.RequestValues;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final WalletService walletService;

  public UserService(DatabaseSupport databaseSupport, DtoAssembler dtoAssembler, WalletService walletService) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.walletService = walletService;
  }

  public Map<String, Object> getMyProfile(String userId) {
    Map<String, Object> bundle = loadUserBundle(userId);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("user", dtoAssembler.user(row("user", bundle)));
    data.put("profile", dtoAssembler.profile(row("profile", bundle)));
    data.put("credit", dtoAssembler.credit(row("credit", bundle)));
    data.put("wallet", dtoAssembler.wallet(row("wallet", bundle)));
    return data;
  }

  public Map<String, Object> getCredit(String userId) {
    return dtoAssembler.credit(findCredit(userId));
  }

  @Transactional
  public Map<String, Object> updateProfile(String userId, Map<String, Object> body) {
    String city = RequestValues.string(body, "city");
    String bio = RequestValues.string(body, "bio");
    databaseSupport.update("updateUserProfile", params("userId", userId, "city", city, "bio", bio));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("profile", dtoAssembler.profile(findProfile(userId)));
    return data;
  }

  @Transactional
  public Map<String, Object> block(String userId, Map<String, Object> body) {
    String targetUserId = RequestValues.requiredString(body, "targetUserId", "TARGET_USER_REQUIRED", "缺少 targetUserId");
    if (userId.equals(targetUserId)) {
      throw AppException.badRequest("SELF_BLOCK_NOT_ALLOWED", "不能拉黑自己");
    }
    Map<String, Object> existing = databaseSupport.findOne("selectUserBlock", params("userId", userId, "targetUserId", targetUserId)).orElse(null);
    String blockId = existing == null ? Ids.newId() : String.valueOf(existing.get("id"));
    if (existing == null) {
      databaseSupport.update("insertUserBlock",
          params("id", blockId, "userId", userId, "targetUserId", targetUserId, "reason", RequestValues.requiredString(body, "reason", "REASON_REQUIRED", "缺少 reason")));
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("blockId", blockId);
    data.put("status", "blocked");
    return data;
  }

  @Transactional
  public Map<String, Object> unblock(String userId, String targetUserId) {
    databaseSupport.update("deleteUserBlock", params("userId", userId, "targetUserId", targetUserId));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("status", "unblocked");
    return data;
  }

  public Map<String, Object> loadUserBundle(String userId) {
    Map<String, Object> bundle = new HashMap<>();
    bundle.put("user", findUser(userId));
    bundle.put("profile", findProfile(userId));
    bundle.put("credit", findCredit(userId));
    bundle.put("wallet", findWallet(userId));
    return bundle;
  }

  public Map<String, Object> findUser(String userId) {
    return databaseSupport.findOne("selectUserById", params("userId", userId))
        .orElseThrow(() -> AppException.notFound("USER_NOT_FOUND", "用户不存在"));
  }

  public Map<String, Object> findProfile(String userId) {
    return databaseSupport.findOne("selectUserProfileById", params("userId", userId))
        .orElseThrow(() -> AppException.notFound("PROFILE_NOT_FOUND", "用户资料不存在"));
  }

  public Map<String, Object> findCredit(String userId) {
    return databaseSupport.findOne("selectUserCreditById", params("userId", userId))
        .orElseThrow(() -> AppException.notFound("CREDIT_NOT_FOUND", "用户信用不存在"));
  }

  public Map<String, Object> findWallet(String userId) {
    walletService.getWalletSummary(userId);
    return databaseSupport.findOne("selectWalletByUserId", params("userId", userId))
        .orElseThrow(() -> AppException.notFound("WALLET_NOT_FOUND", "钱包不存在"));
  }

  private Map<String, Object> row(String key, Map<String, Object> bundle) {
    return (Map<String, Object>) bundle.get(key);
  }

  private Map<String, Object> params(Object... values) {
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      params.put(String.valueOf(values[i]), values[i + 1]);
    }
    return params;
  }
}
