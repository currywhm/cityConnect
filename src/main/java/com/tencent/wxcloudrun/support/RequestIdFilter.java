package com.tencent.wxcloudrun.support;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.trim().isEmpty()) {
      requestId = RequestContext.initRequestId();
    } else {
      RequestContext.setRequestId(requestId.trim());
    }
    response.setHeader(REQUEST_ID_HEADER, RequestContext.getRequestId());
    try {
      filterChain.doFilter(request, response);
    } finally {
      RequestContext.clear();
    }
  }
}
