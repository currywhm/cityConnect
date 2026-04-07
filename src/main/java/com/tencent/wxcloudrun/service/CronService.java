package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.support.DatabaseSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CronService {

  private final ActivityService activityService;
  private final ConversationService conversationService;
  private final DatabaseSupport databaseSupport;

  public CronService(ActivityService activityService, ConversationService conversationService, DatabaseSupport databaseSupport) {
    this.activityService = activityService;
    this.conversationService = conversationService;
    this.databaseSupport = databaseSupport;
  }

  public Map<String, Object> settleEndedActivities(Map<String, Object> body) {
    Integer batchSize = body.get("batchSize") == null ? null : Integer.parseInt(String.valueOf(body.get("batchSize")));
    boolean dryRun = body.get("dryRun") != null && Boolean.parseBoolean(String.valueOf(body.get("dryRun")));
    return activityService.settleEndedActivities(batchSize, dryRun);
  }

  public Map<String, Object> expireActivityChats(Map<String, Object> body) {
    Integer batchSize = body.get("batchSize") == null ? null : Integer.parseInt(String.valueOf(body.get("batchSize")));
    return conversationService.expireActivityChats(batchSize);
  }

  @Transactional
  public Map<String, Object> syncCreditScore(Map<String, Object> body) {
    Integer batchSize = body.get("batchSize") == null ? 100 : Integer.parseInt(String.valueOf(body.get("batchSize")));
    List<Map<String, Object>> users = databaseSupport.findAll("selectUserIdsForCreditSync", params("limit", batchSize));
    int updated = 0;
    for (Map<String, Object> user : users) {
      String userId = String.valueOf(user.get("id"));
      int noShowCount = databaseSupport.queryNumber("countApplicationsByApplicantAndStatus", params("userId", userId, "status", "no_show")).intValue();
      int withdrawCount = databaseSupport.queryNumber("countApplicationsByApplicantAndStatus", params("userId", userId, "status", "withdrawn")).intValue();
      int completeCount = databaseSupport.queryNumber("countCompletedApplicationsByApplicant", params("userId", userId)).intValue();
      int reportCount = databaseSupport.queryNumber("countReportsByTargetUser", params("userId", userId)).intValue();
      int creditScore = Math.max(0, 100 - noShowCount * 25 - withdrawCount * 8 - reportCount * 10 + Math.min(completeCount, 10) * 3);
      String riskLevel = creditScore >= 90 ? "low" : creditScore >= 70 ? "normal" : "high";
      databaseSupport.update("updateUserCreditScore",
          params("userId", userId, "creditScore", creditScore, "noShowCount", noShowCount, "withdrawCount", withdrawCount,
              "completeCount", completeCount, "reportCount", reportCount, "riskLevel", riskLevel));
      updated++;
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("updatedUserCount", updated);
    return data;
  }

  public Map<String, Object> pushUnreadNotifications(Map<String, Object> body) {
    Integer batchSize = body.get("batchSize") == null ? 100 : Integer.parseInt(String.valueOf(body.get("batchSize")));
    int pushCount = databaseSupport.queryNumber("countUsersWithUnreadNotifications", params("limit", batchSize)).intValue();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("pushCount", pushCount);
    data.put("failCount", 0);
    return data;
  }

  @Transactional
  public Map<String, Object> archiveActivities(Map<String, Object> body) {
    Integer batchSize = body.get("batchSize") == null ? 100 : Integer.parseInt(String.valueOf(body.get("batchSize")));
    int archivedCount = databaseSupport.update("archiveActivities", params("archiveBefore", LocalDateTime.now().minusDays(1), "limit", batchSize));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("archivedCount", archivedCount);
    return data;
  }

  private Map<String, Object> params(Object... values) {
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      params.put(String.valueOf(values[i]), values[i + 1]);
    }
    return params;
  }
}
