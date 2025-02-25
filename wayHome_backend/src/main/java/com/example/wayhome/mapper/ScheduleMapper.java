package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.vo.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Select("SELECT s.scheduleID, s.startTime, s.endTime, s.deInterval, " +
            "s.routeID, s.workdays, s.editTime, s.direction, s.remarks " +
            "FROM schedule s JOIN route r ON s.routeID = r.routeID " +
            "WHERE routeName = #{routeName} AND cityID = #{cityID} AND s.isDeleted = 0")
    List<Schedule> queryByName(@Param("routeName") String routeName, @Param("cityID") Integer cityID);
}
