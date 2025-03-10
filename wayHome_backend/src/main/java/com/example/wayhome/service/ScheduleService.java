package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.vo.ScheduleVO;

import java.util.List;

public interface ScheduleService extends IService<Schedule> {

    void scheduleInsert(ScheduleDTO scheduleDTO);

    List<ScheduleVO> scheduleQuery(Long routeID);

    void scheduleUpdate(ScheduleDTO scheduleDTO);

    void scheduleDelete(Long scheduleID);

    List<ScheduleVO> scheduleQueryByName(String routeName, Integer cityID);
}
