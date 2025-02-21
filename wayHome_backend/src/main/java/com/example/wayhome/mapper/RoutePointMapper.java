package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.entity.RoutePoint;
import com.example.wayhome.vo.PointVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RoutePointMapper extends BaseMapper<RoutePoint> {

    @Insert("<script>" +
                "INSERT INTO route_point (routeID, pointID) VALUES " +
                "<foreach collection='routePoints' item='routePoint' separator=','>" +
                    "(#{routePoint.routeID}, #{routePoint.pointID})" +
                "</foreach>" +
            "</script>")
    int insertBatch(@Param("routePoints") List<RoutePoint> routePoints);

    @Select("<script>" +
                "SELECT rp.routeID, p.pointID, p.pointLng, p.pointLat, s.staID, s.staName, s.staAddress " +
                "FROM route_point rp " +
                "JOIN point p ON rp.pointID = p.pointID " +
                "LEFT JOIN station s ON s.staID = p.staID " +
                "WHERE rp.routeID IN " +
                "<foreach item='routeID' collection='routeIDList' open='(' separator=',' close=')'>" +
                    "#{routeID}" +
                "</foreach>" +
                "AND rp.isDeleted = 0" +
            "</script>")
    List<PointVO> pointInRouteQuery(List<Long> routeIDList);
}
