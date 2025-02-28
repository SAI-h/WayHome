package com.example.wayhome.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/calPath")
public class PathCalculateController {

    @GetMapping
    public void PathCal(@NotNull @RequestParam("startStaID") Long startStaID,
                        @NotNull @RequestParam("endStaID") Long endStaID,
                        @NotNull @RequestParam("searchTime")
                            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") LocalDateTime searchTime,
                        @RequestParam(value = "scheme", defaultValue = "minTimeCost") String scheme) {


    }

}
