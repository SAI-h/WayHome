package com.example.wayhome.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
public class ScheduleDTO {

    private Long scheduleID;

    @NotNull
    private Long routeID;

    @NotNull
    private Boolean direction;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull
    private int deInterval;

    @NotNull
    private String workdays;

    private String remarks;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;
}
