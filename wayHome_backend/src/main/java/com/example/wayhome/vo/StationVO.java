package com.example.wayhome.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class StationVO {

    private Long staID;

    private String staName;

    private String staAddress;

    private Long pointID;

    private Double staLng;

    private Double staLat;

    private String remarks;

    private Integer cityID;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;
}
