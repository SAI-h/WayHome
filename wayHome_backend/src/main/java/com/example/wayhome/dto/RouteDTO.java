package com.example.wayhome.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RouteDTO {

    private Long routeID;

    @NotBlank
    private String routeName;

    @NotNull
    private List<PointDTO> points;

    @NotNull
    private Integer cityID;

    private String remarks;

    @NotNull
    private LocalDateTime editTime;

}
