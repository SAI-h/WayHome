package com.example.wayhome.controller;


import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.service.ScheduleService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.ScheduleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping
    public Result<?> scheduleInsert(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        scheduleService.scheduleInsert(scheduleDTO);
        return Result.ok(null);
    }

    @GetMapping("/{routeID}")
    public Result<List<ScheduleVO>> scheduleQuery(@NotNull @PathVariable Long routeID) {
        List<ScheduleVO> scheduleVOS = scheduleService.scheduleQuery(routeID);
        return Result.ok(scheduleVOS);
    }

    @GetMapping
    public Result<List<ScheduleVO>> scheduleQueryByName(@NotNull @RequestParam("routeName") String routeName,
                                                        @NotNull @RequestParam("cityID") Integer cityID) {
        List<ScheduleVO> scheduleVOS = scheduleService.scheduleQueryByName(routeName, cityID);
        return Result.ok(scheduleVOS);
    }

    @PatchMapping
    public Result<?> scheduleUpdate(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        scheduleService.scheduleUpdate(scheduleDTO);
        return Result.ok(null);
    }

    @DeleteMapping("/{scheduleID}")
    public Result<?> scheduleDelete(@NotNull @PathVariable Long scheduleID) {
        scheduleService.scheduleDelete(scheduleID);
        return Result.ok(null);
    }

}
