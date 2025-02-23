package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route")
public class Route {

    @TableId(value = "routeID", type = IdType.AUTO)
    private Long routeID;

    @TableField("routeName")
    private String routeName;

    @TableField("cityID")
    private Integer cityID;

    @TableField("remarks")
    private String remarks;

    @TableField("editTime")
    private LocalDateTime editTime;

    @TableField("isDeleted")
    @TableLogic
    private Boolean isDeleted;

//    @TableField(exist = false)
//    private List<Schedule> schedules;
}
