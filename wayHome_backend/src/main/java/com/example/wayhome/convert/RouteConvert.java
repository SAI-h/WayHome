package com.example.wayhome.convert;

import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.entity.Route;
import com.example.wayhome.vo.RouteVO;

/**
 * 线路 DTO、实体与 VO 转换器。
 *
 * @author WayHome
 */
public final class RouteConvert {

    private RouteConvert() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将线路 DTO 转换为实体。
     *
     * @param routeDTO 线路 DTO
     * @return 线路实体
     */
    public static Route convertToDo(RouteDTO routeDTO) {
        Route route = new Route();
        route.setRouteID(routeDTO.getRouteID());
        route.setRouteName(routeDTO.getRouteName());
        route.setCityID(routeDTO.getCityID());
        route.setRemarks(routeDTO.getRemarks());
        route.setEditTime(routeDTO.getEditTime());
        return route;
    }

    /**
     * 将线路实体转换为 VO。
     *
     * @param route 线路实体
     * @return 线路 VO
     */
    public static RouteVO convertToVo(Route route) {
        RouteVO routeVO = new RouteVO();
        routeVO.setRouteID(route.getRouteID());
        routeVO.setRouteName(route.getRouteName());
        routeVO.setCityID(route.getCityID());
        routeVO.setRemarks(route.getRemarks());
        routeVO.setEditTime(route.getEditTime());
        return routeVO;
    }
}
