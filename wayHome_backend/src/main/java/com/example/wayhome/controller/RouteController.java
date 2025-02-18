package com.example.wayhome.controller;

import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.service.RouteService;
import com.example.wayhome.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

}
