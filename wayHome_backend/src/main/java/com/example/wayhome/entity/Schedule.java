package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
@TableName("schedule")
public class Schedule {

    @TableId(value = "scheduleID", type = IdType.AUTO)
    private Long scheduleID;

    @TableField("startTime")
    private LocalTime startTime;

    @TableField("endTime")
    private LocalTime endTime;

    @TableField("deInterval")
    private int deInterval;

    @TableField("routeID")
    private Long routeID;

    @TableField(exist = false)
    private Route route;

    @TableField("workdays")
    private Map<String, Boolean> workdays;

    @TableField("editTime")
    private LocalDateTime editTime;

    @TableField("isDeleted")
    @TableLogic
    private Boolean isDeleted;

}
