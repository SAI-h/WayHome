package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("point")
public class Point {

    @TableId(value = "pointID", type = IdType.AUTO)
    private Long pointID;

    @TableField("pointLng")
    private Double pointLng;

    @TableField("pointLat")
    private Double pointLat;

    @TableField("staID")
    private Long staID;

    @TableField(exist = false)
    private Station station;

    @TableField("editTime")
    private LocalDateTime editTime;

    @TableField("isDeleted")
    @TableLogic
    private Boolean isDeleted;

}
