package com.example.wayhome.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;

}
