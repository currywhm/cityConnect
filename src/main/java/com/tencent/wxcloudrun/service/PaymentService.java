package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Jsons;
import com.tencent.wxcloudrun.support.Moneys;
import com.tencent.wxcloudrun.support.Numbers;
import com.tencent.wxcloudrun.support.RequestValues;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final WalletService walletService;
  private final ApplicationService applicationService;
  private final Jsons jsons;
  private final AppProperties appProperties;

  public PaymentService(
      DatabaseSupport databaseSupport,
      DtoAssembler dtoAssembler,
      WalletService walletService,
      ApplicationService applicationService,
      Jsons jsons,
      AppProperties appProperties) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.walletService = walletService;
    this.applicationService = applicationService;
    this.jsons = jsons;
    this.appProperties = appProperties;
  }

  @Transactional
  public Map<String, Object> createRechargeOrder(String userId, Map<String, Object> body) {
    BigDecimal amount = RequestValues.decimal(body, "amount");
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw AppException.badRequest("AMOUNT_INVALID", "充值金额必须大于 0");
    }
    String clientRequestId = RequestValues.requiredString(body, "clientRequestId", "CLIENT_REQUEST_ID_REQUIRED", "缺少 clientRequestId");
    Map<String, Object> order = findOrderByClientRequestId(clientRequestId);
    if (order == null) {
      order = createOrder(userId, "recharge", null, amount, clientRequestId, "wechat_pay", "RC");
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("order", dtoAssembler.paymentOrder(order));
    data.put("paymentParams", paymentParams(String.valueOf(order.get("order_no")), amount));
    return data;
  }

  @Transactional
  public Map<String, Object> createAcceptOrder(String userId, Map<String, Object> body) {
    String applicationId = RequestValues.requiredString(body, "applicationId", "APPLICATION_ID_REQUIRED", "缺少 applicationId");
    Map<String, Object> application = applicationService.findApplication(applicationId);
    if (!userId.equals(String.valueOf(application.get("applicant_user_id")))) {
      throw AppException.forbidden("APPLICATION_OWNER_REQUIRED", "只有申请人可以支付进群费");
    }
    if (!"accepted_pending_payment".equals(String.valueOf(application.get("status")))) {
      throw AppException.conflict("APPLICATION_NOT_PAYABLE", "当前申请状态不可支付");
    }
    String clientRequestId = RequestValues.requiredString(body, "clientRequestId", "CLIENT_REQUEST_ID_REQUIRED", "缺少 clientRequestId");
    Map<String, Object> order = findOrderByClientRequestId(clientRequestId);
    if (order == null) {
      order = createOrder(userId, "accept_fee", applicationId, Moneys.of(application.get("accept_fee_amount")), clientRequestId, "wechat_pay", "AP");
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("order", dtoAssembler.paymentOrder(order));
    data.put("paymentParams", paymentParams(String.valueOf(order.get("order_no")), Moneys.of(order.get("amount"))));
    return data;
  }

  public Map<String, Object> queryOrder(String userId, String orderNo) {
    Map<String, Object> order = databaseSupport.findOne("selectPaymentOrderByOrderNo", params("orderNo", orderNo))
        .orElseThrow(() -> AppException.notFound("ORDER_NOT_FOUND", "订单不存在"));
    if (!userId.equals(String.valueOf(order.get("user_id")))) {
      throw AppException.forbidden("ORDER_OWNER_REQUIRED", "无权查看该订单");
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("order", dtoAssembler.paymentOrder(order));
    return data;
  }

  @Transactional
  public Map<String, Object> paymentCallback(Map<String, Object> body) {
    Object rawPayload = body.get("rawCallbackPayload");
    Map<String, Object> callback = rawPayload instanceof Map ? (Map<String, Object>) rawPayload : jsons.toMap(rawPayload);
    String orderNo = callback.get("orderNo") == null ? null : String.valueOf(callback.get("orderNo"));
    if (orderNo == null || orderNo.trim().isEmpty()) {
      orderNo = RequestValues.string(body, "orderNo");
    }
    if (orderNo == null || orderNo.trim().isEmpty()) {
      throw AppException.badRequest("ORDER_NO_REQUIRED", "支付回调缺少 orderNo");
    }
    Map<String, Object> order = databaseSupport.findOne("selectPaymentOrderByOrderNo", params("orderNo", orderNo))
        .orElseThrow(() -> AppException.notFound("ORDER_NOT_FOUND", "订单不存在"));
    if (!"success".equals(String.valueOf(order.get("status")))) {
      databaseSupport.update("updatePaymentOrderSuccess",
          params("orderId", order.get("id"), "callbackJson", jsons.toJson(callback), "transactionId",
              callback.get("transactionId") == null ? Ids.newToken() : callback.get("transactionId")));
      if ("recharge".equals(String.valueOf(order.get("order_type")))) {
        walletService.creditRecharge(String.valueOf(order.get("user_id")), String.valueOf(order.get("id")), Moneys.of(order.get("amount")));
      } else if ("accept_fee".equals(String.valueOf(order.get("order_type")))) {
        applicationService.markAcceptOrderPaid(String.valueOf(order.get("biz_id")), String.valueOf(order.get("id")));
      }
      order = databaseSupport.findOne("selectPaymentOrderById", params("id", order.get("id"))).orElse(order);
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("success", true);
    data.put("orderNo", orderNo);
    data.put("status", order.get("status"));
    return data;
  }

  @Transactional
  public Map<String, Object> applyWithdraw(String userId, Map<String, Object> body) {
    BigDecimal amount = RequestValues.decimal(body, "amount");
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw AppException.badRequest("AMOUNT_INVALID", "提现金额必须大于 0");
    }
    String clientRequestId = RequestValues.requiredString(body, "clientRequestId", "CLIENT_REQUEST_ID_REQUIRED", "缺少 clientRequestId");
    Map<String, Object> existing = databaseSupport.findOne("selectWithdrawRequestByClientRequestId", params("clientRequestId", clientRequestId)).orElse(null);
    if (existing != null) {
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("withdrawRequest", dtoAssembler.withdrawRequest(existing));
      return data;
    }
    String requestId = Ids.newId();
    String requestNo = Numbers.orderNo("WD");
    walletService.freezeWithdrawal(userId, requestId, amount);
    databaseSupport.update("insertWithdrawRequest",
        params("id", requestId, "requestNo", requestNo, "userId", userId, "amount", amount,
            "channel", RequestValues.requiredString(body, "channel", "CHANNEL_REQUIRED", "缺少 channel"),
            "clientRequestId", clientRequestId));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("withdrawRequest", dtoAssembler.withdrawRequest(
        databaseSupport.findOne("selectWithdrawRequestById", params("id", requestId)).orElseThrow(IllegalStateException::new)
    ));
    return data;
  }

  public Map<String, Object> listWithdrawMine(String userId, Map<String, String> query) {
    return listWithdraw(params("userId", userId), query);
  }

  public Map<String, Object> listWithdrawAdmin(Map<String, String> query) {
    return listWithdraw(new HashMap<>(), query);
  }

  @Transactional
  public Map<String, Object> adminReviewWithdraw(String withdrawRequestId, Map<String, Object> body) {
    Map<String, Object> withdraw = databaseSupport.findOne("selectWithdrawRequestById", params("id", withdrawRequestId))
        .orElseThrow(() -> AppException.notFound("WITHDRAW_REQUEST_NOT_FOUND", "提现申请不存在"));
    String decision = RequestValues.requiredString(body, "decision", "DECISION_REQUIRED", "缺少 decision");
    String nextStatus;
    if ("approve".equalsIgnoreCase(decision)) {
      nextStatus = "paid";
      walletService.approveWithdrawal(String.valueOf(withdraw.get("user_id")), withdrawRequestId, Moneys.of(withdraw.get("amount")));
    } else {
      nextStatus = "rejected";
      walletService.rejectWithdrawal(String.valueOf(withdraw.get("user_id")), withdrawRequestId, Moneys.of(withdraw.get("amount")));
    }
    databaseSupport.update("updateWithdrawRequestReview",
        params("id", withdrawRequestId, "status", nextStatus,
            "paidAt", "paid".equals(nextStatus) ? LocalDateTime.now() : null,
            "rejectReason", RequestValues.string(body, "remark")));
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("withdrawRequestId", withdrawRequestId);
    data.put("status", nextStatus);
    return data;
  }

  private Map<String, Object> createOrder(String userId, String orderType, String bizId, BigDecimal amount, String clientRequestId, String channel, String prefix) {
    String orderId = Ids.newId();
    String orderNo = Numbers.orderNo(prefix);
    databaseSupport.update("insertPaymentOrder",
        params("id", orderId, "orderNo", orderNo, "userId", userId, "orderType", orderType, "bizId", bizId,
            "amount", amount, "channel", channel, "clientRequestId", clientRequestId, "prepayId", "prepay_" + orderNo,
            "prepayPackage", "prepay_id=" + orderNo));
    return databaseSupport.findOne("selectPaymentOrderById", params("id", orderId)).orElseThrow(IllegalStateException::new);
  }

  private Map<String, Object> paymentParams(String orderNo, BigDecimal amount) {
    Map<String, Object> paymentParams = new LinkedHashMap<>();
    paymentParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
    paymentParams.put("nonceStr", Ids.newId());
    paymentParams.put("package", "prepay_id=" + orderNo);
    paymentParams.put("signType", "MD5");
    paymentParams.put("paySign", Ids.newToken());
    paymentParams.put("mockAmount", Moneys.format(amount));
    return paymentParams;
  }

  private Map<String, Object> listWithdraw(Map<String, Object> baseParams, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(parseInteger(query.get("pageSize")));
    String status = query.get("status");
    Map<String, Object> params = new HashMap<>(baseParams);
    params.put("offset", offset);
    params.put("limit", pageSize + 1);
    if (status != null && !status.trim().isEmpty()) {
      params.put("status", status.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectWithdrawRequests", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.withdrawRequest(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  private Map<String, Object> findOrderByClientRequestId(String clientRequestId) {
    return databaseSupport.findOne("selectPaymentOrderByClientRequestId", params("clientRequestId", clientRequestId)).orElse(null);
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
