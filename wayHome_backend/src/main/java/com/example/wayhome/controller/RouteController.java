package com.example.wayhome.controller;

import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.service.RouteService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.RouteVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping
    public Result<?> routeInsert(@Valid @RequestBody RouteDTO routeDTO) {
        routeService.routeInsert(routeDTO);
        return Result.ok(null);
    }

    @GetMapping
    public Result<List<RouteVO>> routeQuery(@RequestParam(value = "routeName", required = false) String routeName,
                                            @NotNull @RequestParam("cityID") Integer cityID) {
        List<RouteVO> routeVOS = routeService.routeQuery(routeName, cityID);
        return Result.ok(routeVOS);
    }

}
