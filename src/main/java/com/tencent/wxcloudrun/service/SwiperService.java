package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.SwiperRequest;
import com.tencent.wxcloudrun.model.Swiper;

import java.util.List;

public interface SwiperService {
    List<Swiper> getSwiper(SwiperRequest swiperRequest);

}
