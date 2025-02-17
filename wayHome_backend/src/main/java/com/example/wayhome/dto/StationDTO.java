package com.example.wayhome.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StationDTO {

    private Long staID;

    @NotBlank
    private String staName;

    @NotBlank
    private String staAddress;

    private Long pointID;

    @NotNull
    private Double staLng;

    @NotNull
    private Double staLat;

    @NotNull
    private LocalDateTime editTime;

    private String remarks;

    @NotNull
    private Integer cityID;

}
