package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.AuthSupport;
import com.tencent.wxcloudrun.service.WalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WalletApiController {

  private final AuthSupport authSupport;
  private final WalletService walletService;

  public WalletApiController(AuthSupport authSupport, WalletService walletService) {
    this.authSupport = authSupport;
    this.walletService = walletService;
  }

  @GetMapping("/api/wallet/summary")
  public ApiResponse<Map<String, Object>> summary() {
    return ApiResponse.ok(walletService.getWalletSummary(authSupport.requireUserId()));
  }

  @GetMapping("/api/wallet/ledger")
  public ApiResponse<Map<String, Object>> ledger(@RequestParam Map<String, String> query) {
    return ApiResponse.ok(walletService.listLedger(authSupport.requireUserId(), query));
  }
}
