package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.HotRecently;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotRecentlyMapper {
    List<HotRecently> getHotRecently(HotRecently hotRecently);
}
