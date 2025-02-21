package com.example.wayhome.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RouteVO {

    private Long routeID;

    private String routeName;

    private String remarks;

    private Integer cityID;

    private LocalDateTime editTime;

    private List<PointVO> points;

}
