package com.example.wayhome.vo;

import lombok.Data;

/**
 * 多表级联接收作用
 */
@Data
public class RouteQStationVO {

    private Long routeID;

    private String routeName;

    private Long pointID;

    private Double pointLng;

    private Double pointLat;

    private Long staID;

    private String staName;

}
