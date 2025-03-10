package com.example.wayhome.vo;


import lombok.Data;

@Data
public class PointVO {

    private Long routeID;

    private Long pointID;

    private Double pointLat;

    private Double pointLng;

    private Long staID;

    private String staName;

    private String staAddress;
}
