package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.entity.Station;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StationMapper extends BaseMapper<Station> {

    @Insert("INSERT INTO station (staName, staAddress, pointID, editTime, cityID, remarks) " +
            "VALUES (#{station.staName}, #{station.staAddress}, " +
            "#{station.pointID}, #{station.editTime}, #{station.cityID}, #{station.remarks})")
    @Options(useGeneratedKeys = true, keyProperty = "staID")
    int stationInsert(@Param("station") Station station);


    @Select("SELECT s.staID, s.staName, s.staAddress, s.editTime, s.cityID, s.remarks, s.isDeleted," +
            "p.pointID, p.pointLat, p.pointLng " +
            "FROM station s " +
            "JOIN point p ON s.pointID = p.pointID " +
            "WHERE s.staName = #{staName}")
    @Results(id = "stationResultMap", value = {
            @Result(property = "point.pointID", column = "pointID"),
            @Result(property = "point.pointLat", column = "pointLat"),
            @Result(property = "point.pointLng", column = "pointLng")
    })
    Station stationQuery(@Param("staName") String staName);

}
