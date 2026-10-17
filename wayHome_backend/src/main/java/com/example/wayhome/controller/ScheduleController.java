package com.example.wayhome.controller;

import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.service.ScheduleService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.ScheduleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公交班次接口。
 *
 * @author WayHome
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 新增班次。
     *
     * @param scheduleDTO 班次信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> scheduleInsert(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        scheduleService.scheduleInsert(scheduleDTO);
        return Result.ok(null);
    }

    /**
     * 根据线路 ID 查询班次。
     *
     * @param routeId 线路 ID
     * @return 班次列表
     */
    @GetMapping("/{routeID}")
    public Result<List<ScheduleVO>> scheduleQuery(@NotNull @PathVariable("routeID") Long routeId) {
        return Result.ok(scheduleService.scheduleQuery(routeId));
    }

    /**
     * 根据线路名称与城市查询班次。
     *
     * @param routeName 线路名称
     * @param cityId    城市 ID
     * @return 班次列表
     */
    @GetMapping
    public Result<List<ScheduleVO>> scheduleQueryByName(@NotNull @RequestParam("routeName") String routeName,
                                                        @NotNull @RequestParam("cityID") Integer cityId) {
        return Result.ok(scheduleService.scheduleQueryByName(routeName, cityId));
    }

    /**
     * 更新班次。
     *
     * @param scheduleDTO 班次信息
     * @return 操作结果
     */
    @PatchMapping
    public Result<Void> scheduleUpdate(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        scheduleService.scheduleUpdate(scheduleDTO);
        return Result.ok(null);
    }

    /**
     * 删除班次。
     *
     * @param scheduleId 班次 ID
     * @return 操作结果
     */
    @DeleteMapping("/{scheduleID}")
    public Result<Void> scheduleDelete(@NotNull @PathVariable("scheduleID") Long scheduleId) {
        scheduleService.scheduleDelete(scheduleId);
        return Result.ok(null);
    }
}
