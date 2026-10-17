package com.example.wayhome.controller;

import com.example.wayhome.service.CityService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.CityVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 城市信息接口。
 *
 * @author WayHome
 */
@RestController
@RequestMapping("/city")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    /**
     * 获取城市列表。
     *
     * @return 城市列表
     */
    @GetMapping
    public Result<List<CityVO>> getCityList() {
        return Result.ok(cityService.getCityList());
    }
}
