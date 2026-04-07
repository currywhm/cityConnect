package com.tencent.wxcloudrun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private Auth auth = new Auth();
  private Chat chat = new Chat();
  private Fee fee = new Fee();

  public static class Auth {
    private String adminToken = "city-connect-admin";
    private String internalToken = "city-connect-internal";
    private Integer sessionDays = 7;

    public String getAdminToken() {
      return adminToken;
    }

    public void setAdminToken(String adminToken) {
      this.adminToken = adminToken;
    }

    public String getInternalToken() {
      return internalToken;
    }

    public void setInternalToken(String internalToken) {
      this.internalToken = internalToken;
    }

    public Integer getSessionDays() {
      return sessionDays;
    }

    public void setSessionDays(Integer sessionDays) {
      this.sessionDays = sessionDays;
    }
  }

  public static class Chat {
    private String wsUrl = "wss://example.com/city-connect/ws";

    public String getWsUrl() {
      return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
      this.wsUrl = wsUrl;
    }
  }

  public static class Fee {
    private BigDecimal signupBonusAmount = new BigDecimal("100.00");
    private BigDecimal publishFeeAmount = new BigDecimal("5.00");
    private BigDecimal acceptFeeAmount = new BigDecimal("5.00");
    private BigDecimal commissionRate = new BigDecimal("0.50");

    public BigDecimal getSignupBonusAmount() {
      return signupBonusAmount;
    }

    public void setSignupBonusAmount(BigDecimal signupBonusAmount) {
      this.signupBonusAmount = signupBonusAmount;
    }

    public BigDecimal getPublishFeeAmount() {
      return publishFeeAmount;
    }

    public void setPublishFeeAmount(BigDecimal publishFeeAmount) {
      this.publishFeeAmount = publishFeeAmount;
    }

    public BigDecimal getAcceptFeeAmount() {
      return acceptFeeAmount;
    }

    public void setAcceptFeeAmount(BigDecimal acceptFeeAmount) {
      this.acceptFeeAmount = acceptFeeAmount;
    }

    public BigDecimal getCommissionRate() {
      return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
      this.commissionRate = commissionRate;
    }
  }

  public Auth getAuth() {
    return auth;
  }

  public void setAuth(Auth auth) {
    this.auth = auth;
  }

  public Chat getChat() {
    return chat;
  }

  public void setChat(Chat chat) {
    this.chat = chat;
  }

  public Fee getFee() {
    return fee;
  }

  public void setFee(Fee fee) {
    this.fee = fee;
  }
}
