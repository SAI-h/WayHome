package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route_point")
public class RoutePoint {

    @TableId(value = "linkID", type = IdType.AUTO)
    private Long linkID;

    @TableField("routeID")
    private Long routeID;

    @TableField("pointID")
    private Long pointID;

    @TableField("editTime")
    private LocalDateTime editTime;

    @TableField("isDeleted")
    @TableLogic
    private Boolean isDeleted;

}
