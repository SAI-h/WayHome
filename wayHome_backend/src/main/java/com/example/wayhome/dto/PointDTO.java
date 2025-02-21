package com.example.wayhome.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointDTO {

    private Long pointID;

    private Long staID;

    @NotNull
    private Double pointLng;

    @NotNull
    private Double pointLat;

    @NotNull
    private LocalDateTime editTime;

}
