package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.AppProperties;
import com.tencent.wxcloudrun.support.Cursors;
import com.tencent.wxcloudrun.support.AppException;
import com.tencent.wxcloudrun.support.DatabaseSupport;
import com.tencent.wxcloudrun.support.DtoAssembler;
import com.tencent.wxcloudrun.support.Ids;
import com.tencent.wxcloudrun.support.Moneys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

  private final DatabaseSupport databaseSupport;
  private final DtoAssembler dtoAssembler;
  private final AppProperties appProperties;

  public WalletService(DatabaseSupport databaseSupport, DtoAssembler dtoAssembler, AppProperties appProperties) {
    this.databaseSupport = databaseSupport;
    this.dtoAssembler = dtoAssembler;
    this.appProperties = appProperties;
  }

  public Map<String, Object> getWalletSummary(String userId) {
    ensureWallet(userId);
    return dtoAssembler.wallet(findWallet(userId));
  }

  public Map<String, Object> listLedger(String userId, Map<String, String> query) {
    int offset = Cursors.offset(query.get("cursor"));
    int pageSize = Cursors.pageSize(query.get("pageSize") == null ? null : Integer.parseInt(query.get("pageSize")));
    String bizType = query.get("bizType");
    Map<String, Object> params = params("userId", userId, "offset", offset, "limit", pageSize + 1);
    if (bizType != null && !bizType.trim().isEmpty()) {
      params.put("bizType", bizType.trim());
    }
    List<Map<String, Object>> rows = databaseSupport.findAll("selectWalletLedgerList", params);
    boolean hasMore = rows.size() > pageSize;
    if (hasMore) {
      rows = rows.subList(0, pageSize);
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      list.add(dtoAssembler.walletLedger(row));
    }
    return Cursors.pageResult(list, offset, pageSize, hasMore);
  }

  @Transactional
  public void grantSignupBonus(String userId) {
    ensureWallet(userId);
    BigDecimal amount = appProperties.getFee().getSignupBonusAmount();
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal total = Moneys.of(wallet.get("total_balance")).add(amount);
    BigDecimal bonus = Moneys.of(wallet.get("bonus_balance")).add(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "total", total, "bonus", bonus));
    createLedger(userId, "signup_bonus", userId, "in", amount, BigDecimal.ZERO, amount, BigDecimal.ZERO,
        total, Moneys.of(wallet.get("withdrawable_balance")), bonus, Moneys.of(wallet.get("frozen_balance")),
        "新用户赠送消费金", "system", null);
  }

  @Transactional
  public void creditRecharge(String userId, String orderId, BigDecimal amount) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal total = Moneys.of(wallet.get("total_balance")).add(amount);
    BigDecimal withdrawable = Moneys.of(wallet.get("withdrawable_balance")).add(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "total", total, "withdrawable", withdrawable));
    createLedger(userId, "recharge", orderId, "in", amount, amount, BigDecimal.ZERO, BigDecimal.ZERO,
        total, withdrawable, Moneys.of(wallet.get("bonus_balance")), Moneys.of(wallet.get("frozen_balance")),
        "余额充值到账", "system", null);
  }

  @Transactional
  public void chargePublishFee(String userId, String activityId, BigDecimal amount) {
    deductBalance(userId, "publish_fee", activityId, amount, "发布活动服务费");
  }

  @Transactional
  public void chargeAcceptFee(String userId, String applicationId, BigDecimal amount) {
    deductBalance(userId, "accept_fee", applicationId, amount, "报名通过进群费");
  }

  @Transactional
  public void refundPublishFee(String userId, String activityId, BigDecimal amount) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal total = Moneys.of(wallet.get("total_balance")).add(amount);
    BigDecimal bonus = Moneys.of(wallet.get("bonus_balance")).add(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "total", total, "bonus", bonus));
    createLedger(userId, "publish_fee_refund", activityId, "in", amount, BigDecimal.ZERO, amount, BigDecimal.ZERO,
        total, Moneys.of(wallet.get("withdrawable_balance")), bonus, Moneys.of(wallet.get("frozen_balance")),
        "活动发布费退回", "system", null);
  }

  @Transactional
  public void creditSettlementIncome(String userId, String settlementId, String activityId, BigDecimal amount) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal total = Moneys.of(wallet.get("total_balance")).add(amount);
    BigDecimal withdrawable = Moneys.of(wallet.get("withdrawable_balance")).add(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "total", total, "withdrawable", withdrawable));
    createLedger(userId, "settlement_income", settlementId, "in", amount, amount, BigDecimal.ZERO, BigDecimal.ZERO,
        total, withdrawable, Moneys.of(wallet.get("bonus_balance")), Moneys.of(wallet.get("frozen_balance")),
        "活动结算收益", "system", activityId);
  }

  @Transactional
  public void freezeWithdrawal(String userId, String withdrawRequestId, BigDecimal amount) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal withdrawable = Moneys.of(wallet.get("withdrawable_balance"));
    if (withdrawable.compareTo(amount) < 0) {
      throw AppException.badRequest("BALANCE_NOT_ENOUGH", "可提现余额不足");
    }
    BigDecimal nextWithdrawable = withdrawable.subtract(amount);
    BigDecimal nextFrozen = Moneys.of(wallet.get("frozen_balance")).add(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "withdrawable", nextWithdrawable, "frozen", nextFrozen));
    createLedger(userId, "withdraw_apply", withdrawRequestId, "out", BigDecimal.ZERO, amount.negate(), BigDecimal.ZERO, amount,
        Moneys.of(wallet.get("total_balance")), nextWithdrawable, Moneys.of(wallet.get("bonus_balance")), nextFrozen,
        "提现申请冻结金额", "user", userId);
  }

  @Transactional
  public void approveWithdrawal(String userId, String withdrawRequestId, BigDecimal amount) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal total = Moneys.of(wallet.get("total_balance")).subtract(amount);
    BigDecimal frozen = Moneys.of(wallet.get("frozen_balance")).subtract(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "total", total, "frozen", frozen));
    createLedger(userId, "withdraw_paid", withdrawRequestId, "out", amount, BigDecimal.ZERO, BigDecimal.ZERO, amount.negate(),
        total, Moneys.of(wallet.get("withdrawable_balance")), Moneys.of(wallet.get("bonus_balance")), frozen,
        "提现打款完成", "admin", "01ADMIN000000000000000001");
  }

  @Transactional
  public void rejectWithdrawal(String userId, String withdrawRequestId, BigDecimal amount) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal withdrawable = Moneys.of(wallet.get("withdrawable_balance")).add(amount);
    BigDecimal frozen = Moneys.of(wallet.get("frozen_balance")).subtract(amount);
    databaseSupport.update("updateWalletAccountBalances", params("userId", userId, "withdrawable", withdrawable, "frozen", frozen));
    createLedger(userId, "withdraw_reject", withdrawRequestId, "in", BigDecimal.ZERO, amount, BigDecimal.ZERO, amount.negate(),
        Moneys.of(wallet.get("total_balance")), withdrawable, Moneys.of(wallet.get("bonus_balance")), frozen,
        "提现驳回解冻", "admin", "01ADMIN000000000000000001");
  }

  private void deductBalance(String userId, String bizType, String bizId, BigDecimal amount, String remark) {
    ensureWallet(userId);
    Map<String, Object> wallet = findWallet(userId);
    BigDecimal total = Moneys.of(wallet.get("total_balance"));
    if (total.compareTo(amount) < 0) {
      throw AppException.badRequest("BALANCE_NOT_ENOUGH", "余额不足，请先充值");
    }
    BigDecimal withdrawable = Moneys.of(wallet.get("withdrawable_balance"));
    BigDecimal bonus = Moneys.of(wallet.get("bonus_balance"));
    BigDecimal bonusUsed = bonus.min(amount);
    BigDecimal withdrawableUsed = amount.subtract(bonusUsed);
    BigDecimal nextTotal = total.subtract(amount);
    BigDecimal nextWithdrawable = withdrawable.subtract(withdrawableUsed);
    BigDecimal nextBonus = bonus.subtract(bonusUsed);
    databaseSupport.update("updateWalletAccountBalances",
        params("userId", userId, "total", nextTotal, "withdrawable", nextWithdrawable, "bonus", nextBonus));
    createLedger(userId, bizType, bizId, "out", amount, withdrawableUsed.negate(), bonusUsed.negate(), BigDecimal.ZERO,
        nextTotal, nextWithdrawable, nextBonus, Moneys.of(wallet.get("frozen_balance")),
        remark, "user", userId);
  }

  private void ensureWallet(String userId) {
    if (findWalletOptional(userId) != null) {
      return;
    }
    databaseSupport.update("insertWalletAccount", params("userId", userId));
  }

  private Map<String, Object> findWallet(String userId) {
    Map<String, Object> wallet = findWalletOptional(userId);
    if (wallet == null) {
      throw AppException.notFound("WALLET_NOT_FOUND", "钱包不存在");
    }
    return wallet;
  }

  private Map<String, Object> findWalletOptional(String userId) {
    return databaseSupport.findOne("selectWalletByUserId", params("userId", userId)).orElse(null);
  }

  private void createLedger(
      String userId,
      String bizType,
      String bizId,
      String direction,
      BigDecimal changeAmount,
      BigDecimal withdrawableChange,
      BigDecimal bonusChange,
      BigDecimal frozenChange,
      BigDecimal totalAfter,
      BigDecimal withdrawableAfter,
      BigDecimal bonusAfter,
      BigDecimal frozenAfter,
      String remark,
      String operatorType,
      String operatorId) {
    databaseSupport.update("insertWalletLedger",
        params("id", Ids.newId(), "userId", userId, "bizType", bizType, "bizId", bizId, "direction", direction,
            "changeAmount", changeAmount, "withdrawableChange", withdrawableChange, "bonusChange", bonusChange,
            "frozenChange", frozenChange, "totalAfter", totalAfter, "withdrawableAfter", withdrawableAfter,
            "bonusAfter", bonusAfter, "frozenAfter", frozenAfter, "remark", remark, "operatorType", operatorType, "operatorId", operatorId));
  }

  private Map<String, Object> params(Object... values) {
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      params.put(String.valueOf(values[i]), values[i + 1]);
    }
    return params;
  }
}
