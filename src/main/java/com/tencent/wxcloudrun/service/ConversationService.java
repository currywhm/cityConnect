package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final NotificationService notificationService;
  private final ActivityService activityService;
  private final Jsons jsons;
  private final AppProperties appProperties;

  public ConversationService(
      DatabaseSupport databaseSupport,
      DtoAssembler dtoAssembler,
      NotificationService notificationService,
      ActivityService activityService,
      Jsons jsons,
      AppProperties appProperties) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.notificationService = notificationService;
    this.activityService = activityService;
    this.jsons = jsons;
    this.appProperties = appProperties;
  }

  public Map<String, Object> list(String userId, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    List<Map<String, Object>> rows = databaseSupport.findAll("selectConversationListByUserId",
        params("userId", userId, "limit", pageSize + 1, "offset", offset));
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.conversationSummary(row));
    }
    Map<String, Object> data = Cursors.pageResult(list, offset, pageSize, hasMore);
    data.put("unreadSummary", notificationService.unreadSummary(userId));
    return data;
  }

  public Map<String, Object> detail(String userId, String conversationId) {
    Map<String, Object> membership = requireMembership(userId, conversationId);
    Map<String, Object> conversation = findConversation(conversationId);
    List<Map<String, Object>> memberRows = databaseSupport.findAll("selectConversationMembersByConversationId", params("conversationId", conversationId));
    List<Map<String, Object>> members = new ArrayList<>();
    for (Map<String, Object> memberRow : memberRows) {
      members.add(dtoAssembler.conversationMember(memberRow));
    }
    Map<String, Object> access = dtoAssembler.activityAccess(true, true, isTrue(membership.get("can_send")), "");
    if ("activity".equals(String.valueOf(conversation.get("biz_type"))) && conversation.get("biz_id") != null) {
      Map<String, Object> activity = activityService.findActivity(String.valueOf(conversation.get("biz_id")));
      access = activityService.access(activity, userId);
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("conversation", dtoAssembler.conversationSummary(mergeConversation(conversation, membership)));
    data.put("members", members);
    data.put("access", access);
    data.put("lastReadMessageSeq", ((Number) membership.get("last_read_message_seq")).longValue());
    return data;
  }

  public Map<String, Object> listMessages(String userId, String conversationId, Map<String, String> query) {
    requireMembership(userId, conversationId);
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String direction = query.getOrDefault("direction", "before");
    List<Map<String, Object>> rows = databaseSupport.findAll("selectMessagesByConversationId",
        params("conversationId", conversationId, "direction", direction, "limit", pageSize + 1, "offset", offset));
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.message(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  @Transactional
  public Map<String, Object> sendMessage(String userId, String conversationId, Map<String, Object> body) {
    Map<String, Object> membership = requireMembership(userId, conversationId);
    if (!isTrue(membership.get("can_send"))) {
      throw AppException.forbidden("MESSAGE_SEND_FORBIDDEN", "当前会话不可发送消息");
    }
    String text = RequestValues.requiredString(body, "text", "TEXT_REQUIRED", "缺少消息内容");
    String moderationStatus = moderateStatus(text);
    String moderationReason = "pass".equals(moderationStatus) ? null : "命中风控关键词";
    long nextSeq = databaseSupport.queryNumber("selectNextMessageSeq", params("conversationId", conversationId)).longValue();
    String messageId = Ids.newId();
    Object extJson = RequestValues.object(body, "extJson");
    databaseSupport.update("insertMessage",
        params("id", messageId, "conversationId", conversationId, "messageSeq", nextSeq,
            "clientMsgId", RequestValues.requiredString(body, "clientMsgId", "CLIENT_MSG_ID_REQUIRED", "缺少 clientMsgId"),
            "senderUserId", userId, "msgType", RequestValues.requiredString(body, "msgType", "MSG_TYPE_REQUIRED", "缺少 msgType"),
            "contentText", text, "extJson", extJson == null ? null : jsons.toJson(extJson),
            "moderationStatus", moderationStatus, "moderationReason", moderationReason));
    databaseSupport.update("updateConversationLastMessage",
        params("conversationId", conversationId, "lastMessageId", messageId, "lastMessageSeq", nextSeq, "preview", text.length() > 100 ? text.substring(0, 100) : text));
    databaseSupport.update("incrementConversationUnreadForOthers", params("conversationId", conversationId, "userId", userId));
    List<Map<String, Object>> recipients = databaseSupport.findAll("selectOtherConversationMemberUserIds",
        params("conversationId", conversationId, "userId", userId));
    for (Map<String, Object> recipient : recipients) {
      notificationService.createNotification(String.valueOf(recipient.get("user_id")), "message", "你有一条新消息", text, "conversation", conversationId, null);
    }
    Map<String, Object> message = databaseSupport.findOne("selectMessageById", params("id", messageId)).orElseThrow(IllegalStateException::new);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("message", dtoAssembler.message(message));
    return data;
  }

  @Transactional
  public Map<String, Object> markRead(String userId, String conversationId, Map<String, Object> body) {
    Map<String, Object> membership = requireMembership(userId, conversationId);
    Long lastReadMessageSeq = RequestValues.integer(body, "lastReadMessageSeq") == null
        ? ((Number) membership.get("last_read_message_seq")).longValue()
        : RequestValues.integer(body, "lastReadMessageSeq").longValue();
    Map<String, Object> lastMessage = databaseSupport.findOne("selectMessageIdByConversationAndSeq",
        params("conversationId", conversationId, "messageSeq", lastReadMessageSeq)).orElse(null);
    databaseSupport.update("updateConversationMemberRead",
        params("membershipId", membership.get("id"), "lastReadMessageId", lastMessage == null ? null : lastMessage.get("id"), "lastReadMessageSeq", lastReadMessageSeq));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("conversationId", conversationId);
    data.put("unreadCount", 0);
    return data;
  }

  public Map<String, Object> accessCheck(String userId, String activityId) {
    Map<String, Object> activity = activityService.findActivity(activityId);
    Map<String, Object> access = activityService.access(activity, userId);
    String conversationId = databaseSupport.findOne("selectConversationIdByActivityId", params("activityId", activityId))
        .map(row -> String.valueOf(row.get("id"))).orElse(null);
    access.put("conversationId", conversationId);
    return access;
  }

  public Map<String, Object> wsToken(String userId) {
    OffsetDateTime expireAt = Times.plusDays(1);
    return dtoAssembler.wsToken(appProperties.getChat().getWsUrl(), Ids.newToken(), Times.formatDateTime(expireAt));
  }

  @Transactional
  public Map<String, Object> expireActivityChats(Integer batchSize) {
    int limit = batchSize == null ? 20 : Math.max(1, Math.min(batchSize, 100));
    List<Map<String, Object>> rows = databaseSupport.findAll("selectClosableConversationIds", params("limit", limit));
    int count = 0;
    for (Map<String, Object> row : rows) {
      count += databaseSupport.update("updateConversationClosed", params("conversationId", row.get("id")));
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("closedConversationCount", count);
    return data;
  }

  private Map<String, Object> requireMembership(String userId, String conversationId) {
    return databaseSupport.findOne("selectConversationMemberByConversationAndUser", params("conversationId", conversationId, "userId", userId))
        .orElseThrow(() -> AppException.forbidden("CONVERSATION_MEMBER_REQUIRED", "当前用户不在会话内"));
  }

  private Map<String, Object> findConversation(String conversationId) {
    return databaseSupport.findOne("selectConversationById", params("conversationId", conversationId))
        .orElseThrow(() -> AppException.notFound("CONVERSATION_NOT_FOUND", "会话不存在"));
  }

  private Map<String, Object> mergeConversation(Map<String, Object> conversation, Map<String, Object> membership) {
    Map<String, Object> merged = new HashMap<>(conversation);
    merged.put("unread_count", membership.get("unread_count"));
    merged.put("can_send", membership.get("can_send"));
    return merged;
  }

  private String moderateStatus(String text) {
    String normalized = text.toLowerCase();
    if (normalized.contains("vx") || normalized.contains("微信") || normalized.contains("手机号") || normalized.contains("phone")) {
      return "review";
    }
    if (normalized.contains("辱骂") || normalized.contains("诈骗") || normalized.contains("色情")) {
      return "blocked";
    }
    return "pass";
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
