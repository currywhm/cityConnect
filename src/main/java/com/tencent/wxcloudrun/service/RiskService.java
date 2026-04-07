package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Jsons;
import com.tencent.wxcloudrun.support.RequestValues;
import com.tencent.wxcloudrun.support.Times;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiskService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final Jsons jsons;
  private final NotificationService notificationService;

  public RiskService(DatabaseSupport databaseSupport, DtoAssembler dtoAssembler, Jsons jsons, NotificationService notificationService) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.jsons = jsons;
    this.notificationService = notificationService;
  }

  @Transactional
  public Map<String, Object> createReport(String userId, Map<String, Object> body) {
    String reportId = Ids.newId();
    databaseSupport.update("insertReport",
        params("id", reportId, "userId", userId,
            "targetType", RequestValues.requiredString(body, "targetType", "TARGET_TYPE_REQUIRED", "缺少 targetType"),
            "targetId", RequestValues.requiredString(body, "targetId", "TARGET_ID_REQUIRED", "缺少 targetId"),
            "reasonType", RequestValues.requiredString(body, "reasonType", "REASON_TYPE_REQUIRED", "缺少 reasonType"),
            "contentText", RequestValues.requiredString(body, "content", "CONTENT_REQUIRED", "缺少 content"),
            "evidenceJson", jsons.toJson(RequestValues.stringList(body, "evidenceUrls"))));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("reportId", reportId);
    data.put("status", "pending");
    return data;
  }

  public Map<String, Object> getRestriction(String userId) {
    Map<String, Object> credit = databaseSupport.findOne("selectUserCreditById", params("userId", userId))
        .orElseThrow(() -> AppException.notFound("CREDIT_NOT_FOUND", "信用信息不存在"));
    LocalDateTime restrictedUntil = credit.get("restricted_until") == null ? null : ((java.sql.Timestamp) credit.get("restricted_until")).toLocalDateTime();
    boolean restricted = restrictedUntil != null && restrictedUntil.isAfter(Times.now());
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("riskLevel", credit.get("risk_level"));
    data.put("restricted", restricted);
    data.put("restrictedUntil", Times.formatDateTime(credit.get("restricted_until")));
    data.put("reason", restricted ? (credit.get("notes") == null ? "账号存在风控限制" : credit.get("notes")) : "");
    return data;
  }

  @Transactional
  public Map<String, Object> moderateMessage(Map<String, Object> body) {
    String text = RequestValues.requiredString(body, "text", "TEXT_REQUIRED", "缺少 text");
    String category = null;
    String action = "allow";
    String hitKeyword = null;
    String normalized = text.toLowerCase();
    if (normalized.contains("vx") || normalized.contains("微信") || normalized.contains("手机号") || normalized.contains("phone")) {
      category = "contact";
      action = "review";
      hitKeyword = "联系方式";
    }
    if (normalized.contains("诈骗") || normalized.contains("色情") || normalized.contains("辱骂")) {
      category = "sensitive";
      action = "block";
      hitKeyword = "敏感词";
    }
    databaseSupport.update("insertModerationLog",
        params("id", Ids.newId(), "userId", RequestValues.string(body, "senderUserId"), "conversationId", RequestValues.string(body, "conversationId"),
            "category", category == null ? "clean" : category, "hitKeyword", hitKeyword, "action", action, "rawText", text));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("pass", "allow".equals(action));
    data.put("category", category);
    data.put("action", action);
    data.put("hitKeyword", hitKeyword);
    return data;
  }

  public Map<String, Object> adminReportList(Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String status = query.get("status");
    String targetType = query.get("targetType");
    Map<String, Object> params = params("offset", offset, "limit", pageSize + 1);
    if (status != null && !status.trim().isEmpty()) {
      params.put("status", status.trim());
    }
    if (targetType != null && !targetType.trim().isEmpty()) {
      params.put("targetType", targetType.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectReports", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.report(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  @Transactional
  public Map<String, Object> adminReviewReport(String reportId, Map<String, Object> body) {
    String decision = RequestValues.requiredString(body, "decision", "DECISION_REQUIRED", "缺少 decision");
    String action = RequestValues.string(body, "action");
    String remark = RequestValues.string(body, "remark");
    String status = "resolve".equalsIgnoreCase(decision) ? "resolved" : "rejected";
    Map<String, Object> report = databaseSupport.findOne("selectReportById", params("reportId", reportId))
        .orElseThrow(() -> AppException.notFound("REPORT_NOT_FOUND", "举报不存在"));
    databaseSupport.update("updateReportReview", params("reportId", reportId, "status", status, "decision", decision, "remark", remark));
    if ("restrict".equalsIgnoreCase(action) || "ban".equalsIgnoreCase(action)) {
      databaseSupport.update("updateUserStatus",
          params("userId", report.get("target_id"), "userStatus", "ban".equalsIgnoreCase(action) ? "closed" : "restricted"));
      databaseSupport.update("updateUserCreditRestriction",
          params("userId", report.get("target_id"), "restrictedUntil", LocalDateTime.now().plusDays(30), "notes", remark == null ? "被举报后触发风控限制" : remark));
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("reportId", reportId);
    data.put("status", status);
    return data;
  }

  @Transactional
  public Map<String, Object> adminRestrictUser(String userId, Map<String, Object> body) {
    String restrictedUntil = RequestValues.requiredString(body, "restrictedUntil", "RESTRICTED_UNTIL_REQUIRED", "缺少 restrictedUntil");
    String reason = RequestValues.requiredString(body, "reason", "REASON_REQUIRED", "缺少 reason");
    databaseSupport.update("updateUserStatus", params("userId", userId, "userStatus", "restricted"));
    databaseSupport.update("updateUserCreditRestriction",
        params("userId", userId, "restrictedUntil", Times.parseDateTime(restrictedUntil), "notes", reason));
    notificationService.createNotification(userId, "system", "账号已被限制", reason, "user", userId, null);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("userId", userId);
    data.put("status", "restricted");
    return data;
  }

  private Integer parseInteger(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return Integer.parseInt(value.trim());
  }

  private Map<String, Object> params(Object... values) {
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      params.put(String.valueOf(values[i]), values[i + 1]);
    }
    return params;
  }
}
