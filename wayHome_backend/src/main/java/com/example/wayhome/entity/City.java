package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("city")
public class City {

    @TableId
    private Integer cityID;

    private String cityName;

    private Double cityLng;

    private Double cityLat;

}
