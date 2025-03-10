package com.example.wayhome.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityVO implements Serializable {
    private Integer cityID;
    private String cityName;
    private Double cityLng;
    private Double cityLat;
}
