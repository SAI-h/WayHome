package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.vo.ScheduleVO;

import java.util.List;

/**
 * 班次业务接口。
 *
 * @author WayHome
 */
public interface ScheduleService extends IService<Schedule> {

    /**
     * 新增班次。
     *
     * @param scheduleDTO 班次信息
     */
    void scheduleInsert(ScheduleDTO scheduleDTO);

    /**
     * 根据线路 ID 查询班次。
     *
     * @param routeId 线路 ID
     * @return 班次列表
     */
    List<ScheduleVO> scheduleQuery(Long routeId);

    /**
     * 更新班次。
     *
     * @param scheduleDTO 班次信息
     */
    void scheduleUpdate(ScheduleDTO scheduleDTO);

    /**
     * 删除班次。
     *
     * @param scheduleId 班次 ID
     */
    void scheduleDelete(Long scheduleId);

    /**
     * 根据线路名称与城市查询班次。
     *
     * @param routeName 线路名称
     * @param cityId    城市 ID
     * @return 班次列表
     */
    List<ScheduleVO> scheduleQueryByName(String routeName, Integer cityId);
}
