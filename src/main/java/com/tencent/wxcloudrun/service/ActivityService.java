package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Moneys;
import com.tencent.wxcloudrun.support.RequestValues;
import com.tencent.wxcloudrun.support.Times;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ActivityService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final UserService userService;
  private final WalletService walletService;
  private final AuthService authService;
  private final NotificationService notificationService;
  private final AppProperties appProperties;

  public ActivityService(
      DatabaseSupport databaseSupport,
      DtoAssembler dtoAssembler,
      UserService userService,
      WalletService walletService,
      AuthService authService,
      NotificationService notificationService,
      AppProperties appProperties) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.userService = userService;
    this.walletService = walletService;
    this.authService = authService;
    this.notificationService = notificationService;
    this.appProperties = appProperties;
  }

  @Transactional
  public Map<String, Object> create(String userId, Map<String, Object> body) {
    ensureUserActive(userId);
    String title = RequestValues.requiredString(body, "title", "TITLE_REQUIRED", "缺少活动标题");
    String category = RequestValues.requiredString(body, "category", "CATEGORY_REQUIRED", "缺少活动分类");
    String description = RequestValues.requiredString(body, "description", "DESCRIPTION_REQUIRED", "缺少活动描述");
    LocalDateTime startAt = Times.parseDateTime(RequestValues.requiredString(body, "startAt", "START_AT_REQUIRED", "缺少活动开始时间"));
    Integer durationHours = RequestValues.integer(body, "durationHours");
    Integer capacity = RequestValues.integer(body, "capacity");
    if (durationHours == null || durationHours <= 0) {
      throw AppException.badRequest("DURATION_INVALID", "durationHours 必须大于 0");
    }
    if (capacity == null || capacity <= 0) {
      throw AppException.badRequest("CAPACITY_INVALID", "capacity 必须大于 0");
    }
    LocalDateTime endAt = startAt.plusHours(durationHours);
    String activityId = Ids.newId();
    String orderId = Ids.newId();
    String orderNo = authService.createPaymentOrderNo("PF");
    BigDecimal publishFee = appProperties.getFee().getPublishFeeAmount();
    BigDecimal feeAmount = RequestValues.decimal(body, "feeAmount");
    List<String> coverUrls = RequestValues.stringList(body, "coverUrls");
    String coverUrl = coverUrls.isEmpty() ? "" : coverUrls.get(0);

    databaseSupport.update("insertActivity",
        params("id", activityId, "userId", userId, "title", title, "category", category, "description", description,
            "locationName", RequestValues.requiredString(body, "locationName", "LOCATION_NAME_REQUIRED", "缺少 locationName"),
            "locationAddress", RequestValues.requiredString(body, "locationAddress", "LOCATION_ADDRESS_REQUIRED", "缺少 locationAddress"),
            "latitude", RequestValues.decimal(body, "latitude"), "longitude", RequestValues.decimal(body, "longitude"),
            "areaCode", Optional.ofNullable(RequestValues.string(body, "areaCode")).orElse("citywide"),
            "coverUrl", coverUrl, "feeMode", Optional.ofNullable(RequestValues.string(body, "feeMode")).orElse("onsite_confirm"),
            "feeAmount", feeAmount, "feeDesc", Optional.ofNullable(RequestValues.string(body, "feeDesc")).orElse(""),
            "publishFee", publishFee, "publishFeeOrderId", orderId, "capacity", capacity, "startAt", startAt, "endAt", endAt));
    databaseSupport.update("insertSuccessPaymentOrder",
        params("id", orderId, "orderNo", orderNo, "userId", userId, "bizId", activityId, "amount", publishFee,
            "clientRequestId", RequestValues.requiredString(body, "clientRequestId", "CLIENT_REQUEST_ID_REQUIRED", "缺少 clientRequestId")));
    walletService.chargePublishFee(userId, activityId, publishFee);
    databaseSupport.update("insertOrganizerParticipant", params("id", Ids.newId(), "activityId", activityId, "userId", userId));
    for (int i = 0; i < coverUrls.size(); i++) {
      databaseSupport.update("insertActivityMedia", params("id", Ids.newId(), "activityId", activityId, "mediaUrl", coverUrls.get(i), "sortNo", i + 1));
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("activityId", activityId);
    data.put("reviewStatus", "pending");
    data.put("publishFeeAmount", Moneys.format(publishFee));
    data.put("publishOrderNo", orderNo);
    return data;
  }

  public Map<String, Object> list(Map<String, String> query) {
    return listInternal(query, false, null);
  }

  public Map<String, Object> nearby(Map<String, String> query) {
    return listInternal(query, true, null);
  }

  public Map<String, Object> listMine(String userId, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String reviewStatus = query.get("reviewStatus");
    String activityStatus = query.get("activityStatus");
    Map<String, Object> params = params("userId", userId, "offset", offset, "limit", pageSize + 1);
    if (reviewStatus != null && !reviewStatus.trim().isEmpty()) {
      params.put("reviewStatus", reviewStatus.trim());
    }
    if (activityStatus != null && !activityStatus.trim().isEmpty()) {
      params.put("activityStatus", activityStatus.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectMineActivities", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.activitySummary(row, null));
    }
    Map<String, Object> data = Cursors.pageResult(list, offset, pageSize, hasMore);
    Map<String, Object> unreadSummary = new LinkedHashMap<>();
    unreadSummary.put("pendingApplications", countNumber("countPendingApplicationsByOrganizer", params("userId", userId)).intValue());
    unreadSummary.put("endedActivities", countNumber("countEndedActivitiesByOrganizer", params("userId", userId)).intValue());
    data.put("unreadSummary", unreadSummary);
    return data;
  }

  public Map<String, Object> detail(String activityId, String currentUserId) {
    Map<String, Object> activity = findActivity(activityId);
    Map<String, Object> organizer = databaseSupport.findOne("selectOrganizerProfileByUserId", params("userId", activity.get("organizer_user_id")))
        .orElseThrow(() -> AppException.notFound("ORGANIZER_NOT_FOUND", "发起人不存在"));
    List<Map<String, Object>> mediaRows = databaseSupport.findAll("selectActivityMediaByActivityId", params("activityId", activityId));
    Map<String, Object> activityDetail = dtoAssembler.activitySummary(activity, null);
    activityDetail.put("description", activity.get("description"));
    activityDetail.put("organizer", dtoAssembler.organizer(organizer));
    activityDetail.put("mediaList", dtoAssembler.activityMediaList(mediaRows));

    Map<String, Object> myApplication = null;
    if (currentUserId != null) {
      myApplication = databaseSupport.findOne("selectMyApplicationForActivity", params("activityId", activityId, "userId", currentUserId))
          .map(dtoAssembler::application).orElse(null);
    }
    int paidParticipantCount = countNumber("countActivityParticipantsByStatuses",
        params("activityId", activityId, "statuses", Arrays.asList("paid", "organizer"))).intValue();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("activity", activityDetail);
    data.put("myApplication", myApplication);
    data.put("access", access(activity, currentUserId));
    data.put("participantSummary", dtoAssembler.participantSummary(
        ((Number) activity.get("participant_count")).intValue(),
        paidParticipantCount,
        String.valueOf(activity.get("organizer_user_id"))
    ));
    return data;
  }

  @Transactional
  public Map<String, Object> withdraw(String userId, String activityId) {
    Map<String, Object> activity = requireOwnedActivity(userId, activityId);
    databaseSupport.update("updateActivityWithdrawn", params("activityId", activityId));
    BigDecimal refundAmount = BigDecimal.ZERO;
    if (isTrue(activity.get("publish_fee_paid")) && activity.get("publish_fee_refunded_at") == null) {
      refundAmount = Moneys.of(activity.get("publish_fee_amount"));
      walletService.refundPublishFee(userId, activityId, refundAmount);
      databaseSupport.update("updateActivityPublishFeeRefundedNow", params("activityId", activityId));
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("activityId", activityId);
    data.put("activityStatus", "withdrawn");
    data.put("refundAmount", Moneys.format(refundAmount));
    return data;
  }

  @Transactional
  public Map<String, Object> finish(String userId, String activityId) {
    Map<String, Object> activity = requireOwnedActivity(userId, activityId);
    String status = String.valueOf(activity.get("activity_status"));
    if ("ended".equals(status) || "archived".equals(status)) {
      throw AppException.conflict("ACTIVITY_ALREADY_FINISHED", "活动已经结束");
    }
    Map<String, Object> settlement = databaseSupport.findOne("selectActivitySettlementByActivityId", params("activityId", activityId)).orElse(null);
    if (settlement == null) {
      settlement = settleActivityInternal(activity, "manual_finish", true);
    }
    String settlementStatus = settlement.get("settlementStatus") == null
        ? String.valueOf(settlement.get("settlement_status"))
        : String.valueOf(settlement.get("settlementStatus"));
    String settledAmount = settlement.get("settledAmount") == null
        ? Moneys.format(settlement.get("organizer_income_amount"))
        : String.valueOf(settlement.get("settledAmount"));
    String refundedAmount = settlement.get("refundedAmount") == null
        ? Moneys.format(settlement.get("publish_fee_refund_amount"))
        : String.valueOf(settlement.get("refundedAmount"));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("activityId", activityId);
    data.put("activityStatus", "ended");
    data.put("settlementStatus", settlementStatus);
    data.put("settledAmount", settledAmount);
    data.put("refundedAmount", refundedAmount);
    return data;
  }

  public Map<String, Object> adminReviewList(Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String reviewStatus = query.get("reviewStatus");
    Map<String, Object> params = params("offset", offset, "limit", pageSize + 1);
    if (reviewStatus != null && !reviewStatus.trim().isEmpty()) {
      params.put("reviewStatus", reviewStatus.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectAdminReviewActivities", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Map<String, Object> item = dtoAssembler.activitySummary(row, null);
      item.put("organizer", dtoAssembler.organizer(row));
      list.add(item);
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  @Transactional
  public Map<String, Object> adminReview(String activityId, Map<String, Object> body) {
    String decision = RequestValues.requiredString(body, "decision", "DECISION_REQUIRED", "缺少 decision");
    String reason = Optional.ofNullable(RequestValues.string(body, "reason")).orElse("");
    Map<String, Object> activity = findActivity(activityId);
    String reviewId = Ids.newId();
    databaseSupport.update("insertActivityReview",
        params("id", reviewId, "activityId", activityId, "reviewerAdminId", "01ADMIN000000000000000001", "decision", decision, "reason", reason));
    String reviewStatus = "approve".equalsIgnoreCase(decision) ? "approved" : "rejected";
    databaseSupport.update("updateActivityReviewStatus", params("activityId", activityId, "reviewStatus", reviewStatus));
    if ("rejected".equals(reviewStatus) && isTrue(activity.get("publish_fee_paid")) && activity.get("publish_fee_refunded_at") == null) {
      walletService.refundPublishFee(String.valueOf(activity.get("organizer_user_id")), activityId, Moneys.of(activity.get("publish_fee_amount")));
      databaseSupport.update("updateActivityPublishFeeRefundedNow", params("activityId", activityId));
    }
    notificationService.createNotification(
        String.valueOf(activity.get("organizer_user_id")),
        "activity",
        "approved".equals(reviewStatus) ? "活动审核通过" : "活动审核未通过",
        reason.isEmpty() ? "你的活动审核结果已更新" : reason,
        "activity",
        activityId,
        null
    );
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("reviewId", reviewId);
    data.put("activityId", activityId);
    data.put("reviewStatus", reviewStatus);
    return data;
  }

  @Transactional
  public Map<String, Object> settleEndedActivities(Integer batchSize, boolean dryRun) {
    int limit = batchSize == null ? 20 : Math.max(1, Math.min(batchSize, 100));
    List<Map<String, Object>> rows = databaseSupport.findAll("selectExpiredOpenActivities", params("limit", limit));
    int settledCount = 0;
    int refundCount = 0;
    for (Map<String, Object> row : rows) {
      if (dryRun) {
        settledCount++;
        continue;
      }
      Map<String, Object> settlement = settleActivityInternal(row, "cron_auto_finish", true);
      settledCount++;
      if (new BigDecimal(String.valueOf(settlement.get("refundedAmount"))).compareTo(BigDecimal.ZERO) > 0) {
        refundCount++;
      }
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("settledCount", settledCount);
    data.put("refundCount", refundCount);
    data.put("errorCount", 0);
    return data;
  }

  public Map<String, Object> access(Map<String, Object> activity, String currentUserId) {
    if (currentUserId == null || currentUserId.trim().isEmpty()) {
      return dtoAssembler.activityAccess(false, false, false, "请先登录");
    }
    String organizerUserId = String.valueOf(activity.get("organizer_user_id"));
    if (organizerUserId.equals(currentUserId)) {
      return dtoAssembler.activityAccess(false, true, true, "");
    }
    String reviewStatus = String.valueOf(activity.get("review_status"));
    String activityStatus = String.valueOf(activity.get("activity_status"));
    if (!"approved".equals(reviewStatus) || !"open".equals(activityStatus)) {
      return dtoAssembler.activityAccess(false, false, false, "活动暂不可加入");
    }
    boolean applied = countNumber("countActiveApplicationsForActivityAndUser",
        params("activityId", activity.get("id"), "userId", currentUserId)).intValue() > 0;
    boolean joined = countNumber("countActivityParticipantsByUserAndStatuses",
        params("activityId", activity.get("id"), "userId", currentUserId, "statuses", Arrays.asList("accepted", "paid", "organizer"))).intValue() > 0;
    if (joined) {
      return dtoAssembler.activityAccess(false, true, true, "");
    }
    boolean full = ((Number) activity.get("participant_count")).intValue() >= ((Number) activity.get("capacity")).intValue();
    if (applied) {
      return dtoAssembler.activityAccess(false, false, false, "你已经提交过报名申请");
    }
    if (full) {
      return dtoAssembler.activityAccess(false, false, false, "活动已满员");
    }
    return dtoAssembler.activityAccess(true, false, false, "");
  }

  public Map<String, Object> findActivity(String activityId) {
    return databaseSupport.findOne("selectActivityById", params("activityId", activityId))
        .orElseThrow(() -> AppException.notFound("ACTIVITY_NOT_FOUND", "活动不存在"));
  }

  public Map<String, Object> requireOwnedActivity(String userId, String activityId) {
    Map<String, Object> activity = findActivity(activityId);
    if (!userId.equals(String.valueOf(activity.get("organizer_user_id")))) {
      throw AppException.forbidden("ACTIVITY_OWNER_REQUIRED", "只有发起人可以操作该活动");
    }
    return activity;
  }

  @Transactional
  public Map<String, Object> settleActivityInternal(Map<String, Object> activity, String reason, boolean notifyOrganizer) {
    String activityId = String.valueOf(activity.get("id"));
    String organizerUserId = String.valueOf(activity.get("organizer_user_id"));
    int participantCount = countNumber("countActivityParticipantsByStatuses",
        params("activityId", activityId, "statuses", Arrays.asList("organizer", "accepted", "paid"))).intValue();
    int paidParticipantCount = countNumber("countActivityParticipantsByStatuses",
        params("activityId", activityId, "statuses", Arrays.asList("organizer", "paid"))).intValue();
    BigDecimal gross = Moneys.of(activity.get("accepted_fee_total"));
    BigDecimal organizerIncome = gross.multiply(appProperties.getFee().getCommissionRate());
    BigDecimal refundAmount = BigDecimal.ZERO;
    String settlementStatus = organizerIncome.compareTo(BigDecimal.ZERO) > 0 ? "settled" : "skipped";
    if (paidParticipantCount <= 1 && isTrue(activity.get("publish_fee_paid")) && activity.get("publish_fee_refunded_at") == null) {
      refundAmount = Moneys.of(activity.get("publish_fee_amount"));
      walletService.refundPublishFee(organizerUserId, activityId, refundAmount);
      databaseSupport.update("updateActivityPublishFeeRefundedNow", params("activityId", activityId));
    }
    if (organizerIncome.compareTo(BigDecimal.ZERO) > 0) {
      String settlementId = Ids.newId();
      databaseSupport.update("insertActivitySettlement",
          params("id", settlementId, "activityId", activityId, "organizerUserId", organizerUserId, "participantCount", participantCount,
              "paidParticipantCount", paidParticipantCount, "gross", gross, "income", organizerIncome, "refund", refundAmount,
              "settlementStatus", settlementStatus, "reason", reason, "ruleSnapshotJson", "{\"commissionRate\":\"0.50\"}"));
      walletService.creditSettlementIncome(organizerUserId, settlementId, activityId, organizerIncome);
      databaseSupport.update("insertCommissionRecord",
          params("id", Ids.newId(), "userId", organizerUserId, "activityId", activityId, "settlementId", settlementId, "baseAmount", gross,
              "rateValue", appProperties.getFee().getCommissionRate(), "amount", organizerIncome));
    } else {
      databaseSupport.update("insertActivitySettlement",
          params("id", Ids.newId(), "activityId", activityId, "organizerUserId", organizerUserId, "participantCount", participantCount,
              "paidParticipantCount", paidParticipantCount, "gross", gross, "income", organizerIncome, "refund", refundAmount,
              "settlementStatus", settlementStatus, "reason", reason, "ruleSnapshotJson", "{\"commissionRate\":\"0.50\"}"));
    }
    databaseSupport.update("updateActivityEnded", params("activityId", activityId, "settlementStatus", settlementStatus, "reason", reason));
    databaseSupport.update("updateActivityParticipantsEnded", params("activityId", activityId));
    if (notifyOrganizer) {
      notificationService.createNotification(organizerUserId, "commission", "活动已完成结算", "活动结束后的结算结果已经生成", "activity", activityId, null);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("settlementStatus", settlementStatus);
    result.put("settledAmount", Moneys.format(organizerIncome));
    result.put("refundedAmount", Moneys.format(refundAmount));
    return result;
  }

  private Map<String, Object> listInternal(Map<String, String> query, boolean radiusRequired, String organizerUserId) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String category = query.get("category");
    String keyword = query.get("keyword");
    String areaCode = query.get("areaCode");
    String timeScope = query.get("timeScope");
    boolean joinableOnly = "true".equalsIgnoreCase(query.get("joinableOnly"));
    BigDecimal latitude = query.get("latitude") == null ? null : new BigDecimal(query.get("latitude"));
    BigDecimal longitude = query.get("longitude") == null ? null : new BigDecimal(query.get("longitude"));
    Integer radiusMeters = parseInteger(query.get("radiusMeters"));
    Map<String, Object> params = params("offset", offset, "limit", pageSize + 40);
    if (organizerUserId != null) {
      params.put("organizerUserId", organizerUserId);
    }
    if (category != null && !category.trim().isEmpty()) {
      params.put("category", category.trim());
    }
    if (keyword != null && !keyword.trim().isEmpty()) {
      params.put("keyword", "%" + keyword.trim() + "%");
    }
    if (areaCode != null && !areaCode.trim().isEmpty()) {
      params.put("areaCode", areaCode.trim());
    }
    params.put("timeScope", timeScope);
    params.put("joinableOnly", joinableOnly);
    List<Map<String, Object>> rows = databaseSupport.findAll("selectPublicActivities", params);
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      Integer distance = null;
      if (latitude != null && longitude != null && row.get("latitude") != null && row.get("longitude") != null) {
        distance = distanceMeters(latitude.doubleValue(), longitude.doubleValue(),
            new BigDecimal(String.valueOf(row.get("latitude"))).doubleValue(),
            new BigDecimal(String.valueOf(row.get("longitude"))).doubleValue());
        if (radiusMeters != null && distance > radiusMeters) {
          continue;
        }
      } else if (radiusRequired) {
        continue;
      }
      list.add(dtoAssembler.activitySummary(row, distance));
      if (list.size() > pageSize) {
        break;
      }
    }
    boolean hasMore = list.size() > pageSize;
    if (hasMore) {
      list = list.subList(0, pageSize);
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  private void ensureUserActive(String userId) {
    Map<String, Object> credit = userService.findCredit(userId);
    if (credit.get("restricted_until") != null) {
      throw AppException.forbidden("USER_RESTRICTED", "当前账号已被限制操作");
    }
  }

  private Integer parseInteger(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return Integer.parseInt(value.trim());
  }

  private Number countNumber(String sql, Map<String, Object> params) {
    return databaseSupport.queryNumber(sql, params);
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

  private int distanceMeters(double lat1, double lng1, double lat2, double lng2) {
    double earthRadius = 6371000d;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return (int) Math.round(earthRadius * c);
  }

  private Map<String, Object> params(Object... values) {
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      params.put(String.valueOf(values[i]), values[i + 1]);
    }
    return params;
  }
}
