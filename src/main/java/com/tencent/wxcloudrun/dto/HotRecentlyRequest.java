package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class HotRecentlyRequest {
    private Integer id;

    private String name;

    private String sub;

    private Integer price;

    private String score;
}
