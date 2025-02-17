package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

}
