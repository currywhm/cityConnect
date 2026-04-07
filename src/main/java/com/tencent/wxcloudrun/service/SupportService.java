package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Jsons;
import com.tencent.wxcloudrun.support.Numbers;
import com.tencent.wxcloudrun.support.RequestValues;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupportService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final Jsons jsons;
  private final NotificationService notificationService;

  public SupportService(DatabaseSupport databaseSupport, DtoAssembler dtoAssembler, Jsons jsons, NotificationService notificationService) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.jsons = jsons;
    this.notificationService = notificationService;
  }

  public Map<String, Object> askBot(Map<String, Object> body) {
    String question = RequestValues.requiredString(body, "question", "QUESTION_REQUIRED", "缺少 question");
    String normalized = question.toLowerCase();
    String tag = "general";
    String reply = "可以先把遇到的问题描述得更具体一点，我会尽量帮你定位。";
    boolean escalate = false;
    if (normalized.contains("支付") || normalized.contains("充值") || normalized.contains("订单")) {
      tag = "payment";
      reply = "和支付相关的问题，建议先检查订单状态是否已经 success；如果仍是 created/prepaying，可以稍后刷新或联系人工客服。";
      escalate = true;
    } else if (normalized.contains("登录") || normalized.contains("授权")) {
      tag = "login";
      reply = "登录问题通常和登录态过期或微信 code 失效有关，建议重新发起登录，再检查 sessionToken 是否更新。";
    } else if (normalized.contains("活动") || normalized.contains("报名")) {
      tag = "activity";
      reply = "活动相关问题可以先核对活动审核状态、名额和报名状态；如果是审核或结算异常，建议提交工单。";
      escalate = true;
    } else if (normalized.contains("举报") || normalized.contains("风控") || normalized.contains("限制")) {
      tag = "risk";
      reply = "风控限制通常和爽约、举报或敏感内容有关；如果你认为有误判，建议立即创建客服工单。";
      escalate = true;
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("reply", reply);
    data.put("tag", tag);
    data.put("escalateSuggested", escalate);
    return data;
  }

  @Transactional
  public Map<String, Object> createTicket(String userId, Map<String, Object> body) {
    String ticketId = Ids.newId();
    String ticketNo = Numbers.orderNo("TK");
    databaseSupport.update("insertSupportTicket",
        params("id", ticketId, "ticketNo", ticketNo, "userId", userId,
            "sourceConversationId", RequestValues.string(body, "sourceConversationId"),
            "category", RequestValues.requiredString(body, "category", "CATEGORY_REQUIRED", "缺少 category"),
            "tagsJson", jsons.toJson(RequestValues.stringList(body, "attachments")),
            "summary", RequestValues.requiredString(body, "summary", "SUMMARY_REQUIRED", "缺少 summary")));
    String messageId = Ids.newId();
    databaseSupport.update("insertSupportTicketMessage",
        params("id", messageId, "ticketId", ticketId, "senderId", userId,
            "contentText", RequestValues.requiredString(body, "message", "MESSAGE_REQUIRED", "缺少 message"),
            "attachmentsJson", jsons.toJson(RequestValues.stringList(body, "attachments"))));
    notificationService.createNotification(userId, "support", "工单已创建", "客服工单已提交成功", "support_ticket", ticketId, null);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("ticket", dtoAssembler.supportTicket(findTicket(ticketId)));
    data.put("firstMessage", dtoAssembler.supportTicketMessage(findTicketMessage(messageId)));
    return data;
  }

  public Map<String, Object> listMyTickets(String userId, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String status = query.get("status");
    Map<String, Object> params = params("userId", userId, "offset", offset, "limit", pageSize + 1);
    if (status != null && !status.trim().isEmpty()) {
      params.put("status", status.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectSupportTicketsByUserId", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.supportTicket(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  @Transactional
  public Map<String, Object> reply(String userId, String ticketId, Map<String, Object> body) {
    Map<String, Object> ticket = findTicket(ticketId);
    if (!userId.equals(String.valueOf(ticket.get("user_id")))) {
      throw AppException.forbidden("TICKET_OWNER_REQUIRED", "只有工单发起人可以回复");
    }
    String messageId = Ids.newId();
    databaseSupport.update("insertSupportTicketMessage",
        params("id", messageId, "ticketId", ticketId, "senderId", userId,
            "contentText", RequestValues.requiredString(body, "message", "MESSAGE_REQUIRED", "缺少 message"),
            "attachmentsJson", jsons.toJson(RequestValues.stringList(body, "attachments"))));
    databaseSupport.update("updateSupportTicketPendingAgent", params("ticketId", ticketId));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("message", dtoAssembler.supportTicketMessage(findTicketMessage(messageId)));
    data.put("ticketStatus", "pending_agent");
    return data;
  }

  public Map<String, Object> faqList(String keyword) {
    Map<String, Object> params = new HashMap<>();
    if (keyword != null && !keyword.trim().isEmpty()) {
      params.put("keyword", "%" + keyword.trim() + "%");
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectFaqArticles", params);
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.faq(row));
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("list", list);
    return data;
  }

  private Map<String, Object> findTicket(String ticketId) {
    return databaseSupport.findOne("selectSupportTicketById", params("ticketId", ticketId))
        .orElseThrow(() -> AppException.notFound("SUPPORT_TICKET_NOT_FOUND", "工单不存在"));
  }

  private Map<String, Object> findTicketMessage(String messageId) {
    return databaseSupport.findOne("selectSupportTicketMessageById", params("messageId", messageId))
        .orElseThrow(() -> AppException.notFound("SUPPORT_MESSAGE_NOT_FOUND", "工单消息不存在"));
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
