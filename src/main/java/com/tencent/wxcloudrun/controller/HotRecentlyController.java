package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.HotRecentlyRequest;
import com.tencent.wxcloudrun.model.HotRecently;
import com.tencent.wxcloudrun.service.HotRecentlyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * counter控制器
 */
@RestController

public class HotRecentlyController {

    final HotRecentlyService hotRecentlyService;
    final Logger logger;

    public HotRecentlyController(@Autowired HotRecentlyService hotRecentlyService) {
        this.hotRecentlyService = hotRecentlyService;
        this.logger = LoggerFactory.getLogger(HotRecentlyController.class);
    }

    /**
     * 获取热图信息
     * @return API response json
     */
    @PostMapping(value = "/api/switchapp/hotMgr")
    ApiResponse getHotRecently(@RequestBody(required = false)  HotRecentlyRequest request) {
        try{
            List<HotRecently> hotRecently = hotRecentlyService.getHotRecently(request);
            if (hotRecently.isEmpty()) {
                logger.error("hotRecently is empty");
            }
            return ApiResponse.ok(hotRecently);
        }catch (Exception e){
            return ApiResponse.error(e.getMessage());
        }
    }
}
