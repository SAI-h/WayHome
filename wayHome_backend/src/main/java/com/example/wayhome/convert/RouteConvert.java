package com.example.wayhome.convert;

import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.entity.Route;
import com.example.wayhome.vo.RouteVO;

public class RouteConvert {

    public static Route convertToDO(RouteDTO routeDTO) {
        Route route = new Route();
        route.setRouteID(routeDTO.getRouteID());
        route.setRouteName(routeDTO.getRouteName());
        route.setCityID(routeDTO.getCityID());
        route.setRemarks(routeDTO.getRemarks());
        route.setEditTime(routeDTO.getEditTime());
        return route;
    }

    public static RouteVO convertToVO(Route route) {
        RouteVO routeVO = new RouteVO();
        routeVO.setRouteID(route.getRouteID());
        routeVO.setRouteName(route.getRouteName());
        routeVO.setCityID(route.getCityID());
        routeVO.setRemarks(route.getRemarks());
        routeVO.setEditTime(route.getEditTime());
        return routeVO;
    }

}
