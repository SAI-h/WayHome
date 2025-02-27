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

import java.security.Key;
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

    private List<ScheduleVO> loadToCache(ListOperations<String, Object> ops, String key, List<Schedule> schedules) {
        List<ScheduleVO> scheduleVOList = new ArrayList<>();
        if(!schedules.isEmpty()) {
            for (Schedule schedule : schedules) {
                ScheduleVO scheduleVO = ScheduleConvert.ConvertToVO(schedule);
                scheduleVOList.add(scheduleVO);
                ops.rightPush(key, scheduleVO);
                redisTemplate.opsForValue().set("schedule:" + schedule.getScheduleID(), schedule.getRouteID());
            }
            redisTemplate.expire(key, expireTime, java.util.concurrent.TimeUnit.SECONDS);
        }
        return scheduleVOList;
    }


    private List<ScheduleVO> getScheduleVOSFromCache(ListOperations<String, Object> ops, String key) {
        if(ops.size(key) > 0) {
            List<Object> scheduleList = ops.range(key, 0, -1);
            List<ScheduleVO> scheduleVOS = new ArrayList<>();
            if(!scheduleList.isEmpty()) {
                for (Object o : scheduleList) {
                    ScheduleVO scheduleVO = (ScheduleVO) o;
                    scheduleVOS.add(scheduleVO);
                }
            }
            return scheduleVOS;
        }
        return null;
    }


    private void cacheFree(Long routeID, String schedule_key_2) {
        String key_route_reverse = "routeMap:routeID:" + routeID;
        String schedule_key = "schedule:routeID:" + routeID;
        Object routeIDToName = redisTemplate.opsForValue().get(key_route_reverse);

        if(routeIDToName != null) {
            String[] split = ((String)routeIDToName).split(":");
            String key_route = "routeMap:routeName:" + split[0] + ":cityID:" + split[1];
            redisTemplate.delete(key_route);
        }

        redisTemplate.delete(key_route_reverse);
        redisTemplate.delete(schedule_key);
        redisTemplate.delete(schedule_key_2);
    }

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
        String Key = "schedule:routeID:" + routeID;
        List<ScheduleVO> scheduleVOS = getScheduleVOSFromCache(ops, Key);
        if (scheduleVOS != null) return scheduleVOS;

        LambdaQueryWrapper<Schedule> scheduleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        scheduleLambdaQueryWrapper.eq(Schedule::getRouteID, routeID)
                .eq(Schedule::getIsDeleted, false);
        List<Schedule> schedules = scheduleMapper.selectList(scheduleLambdaQueryWrapper);
        return loadToCache(ops, Key, schedules);
    }

    @Override
    @Transactional
    public List<ScheduleVO> scheduleQueryByName(String routeName, Integer cityID) {
        ListOperations<String, Object> ops = redisTemplate.opsForList();

        // 由routeName和cityID查询routeID的信息
        String key_route = "routeMap:routeName:" + routeName + ":cityID:" + cityID;

        // 反序列化Long出现错误,需使用下面方式
        Object routeIDObj = redisTemplate.opsForValue().get(key_route);

        // 存在RouteName + cityID向RouteID的映射关系
        if (routeIDObj != null) {
            long routeID = ((Number)routeIDObj).longValue();
            String Key = "schedule:routeID:" + routeID;
            return getScheduleVOSFromCache(ops, Key);
        }

        List<Schedule> schedules = scheduleMapper.queryByName(routeName, cityID);
        long routeID = schedules.isEmpty() ? -1 : schedules.get(0).getRouteID() ;
        String key_route_reverse = "routeMap:routeID:" + routeID;
        String Key = "schedule:routeID:" + routeID;
        // 存储RouteID到RouteName + cityID的双向映射关系
        if(routeID != -1) {
            redisTemplate.opsForValue().set(key_route, routeID);
            redisTemplate.opsForValue().set(key_route_reverse, routeName + ":" + cityID);
            redisTemplate.expire(key_route, expireTime, java.util.concurrent.TimeUnit.SECONDS);
            redisTemplate.expire(key_route_reverse, expireTime, java.util.concurrent.TimeUnit.SECONDS);
            if(redisTemplate.hasKey(Key)) { // 不额外再次添加缓存
                return getScheduleVOSFromCache(ops, Key);
            }
        }
        return loadToCache(ops, Key, schedules);
    }

    @Override
    @Transactional
    public void scheduleUpdate(ScheduleDTO scheduleDTO) {
        // 更新数据库
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

        // 删除缓存
        Long routeID = scheduleDTO.getRouteID();
        String schedule_key_2 = "schedule:" + scheduleDTO.getScheduleID();
        cacheFree(routeID, schedule_key_2);
    }


    @Override
    @Transactional
    public void scheduleDelete(Long scheduleID) {
        // 更新数据库
        int resScheduleDelete = scheduleMapper.deleteById(scheduleID);
        if(resScheduleDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }

        // 删除缓存
        String schedule_key_2 = "schedule:" + scheduleID;
        Object scheduleToRouteID = redisTemplate.opsForValue().get(schedule_key_2);
        if(scheduleToRouteID != null) {
            long routeID = ((Number) scheduleToRouteID).longValue();
            cacheFree(routeID, schedule_key_2);
        }
    }

}
