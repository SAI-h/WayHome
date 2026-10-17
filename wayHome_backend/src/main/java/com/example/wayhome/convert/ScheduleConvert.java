package com.example.wayhome.convert;

import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.vo.ScheduleVO;

/**
 * 班次 DTO、实体与 VO 转换器。
 *
 * @author WayHome
 */
public final class ScheduleConvert {

    private ScheduleConvert() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将班次 DTO 转换为实体。
     *
     * @param scheduleDTO 班次 DTO
     * @return 班次实体
     */
    public static Schedule convertToDo(ScheduleDTO scheduleDTO) {
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

    /**
     * 将班次实体转换为 VO。
     *
     * @param schedule 班次实体
     * @return 班次 VO
     */
    public static ScheduleVO convertToVo(Schedule schedule) {
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
