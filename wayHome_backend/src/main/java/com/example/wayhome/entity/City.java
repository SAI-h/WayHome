package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("city")
public class City {

    @TableId(value = "cityID", type = IdType.AUTO)
    private Integer cityID;

    @TableField("cityName")
    private String cityName;

    @TableField("cityLng")
    private Double cityLng;

    @TableField("cityLat")
    private Double cityLat;

    @TableField("isDeleted")
    @TableLogic
    private Boolean isDeleted;
}
