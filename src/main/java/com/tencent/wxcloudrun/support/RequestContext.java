package com.tencent.wxcloudrun.support;

import java.util.UUID;

public final class RequestContext {

  private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

  private RequestContext() {
  }

  public static String initRequestId() {
    String requestId = "req_" + UUID.randomUUID().toString().replace("-", "");
    REQUEST_ID_HOLDER.set(requestId);
    return requestId;
  }

  public static void setRequestId(String requestId) {
    REQUEST_ID_HOLDER.set(requestId);
  }

  public static String getRequestId() {
    String requestId = REQUEST_ID_HOLDER.get();
    if (requestId == null || requestId.trim().isEmpty()) {
      requestId = initRequestId();
    }
    return requestId;
  }

  public static void clear() {
    REQUEST_ID_HOLDER.remove();
  }
}
