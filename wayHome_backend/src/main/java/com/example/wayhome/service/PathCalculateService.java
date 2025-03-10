package com.example.wayhome.service;

import com.example.wayhome.vo.SolutionVO;

import java.time.LocalDateTime;
import java.util.List;

public interface PathCalculateService {

    List<SolutionVO<Double>> minTimeCostCal(Long startStaID, Long endStaID, LocalDateTime searchTime, Integer cityID);

    List<SolutionVO<Integer>> minTransfer(Long startStaID, Long endStaID, LocalDateTime searchTime, Integer cityID);
}
