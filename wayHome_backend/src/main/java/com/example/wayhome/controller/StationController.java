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

    @PostMapping("/insert")
    public Result<?> stationInsert(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationInsert(stationDTO);
        return Result.ok(null);
    }

    @GetMapping("/query")
    public Result<StationVO> stationQuery(@RequestParam @NotBlank String staName) {
        StationVO stationVO = stationService.stationQuery(staName);
        return Result.ok(stationVO);
    }

    @PutMapping("/update")
    public Result<?> stationUpdate(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationUpdate(stationDTO);
        return Result.ok(null);
    }

    @DeleteMapping("/delete")
    public Result<?> stationDelete(@RequestParam @NotNull Long staID, @RequestParam @NotNull Long pointID) {
        stationService.stationDelete(staID, pointID);
        return Result.ok(null);
    }

}
