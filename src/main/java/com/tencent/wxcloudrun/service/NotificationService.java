package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Jsons;
import com.tencent.wxcloudrun.support.RequestValues;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final Jsons jsons;

  public NotificationService(DatabaseSupport databaseSupport, DtoAssembler dtoAssembler, Jsons jsons) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.jsons = jsons;
  }

  @Transactional
  public void createNotification(String userId, String type, String title, String content, String bizType, String bizId, Object extraJson) {
    databaseSupport.update("insertNotification",
        params("id", Ids.newId(), "userId", userId, "type", type, "title", title, "content", content, "bizType", bizType, "bizId", bizId,
            "extraJson", extraJson == null ? null : jsons.toJson(extraJson)));
  }

  public Map<String, Object> list(String userId, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String type = query.get("type");
    String readStatus = query.get("readStatus");
    Map<String, Object> params = params("userId", userId, "offset", offset, "limit", pageSize + 1);
    if (type != null && !type.trim().isEmpty()) {
      params.put("type", type.trim());
    }
    if (readStatus != null && !readStatus.trim().isEmpty()) {
      params.put("readStatus", readStatus.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectNotifications", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.notification(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  public Map<String, Object> unreadSummary(String userId) {
    List<Map<String, Object>> rows = databaseSupport.findAll("selectNotificationUnreadSummary", params("userId", userId));
    Map<String, Integer> counters = new HashMap<>();
    for (Map<String, Object> row : rows) {
      counters.put(String.valueOf(row.get("type")), ((Number) row.get("total")).intValue());
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("messageCount", counters.getOrDefault("message", 0));
    data.put("supportCount", counters.getOrDefault("support", 0));
    data.put("postCount", counters.getOrDefault("activity", 0));
    data.put("orderCount", counters.getOrDefault("order", 0));
    data.put("commissionCount", counters.getOrDefault("commission", 0));
    return data;
  }

  @Transactional
  public Map<String, Object> markRead(String userId, Map<String, Object> body) {
    List<String> ids = RequestValues.stringList(body, "ids");
    String type = RequestValues.string(body, "type");
    boolean allOfType = Boolean.TRUE.equals(RequestValues.bool(body, "allOfType"));
    int updatedCount;
    if (!ids.isEmpty()) {
      updatedCount = databaseSupport.update("updateNotificationsReadByIds", params("userId", userId, "ids", ids));
    } else if (allOfType && type != null && !type.isEmpty()) {
      updatedCount = databaseSupport.update("updateNotificationsReadByType", params("userId", userId, "type", type));
    } else {
      updatedCount = 0;
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("updatedCount", updatedCount);
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
