package com.tencent.wxcloudrun.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Cursors {

  private Cursors() {
  }

  public static int offset(String cursor) {
    if (cursor == null || cursor.trim().isEmpty()) {
      return 0;
    }
    try {
      return Math.max(Integer.parseInt(cursor), 0);
    } catch (NumberFormatException exception) {
      return 0;
    }
  }

  public static int pageSize(Integer pageSize) {
    if (pageSize == null) {
      return 20;
    }
    return Math.max(1, Math.min(pageSize, 50));
  }

  public static Map<String, Object> pageResult(List<?> list, int offset, int pageSize, boolean hasMore) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("list", list);
    result.put("nextCursor", hasMore ? String.valueOf(offset + pageSize) : "");
    result.put("hasMore", hasMore);
    return result;
  }
}
