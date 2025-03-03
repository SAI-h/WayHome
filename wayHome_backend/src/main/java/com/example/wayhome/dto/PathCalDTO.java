package com.example.wayhome.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PathCalDTO {

    @NotNull
    private Long startStaID;

    @NotNull
    private Long endStaID;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime searchTime;

    @NotNull
    private String scheme;

    @NotNull
    private Integer cityID;
}
