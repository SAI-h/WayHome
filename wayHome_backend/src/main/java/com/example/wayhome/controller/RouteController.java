package com.example.wayhome.controller;

import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.service.RouteService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.RouteVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/route")
public class RouteController {

    @Resource
    private RouteService routeService;

    @PostMapping
    public Result<?> routeInsert(@Valid @RequestBody RouteDTO routeDTO) {
        routeService.routeInsert(routeDTO);
        return Result.ok(null);
    }

    @GetMapping
    public Result<?> routeQuery(@RequestParam(value = "routeName", required = false) String routeName,
                                            @NotNull @RequestParam("cityID") Integer cityID,
                                            @RequestParam(value = "lazyLoad", defaultValue = "false") Boolean lazyLoad) {
        List<?> routes = routeService.routeQuery(routeName, cityID, lazyLoad);
        return Result.ok(routes);
    }

    @GetMapping("/{staID}")
    public Result<List<RouteVO>> routeQueryByStation(@NotNull @PathVariable(value = "staID") Long staID) {
        List<RouteVO> routes = routeService.routeQueryByStation(staID);
        return Result.ok(routes);
    }

    @PatchMapping
    public Result<?> routeUpdate(@Valid @RequestBody RouteDTO routeDTO) {
        routeService.routeUpdate(routeDTO);
        return Result.ok(null);
    }

    @DeleteMapping("/{routeID}")
    public Result<?> routeDelete(@NotNull @PathVariable("routeID") Long routeID) {
        routeService.routeDelete(routeID);
        return Result.ok(null);
    }

}
