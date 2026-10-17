package com.example.wayhome.service;

import com.example.wayhome.vo.SolutionVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 路径规划业务接口。
 *
 * @author WayHome
 */
public interface PathCalculateService {

    /**
     * 计算最短耗时路径。
     *
     * @param startStaId 起始站点 ID
     * @param endStaId   终点站点 ID
     * @param searchTime 出发时间
     * @param cityId     城市 ID
     * @return 路径方案列表
     */
    List<SolutionVO<Double>> minTimeCostCal(Long startStaId, Long endStaId, LocalDateTime searchTime, Integer cityId);

    /**
     * 计算最少换乘路径。
     *
     * @param startStaId 起始站点 ID
     * @param endStaId   终点站点 ID
     * @param searchTime 出发时间
     * @param cityId     城市 ID
     * @return 路径方案列表
     */
    List<SolutionVO<Integer>> minTransfer(Long startStaId, Long endStaId, LocalDateTime searchTime, Integer cityId);
}
