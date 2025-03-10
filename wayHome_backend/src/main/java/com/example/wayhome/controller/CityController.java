package com.example.wayhome.controller;

import com.example.wayhome.service.CityService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.CityVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("city")
public class CityController {

    @Resource
    private CityService cityService;

    @GetMapping
    private Result<List<CityVO>> getCityList() {
        List<CityVO> cityVOS = cityService.getCityList();
        return Result.ok(cityVOS);
    }

}
