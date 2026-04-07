package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.Swiper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SwiperMapper {
    List<Swiper> getSwiper(Swiper swiper);
}
