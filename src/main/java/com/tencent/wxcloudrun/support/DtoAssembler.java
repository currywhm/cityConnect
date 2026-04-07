package com.tencent.wxcloudrun.support;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DtoAssembler {

  private final Jsons jsons;

  public DtoAssembler(Jsons jsons) {
    this.jsons = jsons;
  }

  public Map<String, Object> user(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("nickname", string(row, "nickname"));
    dto.put("avatarUrl", string(row, "avatar_url"));
    dto.put("status", string(row, "status"));
    dto.put("registeredAt", dateTime(row.get("registered_at")));
    dto.put("lastLoginAt", dateTime(row.get("last_login_at")));
    return dto;
  }

  public Map<String, Object> profile(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("userId", string(row, "user_id"));
    dto.put("city", string(row, "city"));
    dto.put("bio", string(row, "bio"));
    dto.put("phoneCountryCode", string(row, "phone_country_code"));
    dto.put("phoneMasked", string(row, "phone_masked"));
    dto.put("gender", string(row, "gender"));
    dto.put("birthday", date(row.get("birthday")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> credit(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("userId", string(row, "user_id"));
    dto.put("creditScore", integer(row, "credit_score"));
    dto.put("noShowCount", integer(row, "no_show_count"));
    dto.put("withdrawCount", integer(row, "withdraw_count"));
    dto.put("completeCount", integer(row, "complete_count"));
    dto.put("reportCount", integer(row, "report_count"));
    dto.put("riskLevel", string(row, "risk_level"));
    dto.put("restrictedUntil", dateTime(row.get("restricted_until")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> wallet(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("totalBalance", decimal(row, "total_balance"));
    dto.put("withdrawableBalance", decimal(row, "withdrawable_balance"));
    dto.put("bonusBalance", decimal(row, "bonus_balance"));
    dto.put("frozenBalance", decimal(row, "frozen_balance"));
    dto.put("currency", string(row, "currency"));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> walletLedger(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("userId", string(row, "user_id"));
    dto.put("bizType", string(row, "biz_type"));
    dto.put("bizId", string(row, "biz_id"));
    dto.put("direction", string(row, "direction"));
    dto.put("currency", string(row, "currency"));
    dto.put("changeAmount", decimal(row, "change_amount"));
    dto.put("withdrawableChangeAmount", decimal(row, "withdrawable_change_amount"));
    dto.put("bonusChangeAmount", decimal(row, "bonus_change_amount"));
    dto.put("frozenChangeAmount", decimal(row, "frozen_change_amount"));
    dto.put("totalAfter", decimal(row, "total_after"));
    dto.put("withdrawableAfter", decimal(row, "withdrawable_after"));
    dto.put("bonusAfter", decimal(row, "bonus_after"));
    dto.put("frozenAfter", decimal(row, "frozen_after"));
    dto.put("remark", string(row, "remark"));
    dto.put("operatorType", string(row, "operator_type"));
    dto.put("operatorId", string(row, "operator_id"));
    dto.put("createdAt", dateTime(row.get("created_at")));
    return dto;
  }

  public Map<String, Object> activitySummary(Map<String, Object> row, Integer distanceMeters) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("organizerUserId", string(row, "organizer_user_id"));
    dto.put("title", string(row, "title"));
    dto.put("category", string(row, "category"));
    dto.put("reviewStatus", string(row, "review_status"));
    dto.put("activityStatus", string(row, "activity_status"));
    dto.put("settlementStatus", string(row, "settlement_status"));
    dto.put("locationName", string(row, "location_name"));
    dto.put("locationAddress", string(row, "location_address"));
    dto.put("latitude", decimalOrNull(row, "latitude"));
    dto.put("longitude", decimalOrNull(row, "longitude"));
    dto.put("areaCode", string(row, "area_code"));
    dto.put("coverUrl", string(row, "cover_url"));
    dto.put("feeMode", string(row, "fee_mode"));
    dto.put("feeAmount", decimalNullable(row, "fee_amount"));
    dto.put("feeDesc", string(row, "fee_desc"));
    dto.put("publishFeeAmount", decimal(row, "publish_fee_amount"));
    dto.put("publishFeePaid", bool(row, "publish_fee_paid"));
    dto.put("publishFeeRefundedAt", dateTime(row.get("publish_fee_refunded_at")));
    dto.put("acceptedFeeTotal", decimal(row, "accepted_fee_total"));
    dto.put("participantCount", integer(row, "participant_count"));
    dto.put("capacity", integer(row, "capacity"));
    dto.put("reviewedAt", dateTime(row.get("reviewed_at")));
    dto.put("endedAt", dateTime(row.get("ended_at")));
    dto.put("endedReason", string(row, "ended_reason"));
    dto.put("startAt", dateTime(row.get("start_at")));
    dto.put("endAt", dateTime(row.get("end_at")));
    dto.put("createdAt", dateTime(row.get("created_at")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    if (distanceMeters != null) {
      dto.put("distanceMeters", distanceMeters);
    }
    return dto;
  }

  public Map<String, Object> organizer(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("userId", string(row, "id"));
    dto.put("nickname", string(row, "nickname"));
    dto.put("avatarUrl", string(row, "avatar_url"));
    dto.put("city", string(row, "city"));
    dto.put("bio", string(row, "bio"));
    dto.put("creditScore", integer(row, "credit_score"));
    return dto;
  }

  public Map<String, Object> activityMedia(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("activityId", string(row, "activity_id"));
    dto.put("mediaType", string(row, "media_type"));
    dto.put("mediaUrl", string(row, "media_url"));
    dto.put("sortNo", integer(row, "sort_no"));
    return dto;
  }

  public Map<String, Object> application(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("activityId", string(row, "activity_id"));
    dto.put("applicantUserId", string(row, "applicant_user_id"));
    dto.put("applicantNickname", string(row, "applicant_nickname"));
    dto.put("applicantAvatarUrl", string(row, "applicant_avatar_url"));
    dto.put("quote", string(row, "quote"));
    dto.put("status", string(row, "status"));
    dto.put("acceptedByUserId", string(row, "accepted_by_user_id"));
    dto.put("decisionReason", string(row, "decision_reason"));
    dto.put("acceptFeeAmount", decimal(row, "accept_fee_amount"));
    dto.put("acceptFeePaid", bool(row, "accept_fee_paid"));
    dto.put("acceptFeeOrderId", string(row, "accept_fee_order_id"));
    dto.put("conversationId", string(row, "conversation_id"));
    dto.put("createdAt", dateTime(row.get("created_at")));
    dto.put("acceptedAt", dateTime(row.get("accepted_at")));
    dto.put("rejectedAt", dateTime(row.get("rejected_at")));
    dto.put("withdrawnAt", dateTime(row.get("withdrawn_at")));
    dto.put("noShowAt", dateTime(row.get("no_show_at")));
    dto.put("paidAt", dateTime(row.get("paid_at")));
    return dto;
  }

  public Map<String, Object> conversationSummary(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("bizType", string(row, "biz_type"));
    dto.put("bizId", string(row, "biz_id"));
    dto.put("title", string(row, "title"));
    dto.put("status", string(row, "status"));
    dto.put("lastMessageId", string(row, "last_message_id"));
    dto.put("lastMessageSeq", longValue(row, "last_message_seq"));
    dto.put("lastMessagePreview", string(row, "last_message_preview"));
    dto.put("lastMessageAt", dateTime(row.get("last_message_at")));
    dto.put("unreadCount", integer(row, "unread_count"));
    dto.put("canSend", bool(row, "can_send"));
    return dto;
  }

  public Map<String, Object> conversationMember(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("conversationId", string(row, "conversation_id"));
    dto.put("userId", string(row, "user_id"));
    dto.put("memberRole", string(row, "member_role"));
    dto.put("canSend", bool(row, "can_send"));
    dto.put("joinedAt", dateTime(row.get("joined_at")));
    dto.put("leftAt", dateTime(row.get("left_at")));
    dto.put("lastReadMessageId", string(row, "last_read_message_id"));
    dto.put("lastReadMessageSeq", longValue(row, "last_read_message_seq"));
    dto.put("lastReadAt", dateTime(row.get("last_read_at")));
    dto.put("unreadCount", integer(row, "unread_count"));
    return dto;
  }

  public Map<String, Object> message(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("conversationId", string(row, "conversation_id"));
    dto.put("messageSeq", longValue(row, "message_seq"));
    dto.put("clientMsgId", string(row, "client_msg_id"));
    dto.put("senderUserId", string(row, "sender_user_id"));
    dto.put("senderRole", string(row, "sender_role"));
    dto.put("msgType", string(row, "msg_type"));
    dto.put("contentText", string(row, "content_text"));
    dto.put("extJson", jsonValue(row.get("ext_json")));
    dto.put("moderationStatus", string(row, "moderation_status"));
    dto.put("moderationReason", string(row, "moderation_reason"));
    dto.put("clientSentAt", dateTime(row.get("client_sent_at")));
    dto.put("sentAt", dateTime(row.get("sent_at")));
    dto.put("recalledAt", dateTime(row.get("recalled_at")));
    return dto;
  }

  public Map<String, Object> notification(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("userId", string(row, "user_id"));
    dto.put("type", string(row, "type"));
    dto.put("title", string(row, "title"));
    dto.put("content", string(row, "content"));
    dto.put("bizType", string(row, "biz_type"));
    dto.put("bizId", string(row, "biz_id"));
    dto.put("readStatus", string(row, "read_status"));
    dto.put("readAt", dateTime(row.get("read_at")));
    dto.put("extraJson", jsonValue(row.get("extra_json")));
    dto.put("createdAt", dateTime(row.get("created_at")));
    return dto;
  }

  public Map<String, Object> paymentOrder(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("orderNo", string(row, "order_no"));
    dto.put("userId", string(row, "user_id"));
    dto.put("orderType", string(row, "order_type"));
    dto.put("bizId", string(row, "biz_id"));
    dto.put("amount", decimal(row, "amount"));
    dto.put("currency", string(row, "currency"));
    dto.put("status", string(row, "status"));
    dto.put("channel", string(row, "channel"));
    dto.put("transactionId", string(row, "transaction_id"));
    dto.put("paidAt", dateTime(row.get("paid_at")));
    dto.put("closedAt", dateTime(row.get("closed_at")));
    dto.put("refundedAt", dateTime(row.get("refunded_at")));
    dto.put("createdAt", dateTime(row.get("created_at")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> withdrawRequest(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("requestNo", string(row, "request_no"));
    dto.put("userId", string(row, "user_id"));
    dto.put("amount", decimal(row, "amount"));
    dto.put("status", string(row, "status"));
    dto.put("channel", string(row, "channel"));
    dto.put("reviewedAt", dateTime(row.get("reviewed_at")));
    dto.put("paidAt", dateTime(row.get("paid_at")));
    dto.put("rejectReason", string(row, "reject_reason"));
    dto.put("createdAt", dateTime(row.get("created_at")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> supportTicket(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("ticketNo", string(row, "ticket_no"));
    dto.put("userId", string(row, "user_id"));
    dto.put("sourceConversationId", string(row, "source_conversation_id"));
    dto.put("category", string(row, "category"));
    dto.put("priority", string(row, "priority"));
    dto.put("status", string(row, "status"));
    dto.put("tags", jsonStringList(row.get("tags_json")));
    dto.put("summary", string(row, "summary"));
    dto.put("assigneeAdminId", string(row, "assignee_admin_id"));
    dto.put("firstResponseAt", dateTime(row.get("first_response_at")));
    dto.put("resolvedAt", dateTime(row.get("resolved_at")));
    dto.put("createdAt", dateTime(row.get("created_at")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> supportTicketMessage(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("ticketId", string(row, "ticket_id"));
    dto.put("senderType", string(row, "sender_type"));
    dto.put("senderId", string(row, "sender_id"));
    dto.put("contentText", string(row, "content_text"));
    dto.put("attachmentsJson", jsonValue(row.get("attachments_json")));
    dto.put("createdAt", dateTime(row.get("created_at")));
    return dto;
  }

  public Map<String, Object> faq(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("title", string(row, "title"));
    dto.put("keywords", jsonStringList(row.get("keywords_json")));
    dto.put("answerText", string(row, "answer_text"));
    dto.put("status", string(row, "status"));
    dto.put("sortNo", integer(row, "sort_no"));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> report(Map<String, Object> row) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", string(row, "id"));
    dto.put("reporterUserId", string(row, "reporter_user_id"));
    dto.put("targetType", string(row, "target_type"));
    dto.put("targetId", string(row, "target_id"));
    dto.put("reasonType", string(row, "reason_type"));
    dto.put("contentText", string(row, "content_text"));
    dto.put("evidenceJson", jsonValue(row.get("evidence_json")));
    dto.put("status", string(row, "status"));
    dto.put("reviewerAdminId", string(row, "reviewer_admin_id"));
    dto.put("decision", string(row, "decision"));
    dto.put("decisionRemark", string(row, "decision_remark"));
    dto.put("reviewedAt", dateTime(row.get("reviewed_at")));
    dto.put("createdAt", dateTime(row.get("created_at")));
    dto.put("updatedAt", dateTime(row.get("updated_at")));
    return dto;
  }

  public Map<String, Object> activityAccess(boolean canApply, boolean canEnterConversation, boolean canSendMessage, String reason) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("canApply", canApply);
    dto.put("canEnterConversation", canEnterConversation);
    dto.put("canSendMessage", canSendMessage);
    dto.put("reason", reason == null ? "" : reason);
    return dto;
  }

  public Map<String, Object> participantSummary(int participantCount, int paidParticipantCount, String organizerUserId) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("participantCount", participantCount);
    dto.put("paidParticipantCount", paidParticipantCount);
    dto.put("organizerUserId", organizerUserId);
    return dto;
  }

  public Map<String, Object> wsToken(String wsUrl, String token, String expireAt) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("wsUrl", wsUrl);
    dto.put("token", token);
    dto.put("expireAt", expireAt);
    return dto;
  }

  public List<Map<String, Object>> activityMediaList(List<Map<String, Object>> rows) {
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(activityMedia(row));
    }
    return list;
  }

  private String string(Map<String, Object> row, String key) {
    if (row == null || row.get(key) == null) {
      return null;
    }
    return String.valueOf(row.get(key));
  }

  private Integer integer(Map<String, Object> row, String key) {
    if (row == null || row.get(key) == null) {
      return null;
    }
    return ((Number) row.get(key)).intValue();
  }

  private Long longValue(Map<String, Object> row, String key) {
    if (row == null || row.get(key) == null) {
      return 0L;
    }
    return ((Number) row.get(key)).longValue();
  }

  private Boolean bool(Map<String, Object> row, String key) {
    if (row == null || row.get(key) == null) {
      return false;
    }
    Object value = row.get(key);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue() == 1;
    }
    return Boolean.parseBoolean(String.valueOf(value));
  }

  private String decimal(Map<String, Object> row, String key) {
    return Moneys.format(row == null ? null : row.get(key));
  }

  private String decimalNullable(Map<String, Object> row, String key) {
    Object value = row == null ? null : row.get(key);
    return value == null ? null : Moneys.format(value);
  }

  private BigDecimal decimalOrNull(Map<String, Object> row, String key) {
    Object value = row == null ? null : row.get(key);
    if (value == null) {
      return null;
    }
    return new BigDecimal(String.valueOf(value));
  }

  private Object jsonValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Map || value instanceof List) {
      return value;
    }
    return jsons.readJson(String.valueOf(value));
  }

  private List<String> jsonStringList(Object value) {
    if (value == null) {
      return new ArrayList<>();
    }
    if (value instanceof List) {
      return (List<String>) value;
    }
    return jsons.readStringList(String.valueOf(value));
  }

  private String dateTime(Object value) {
    return Times.formatDateTime(value);
  }

  private String date(Object value) {
    if (value instanceof Timestamp) {
      return Times.formatDate(((Timestamp) value).toLocalDateTime().toLocalDate());
    }
    if (value instanceof LocalDate) {
      return Times.formatDate(value);
    }
    return value == null ? null : String.valueOf(value);
  }
}
