package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.SwiperRequest;
import com.tencent.wxcloudrun.model.Swiper;
import com.tencent.wxcloudrun.service.SwiperService;
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
public class SwiperController {

    final SwiperService swiperService;
    final Logger logger;

    public SwiperController(@Autowired SwiperService swiperService) {
        this.swiperService = swiperService;
        this.logger = LoggerFactory.getLogger(SwiperController.class);
    }

    /**
     * 获取热图信息
     * @return API response json
     */
    @PostMapping(value = "/api/switchapp/getSwiper")
    ApiResponse getSwiper(@RequestBody(required = false) SwiperRequest request) {
        try{
            List<Swiper> swiper = swiperService.getSwiper(request);
            if (swiper.isEmpty()) {
                logger.error("swiper is empty");
            }
            return ApiResponse.ok(swiper);
        }catch (Exception e){
            return ApiResponse.error(e.getMessage());
        }
    }
}
