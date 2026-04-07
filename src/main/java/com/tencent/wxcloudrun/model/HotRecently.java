package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotRecently implements Serializable {

    private Integer id;

    private String name;

    private String sub;

    private Integer price;

    private String score;

    private String img;
}
