package com.example.wayhome.controller;

import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.service.StationService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.StationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Autowired
    private StationService stationService;

    @PostMapping
    public Result<?> stationInsert(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationInsert(stationDTO);
        return Result.ok(null);
    }

    @GetMapping
    public Result<List<StationVO>> stationQuery(@RequestParam(value = "staName", required = false) String staName,
                                                @NotNull @RequestParam("cityID") Integer cityID) {
        List<StationVO> stationVOList = stationService.stationQuery(staName, cityID);
        return Result.ok(stationVOList);
    }
//
//    @GetMapping
//    public Result<List<StationVO>> stationQueryAll(@NotNull @RequestParam("cityID") Integer cityID) {
//        List<StationVO> stationVOList = stationService.stationQueryAll(cityID);
//        return Result.ok(stationVOList);
//    }

    @PatchMapping
    public Result<?> stationUpdate(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationUpdate(stationDTO);
        return Result.ok(null);
    }

    @DeleteMapping
    public Result<?> stationDelete(@NotNull @RequestParam("staID") Long staID,
                                   @NotNull @RequestParam("pointID") Long pointID) {
        stationService.stationDelete(staID, pointID);
        return Result.ok(null);
    }

}
