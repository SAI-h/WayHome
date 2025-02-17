package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
@TableName("route")
public class Route {

    @TableId(value = "routeID", type = IdType.AUTO)
    private Long routeID;

    @TableField("routeName")
    private String routeName;

    @TableField("startTime")
    private LocalTime startTime;

    @TableField("endTime")
    private LocalTime endTime;

    @TableField("deInterval")
    private Integer deInterval;

    @TableField("cityID")
    private Integer cityID;

    @TableField(exist = false)
    private City city;

    @TableField("remarks")
    private String remarks;

    @TableField("editTime")
    private LocalDateTime editTime;

    @TableField("idDeleted")
    @TableLogic
    private Boolean isDeleted;

    @TableField("workdays")
    private Map<String, Boolean> workdays;

}
