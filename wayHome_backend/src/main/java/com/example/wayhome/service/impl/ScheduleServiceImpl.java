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
import com.example.wayhome.vo.CityVO;
import com.example.wayhome.vo.ScheduleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.expiration}")
    private Long expireTime;

    @Override
    @Transactional
    public void scheduleInsert(ScheduleDTO scheduleDTO) {
        Schedule schedule = ScheduleConvert.ConvertToDO(scheduleDTO);
        scheduleMapper.insert(schedule);
    }

    @Override
    @Transactional
    public List<ScheduleVO> scheduleQuery(Long routeID) {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        String Key = "schedule:route:" + routeID;
        if (ops.size("scheduleList") > 0) {
            List<Object> scheduleList = ops.range("scheduleList", 0, -1);
            List<ScheduleVO> scheduleVOS = new ArrayList<>();
            for (Object o : scheduleList) {
                ScheduleVO cityVO = (ScheduleVO) o;
                scheduleVOS.add(cityVO);
            }
            return scheduleVOS;
        }

        LambdaQueryWrapper<Schedule> scheduleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        scheduleLambdaQueryWrapper.eq(Schedule::getRouteID, routeID)
                .eq(Schedule::getIsDeleted, false);
        List<Schedule> schedules = scheduleMapper.selectList(scheduleLambdaQueryWrapper);

        if(!schedules.isEmpty()) {
            List<ScheduleVO> scheduleVOList = new ArrayList<>();
            for (Schedule schedule : schedules) {
                ScheduleVO scheduleVO = ScheduleConvert.ConvertToVO(schedule);
                scheduleVOList.add(scheduleVO);
                ops.rightPush("scheduleList", scheduleVO);
            }
            redisTemplate.expire("scheduleList", expireTime, java.util.concurrent.TimeUnit.SECONDS);
            return scheduleVOList;
        }
        return null;
    }


    @Override
    @Transactional
    public List<ScheduleVO> scheduleQueryByName(String routeName, Integer cityID) {
        List<Schedule> schedules = scheduleMapper.queryByName(routeName, cityID);
        List<ScheduleVO> scheduleVOS = new ArrayList<>();
        for(Schedule schedule : schedules) {
            scheduleVOS.add(ScheduleConvert.ConvertToVO(schedule));
        }
        return scheduleVOS;
    }

    @Override
    @Transactional
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
    @Transactional
    public void scheduleDelete(Long scheduleID) {
        int resScheduleDelete = scheduleMapper.deleteById(scheduleID);
        if(resScheduleDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }
    }

}
