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

    @GetMapping("/{staName}")
    public Result<StationVO> stationQuery(@PathVariable @NotBlank String staName) {
        StationVO stationVO = stationService.stationQuery(staName);
        return Result.ok(stationVO);
    }

    @PatchMapping
    public Result<?> stationUpdate(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationUpdate(stationDTO);
        return Result.ok(null);
    }

    @DeleteMapping("/{staID}/{pointID}")
    public Result<?> stationDelete(@PathVariable @NotNull Long staID, @PathVariable @NotNull Long pointID) {
        stationService.stationDelete(staID, pointID);
        return Result.ok(null);
    }

}
