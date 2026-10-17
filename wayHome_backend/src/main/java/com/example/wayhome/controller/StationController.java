package com.example.wayhome.controller;

import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.service.StationService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.StationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公交站点接口。
 *
 * @author WayHome
 */
@RestController
@RequestMapping("/station")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    /**
     * 新增站点。
     *
     * @param stationDTO 站点信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> stationInsert(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationInsert(stationDTO);
        return Result.ok(null);
    }

    /**
     * 查询站点。
     *
     * @param staName 站点名称（可选）
     * @param cityId  城市 ID
     * @return 站点列表
     */
    @GetMapping
    public Result<List<StationVO>> stationQuery(@RequestParam(value = "staName", required = false) String staName,
                                                @NotNull @RequestParam("cityID") Integer cityId) {
        return Result.ok(stationService.stationQuery(staName, cityId));
    }

    /**
     * 更新站点。
     *
     * @param stationDTO 站点信息
     * @return 操作结果
     */
    @PatchMapping
    public Result<Void> stationUpdate(@Valid @RequestBody StationDTO stationDTO) {
        stationService.stationUpdate(stationDTO);
        return Result.ok(null);
    }

    /**
     * 删除站点。
     *
     * @param staId   站点 ID
     * @param pointId 坐标点 ID
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> stationDelete(@NotNull @RequestParam("staID") Long staId,
                                      @NotNull @RequestParam("pointID") Long pointId) {
        stationService.stationDelete(staId, pointId);
        return Result.ok(null);
    }
}
