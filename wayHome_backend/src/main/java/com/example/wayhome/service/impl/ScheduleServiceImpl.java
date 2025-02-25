package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.convert.ScheduleConvert;
import com.example.wayhome.dto.ScheduleDTO;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.exception.BusinessException;
import com.example.wayhome.mapper.ScheduleMapper;
import com.example.wayhome.service.ScheduleService;
import com.example.wayhome.utils.ResultCodeEnum;
import com.example.wayhome.vo.ScheduleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public void scheduleInsert(ScheduleDTO scheduleDTO) {
        Schedule schedule = ScheduleConvert.ConvertToDO(scheduleDTO);
        scheduleMapper.insert(schedule);
    }

    @Override
    public List<ScheduleVO> scheduleQuery(Long routeID) {
        LambdaQueryWrapper<Schedule> scheduleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        scheduleLambdaQueryWrapper.eq(Schedule::getRouteID, routeID)
                .eq(Schedule::getIsDeleted, false);
        List<Schedule> schedules = scheduleMapper.selectList(scheduleLambdaQueryWrapper);

        List<ScheduleVO> scheduleVOList = new ArrayList<>();
        for(Schedule schedule : schedules) {
            scheduleVOList.add(ScheduleConvert.ConvertToVO(schedule));
        }
        return scheduleVOList;
    }


    @Override
    public List<ScheduleVO> scheduleQueryByName(String routeName, Integer cityID) {
        List<Schedule> schedules = scheduleMapper.queryByName(routeName, cityID);
        List<ScheduleVO> scheduleVOS = new ArrayList<>();
        for(Schedule schedule : schedules) {
            scheduleVOS.add(ScheduleConvert.ConvertToVO(schedule));
        }
        return scheduleVOS;
    }

    @Override
    public void scheduleUpdate(ScheduleDTO scheduleDTO) {
        LambdaUpdateWrapper<Schedule> scheduleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        scheduleLambdaUpdateWrapper.eq(Schedule::getScheduleID, scheduleDTO.getScheduleID())
                .set(Schedule::getStartTime, scheduleDTO.getStartTime())
                .set(Schedule::getEndTime, scheduleDTO.getEndTime())
                .set(Schedule::getDeInterval, scheduleDTO.getDeInterval())
                .set(Schedule::getWorkdays, scheduleDTO.getWorkdays())
                .set(Schedule::getRemarks, scheduleDTO.getRemarks())
                .set(Schedule::getDirection, scheduleDTO.getDirection())
                .set(Schedule::getEditTime, scheduleDTO.getEditTime());
        scheduleMapper.update(scheduleLambdaUpdateWrapper);
    }

    @Override
    public void scheduleDelete(Long scheduleID) {
        int resScheduleDelete = scheduleMapper.deleteById(scheduleID);
        if(resScheduleDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }
    }

}
