package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.RequestValues;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final ActivityService activityService;
  private final NotificationService notificationService;
  private final AppProperties appProperties;

  public ApplicationService(
      DatabaseSupport databaseSupport,
      DtoAssembler dtoAssembler,
      ActivityService activityService,
      NotificationService notificationService,
      AppProperties appProperties) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.activityService = activityService;
    this.notificationService = notificationService;
    this.appProperties = appProperties;
  }

  @Transactional
  public Map<String, Object> submit(String userId, String activityId, Map<String, Object> body) {
    Map<String, Object> activity = activityService.findActivity(activityId);
    if (userId.equals(String.valueOf(activity.get("organizer_user_id")))) {
      throw AppException.badRequest("OWN_ACTIVITY_NOT_ALLOWED", "不能报名自己发起的活动");
    }
    if (!"approved".equals(String.valueOf(activity.get("review_status"))) || !"open".equals(String.valueOf(activity.get("activity_status")))) {
      throw AppException.badRequest("ACTIVITY_NOT_JOINABLE", "活动当前不可报名");
    }
    if (((Number) activity.get("participant_count")).intValue() >= ((Number) activity.get("capacity")).intValue()) {
      throw AppException.badRequest("ACTIVITY_FULL", "活动已满员");
    }
    Number duplicated = databaseSupport.queryNumber("countActiveApplicationsForActivityAndUser", params("activityId", activityId, "userId", userId));
    if (duplicated.intValue() > 0) {
      throw AppException.conflict("APPLICATION_DUPLICATED", "你已提交过报名申请");
    }
    int attemptNo = databaseSupport.queryNumber("selectNextApplicationAttemptNo", params("activityId", activityId, "userId", userId)).intValue();
    String applicationId = Ids.newId();
    databaseSupport.update("insertApplication",
        params("id", applicationId, "activityId", activityId, "userId", userId, "attemptNo", attemptNo,
            "quote", RequestValues.requiredString(body, "quote", "QUOTE_REQUIRED", "缺少 quote"),
            "acceptFeeAmount", appProperties.getFee().getAcceptFeeAmount(),
            "clientRequestId", RequestValues.requiredString(body, "clientRequestId", "CLIENT_REQUEST_ID_REQUIRED", "缺少 clientRequestId")));
    notificationService.createNotification(
        String.valueOf(activity.get("organizer_user_id")),
        "activity",
        "你收到新的报名申请",
        "有用户申请加入你发起的活动",
        "application",
        applicationId,
        null
    );
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("applicationId", applicationId);
    data.put("status", "pending");
    return data;
  }

  @Transactional
  public Map<String, Object> review(String userId, String applicationId, Map<String, Object> body) {
    Map<String, Object> application = findApplication(applicationId);
    Map<String, Object> activity = activityService.requireOwnedActivity(userId, String.valueOf(application.get("activity_id")));
    String decision = RequestValues.requiredString(body, "decision", "DECISION_REQUIRED", "缺少 decision");
    String nextStatus = "approve".equalsIgnoreCase(decision) ? "accepted_pending_payment" : "rejected";
    if (!"pending".equals(String.valueOf(application.get("status")))) {
      throw AppException.conflict("APPLICATION_STATUS_INVALID", "当前申请状态不允许审核");
    }
    databaseSupport.update("updateApplicationReview",
        params("applicationId", applicationId, "status", nextStatus,
            "acceptedByUserId", "accepted_pending_payment".equals(nextStatus) ? userId : null,
            "reason", RequestValues.string(body, "reason") == null ? "" : RequestValues.string(body, "reason"),
            "acceptedAt", "accepted_pending_payment".equals(nextStatus) ? com.tencent.wxcloudrun.support.Times.now() : null,
            "rejectedAt", "rejected".equals(nextStatus) ? com.tencent.wxcloudrun.support.Times.now() : null));
    notificationService.createNotification(
        String.valueOf(application.get("applicant_user_id")),
        "activity",
        "accepted_pending_payment".equals(nextStatus) ? "报名已通过，请完成支付" : "报名未通过",
        "accepted_pending_payment".equals(nextStatus) ? "活动发起人已通过你的申请，请支付进群费" : "活动发起人未通过你的申请",
        "application",
        applicationId,
        null
    );
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("applicationId", applicationId);
    data.put("status", nextStatus);
    data.put("participantCount", ((Number) activity.get("participant_count")).intValue());
    return data;
  }

  @Transactional
  public Map<String, Object> withdraw(String userId, String applicationId) {
    Map<String, Object> application = findApplication(applicationId);
    if (!userId.equals(String.valueOf(application.get("applicant_user_id")))) {
      throw AppException.forbidden("APPLICATION_OWNER_REQUIRED", "只有申请人可以撤回");
    }
    if (!"pending".equals(String.valueOf(application.get("status"))) && !"accepted_pending_payment".equals(String.valueOf(application.get("status")))) {
      throw AppException.conflict("APPLICATION_WITHDRAW_INVALID", "当前状态不允许撤回");
    }
    databaseSupport.update("updateApplicationWithdrawn", params("applicationId", applicationId));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("applicationId", applicationId);
    data.put("status", "withdrawn");
    return data;
  }

  @Transactional
  public Map<String, Object> noShow(String applicationId) {
    Map<String, Object> application = findApplication(applicationId);
    databaseSupport.update("updateApplicationNoShow", params("applicationId", applicationId));
    databaseSupport.update("updateParticipantNoShowByApplicationId", params("applicationId", applicationId));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("applicationId", applicationId);
    data.put("status", "no_show");
    return data;
  }

  public Map<String, Object> listMine(String userId, Map<String, String> query) {
    return listByStatement("selectApplicationsMine", userId, query);
  }

  public Map<String, Object> listIncoming(String userId, Map<String, String> query) {
    return listByStatement("selectApplicationsIncoming", userId, query);
  }

  @Transactional
  public Map<String, Object> markAcceptOrderPaid(String applicationId, String orderId) {
    Map<String, Object> application = findApplication(applicationId);
    if (isTrue(application.get("accept_fee_paid"))) {
      return application;
    }
    String conversationId = ensureActivityConversation(String.valueOf(application.get("activity_id")));
    databaseSupport.update("updateApplicationAcceptedPaid", params("orderId", orderId, "conversationId", conversationId, "applicationId", applicationId));
    Map<String, Object> participant = databaseSupport.findOne("selectActivityParticipantByActivityAndUser",
        params("activityId", application.get("activity_id"), "userId", application.get("applicant_user_id"))).orElse(null);
    if (participant == null) {
      databaseSupport.update("insertPaidParticipant",
          params("id", Ids.newId(), "activityId", application.get("activity_id"), "userId", application.get("applicant_user_id"), "applicationId", applicationId));
    } else {
      databaseSupport.update("updateParticipantPaid", params("participantId", participant.get("id"), "applicationId", applicationId));
    }
    databaseSupport.update("updateActivityStatsAfterPayment",
        params("activityId", application.get("activity_id"), "acceptFeeAmount", application.get("accept_fee_amount")));
    syncConversationMembers(conversationId, String.valueOf(application.get("activity_id")));
    notificationService.createNotification(
        String.valueOf(application.get("accepted_by_user_id")),
        "order",
        "报名进群费支付成功",
        "有新的活动成员已完成支付",
        "application",
        applicationId,
        null
    );
    notificationService.createNotification(
        String.valueOf(application.get("applicant_user_id")),
        "message",
        "你已进入活动会话",
        "支付成功后已为你开放活动聊天",
        "conversation",
        conversationId,
        null
    );
    return findApplication(applicationId);
  }

  public Map<String, Object> findApplication(String applicationId) {
    return databaseSupport.findOne("selectApplicationsByActivityAndApplicantWithUser", params("applicationId", applicationId))
        .orElseThrow(() -> AppException.notFound("APPLICATION_NOT_FOUND", "报名申请不存在"));
  }

  private Map<String, Object> listByStatement(String statementId, String userId, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String status = query.get("status");
    Map<String, Object> params = params("userId", userId, "offset", offset, "limit", pageSize + 1);
    if (status != null && !status.trim().isEmpty()) {
      params.put("status", status.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll(statementId, params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.application(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  private String ensureActivityConversation(String activityId) {
    Map<String, Object> conversation = databaseSupport.findOne("selectConversationByBizTypeAndBizId",
        params("bizType", "activity", "bizId", activityId)).orElse(null);
    if (conversation != null) {
      return String.valueOf(conversation.get("id"));
    }
    Map<String, Object> activity = activityService.findActivity(activityId);
    String conversationId = Ids.newId();
    databaseSupport.update("insertConversation", params("id", conversationId, "bizType", "activity", "bizId", activityId, "title", activity.get("title")));
    syncConversationMembers(conversationId, activityId);
    return conversationId;
  }

  private void syncConversationMembers(String conversationId, String activityId) {
    List<Map<String, Object>> participants = databaseSupport.findAll("selectActivityParticipantsForConversationSync", params("activityId", activityId));
    for (Map<String, Object> participant : participants) {
      Number exists = databaseSupport.queryNumber("countConversationMemberByConversationAndUser",
          params("conversationId", conversationId, "userId", participant.get("user_id")));
      if (exists.intValue() == 0) {
        databaseSupport.update("insertConversationMember",
            params("id", Ids.newId(), "conversationId", conversationId, "userId", participant.get("user_id"),
                "memberRole", participant.get("role")));
      }
    }
  }

  private boolean isTrue(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue() == 1;
    }
    return Boolean.parseBoolean(String.valueOf(value));
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
