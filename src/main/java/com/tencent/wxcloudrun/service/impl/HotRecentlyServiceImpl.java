package com.tencent.wxcloudrun.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.tencent.wxcloudrun.dao.HotRecentlyMapper;
import com.tencent.wxcloudrun.dto.HotRecentlyRequest;
import com.tencent.wxcloudrun.model.HotRecently;
import com.tencent.wxcloudrun.service.HotRecentlyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class HotRecentlyServiceImpl implements HotRecentlyService {
    final HotRecentlyMapper hotRecentlyMapper;

    public HotRecentlyServiceImpl(HotRecentlyMapper hotRecentlyMapper) {
        this.hotRecentlyMapper = hotRecentlyMapper;
    }

    @Override
    public List<HotRecently> getHotRecently(HotRecentlyRequest recentlyRequest) {
        HotRecently hotRecently = new HotRecently();
        BeanUtils.copyProperties(recentlyRequest, hotRecently);
        return hotRecentlyMapper.getHotRecently(hotRecently);
    }
}
