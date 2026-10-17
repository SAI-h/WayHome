package com.example.wayhome.controller;

import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.service.RouteService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.RouteVO;
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
 * 公交线路接口。
 *
 * @author WayHome
 */
@RestController
@RequestMapping("/route")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * 新增线路。
     *
     * @param routeDTO 线路信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> routeInsert(@Valid @RequestBody RouteDTO routeDTO) {
        routeService.routeInsert(routeDTO);
        return Result.ok(null);
    }

    /**
     * 查询线路。
     *
     * @param routeName 线路名称（可选）
     * @param cityId    城市 ID
     * @param lazyLoad  是否懒加载
     * @return 线路列表
     */
    @GetMapping
    public Result<?> routeQuery(@RequestParam(value = "routeName", required = false) String routeName,
                                @NotNull @RequestParam("cityID") Integer cityId,
                                @RequestParam(value = "lazyLoad", defaultValue = "false") Boolean lazyLoad) {
        return Result.ok(routeService.routeQuery(routeName, cityId, lazyLoad));
    }

    /**
     * 根据站点查询经过的线路。
     *
     * @param staId 站点 ID
     * @return 线路列表
     */
    @GetMapping("/{staID}")
    public Result<List<RouteVO>> routeQueryByStation(@NotNull @PathVariable("staID") Long staId) {
        return Result.ok(routeService.routeQueryByStation(staId));
    }

    /**
     * 更新线路。
     *
     * @param routeDTO 线路信息
     * @return 操作结果
     */
    @PatchMapping
    public Result<Void> routeUpdate(@Valid @RequestBody RouteDTO routeDTO) {
        routeService.routeUpdate(routeDTO);
        return Result.ok(null);
    }

    /**
     * 删除线路。
     *
     * @param routeId 线路 ID
     * @return 操作结果
     */
    @DeleteMapping("/{routeID}")
    public Result<Void> routeDelete(@NotNull @PathVariable("routeID") Long routeId) {
        routeService.routeDelete(routeId);
        return Result.ok(null);
    }
}
