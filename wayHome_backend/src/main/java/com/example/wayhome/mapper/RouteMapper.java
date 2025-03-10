package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.entity.Route;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RouteMapper extends BaseMapper<Route> {

    @Update("UPDATE route SET isDeleted = 1 " +
            "WHERE routeID = #{routeID} " +
            "AND NOT EXISTS (" +
            "   SELECT 1 " +
            "   FROM schedule " +
            "WHERE routeID = #{routeID} AND isDeleted = 0)")
    int routeDelete(@Param("routeID") Long routeID);

}
