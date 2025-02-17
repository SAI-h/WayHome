package com.example.wayhome.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class StationVO {

    private Long staID;

    private String staName;

    private String staAddress;

    private Double staLng;

    private Double staLat;

    private String remarks;

    private Integer cityID;

    private LocalTime editTime;

}
