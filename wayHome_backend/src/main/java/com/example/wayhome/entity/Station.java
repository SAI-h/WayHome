package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("station")
public class Station {

    @TableId(value = "staID", type = IdType.AUTO)
    private Long staID;

    @TableField("staName")
    private String staName;

    @TableField("staAddress")
    private String staAddress;

    @TableField("pointID")
    private Long pointID;

    @TableField(exist = false)
    private Point point;

    @TableField("editTime")
    private LocalDateTime editTime;

    @TableField("cityID")
    private Integer cityID;

    @TableField("remarks")
    private String remarks;

    @TableField("isDeleted")
    @TableLogic
    private Boolean isDeleted;

}
