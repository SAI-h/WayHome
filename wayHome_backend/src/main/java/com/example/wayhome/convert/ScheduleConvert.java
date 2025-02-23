package com.example.wayhome.convert;

import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.vo.ScheduleVO;

public class ScheduleConvert {

    public static Schedule ConvertToDO(ScheduleDTO scheduleDTO) {
        Schedule schedule = new Schedule();
        schedule.setScheduleID(scheduleDTO.getScheduleID());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setDeInterval(scheduleDTO.getDeInterval());
        schedule.setRouteID(scheduleDTO.getRouteID());
        schedule.setWorkdays(scheduleDTO.getWorkdays());
        schedule.setEditTime(scheduleDTO.getEditTime());
        schedule.setDirection(scheduleDTO.getDirection());
        schedule.setRemarks(scheduleDTO.getRemarks());
        return schedule;
    }

    public static ScheduleVO ConvertToVO(Schedule schedule) {
        ScheduleVO scheduleVO = new ScheduleVO();
        scheduleVO.setScheduleID(schedule.getScheduleID());
        scheduleVO.setRouteID(schedule.getRouteID());
        scheduleVO.setDirection(schedule.getDirection());
        scheduleVO.setStartTime(schedule.getStartTime());
        scheduleVO.setEndTime(schedule.getEndTime());
        scheduleVO.setDeInterval(schedule.getDeInterval());
        scheduleVO.setWorkdays(schedule.getWorkdays());
        scheduleVO.setRemarks(schedule.getRemarks());
        scheduleVO.setEditTime(schedule.getEditTime());
        return scheduleVO;
    }
}
