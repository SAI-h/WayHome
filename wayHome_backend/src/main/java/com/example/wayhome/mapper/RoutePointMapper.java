package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.entity.RoutePoint;
import com.example.wayhome.vo.PointVO;
import com.example.wayhome.vo.RouteQStationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RoutePointMapper extends BaseMapper<RoutePoint> {

    @Insert("<script>" +
                "INSERT INTO route_point (routeID, pointID, editTime) VALUES " +
                "<foreach collection='routePoints' item='routePoint' separator=','>" +
                    "(#{routePoint.routeID}, #{routePoint.pointID}, #{routePoint.editTime})" +
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

    @Select("SELECT r.routeID, r.routeName, p.pointID, p.pointLng, p.pointLat, s.staID, s.staName " +
            "FROM route r " +
            "JOIN route_point rp ON rp.routeID = r.routeID " +
            "JOIN Point p on p.pointID = rp.pointID " +
            "LEFT JOIN Station s on s.pointID = p.pointID " +
            "WHERE EXISTS (" +
            "    SELECT 1 " +
            "    FROM route_point rp2 " +
            "    JOIN station s ON s.pointID = rp2.pointID " +
            "    WHERE s.staID = #{staID} AND rp2.isDeleted = 0 AND rp2.routeID = r.routeID " +
            ")" +
            "AND rp.isDeleted = 0;")
    List<RouteQStationVO> routeQueryByStation(@Param("staID") Long staID);

}
