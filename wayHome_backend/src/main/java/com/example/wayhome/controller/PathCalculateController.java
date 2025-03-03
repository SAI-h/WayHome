package com.example.wayhome.controller;

import com.example.wayhome.service.PathCalculateService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.SolutionVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/calPath")
public class PathCalculateController {

    @Autowired
    private PathCalculateService pathCalculateService;

    @GetMapping
    public Result<?> PathCal(@NotNull @RequestParam("startStaID") Long startStaID,
                          @NotNull @RequestParam("endStaID") Long endStaID,
                          @NotNull @RequestParam("searchTime")
                            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") LocalDateTime searchTime,
                          @RequestParam(value = "scheme", defaultValue = "minTimeCost") String scheme,
                          @NotNull @RequestParam(value = "cityID") Integer cityID
                        ) {

        if("minTimeCost".equals(scheme)) {
            List<SolutionVO<Double>> solutionVOS = pathCalculateService.minTimeCostCal(startStaID, endStaID, searchTime, cityID);
            return Result.ok(solutionVOS);
        } else if ("minTransfer".equals(scheme)) {
            List<SolutionVO<Integer>> solutionVOS = pathCalculateService.minTransfer(startStaID, endStaID, searchTime, cityID);
            return Result.ok(solutionVOS);
        }
        return null;
    }

}
