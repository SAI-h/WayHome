package com.example.wayhome.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;

}
