package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.entity.Route;
import com.example.wayhome.vo.RouteVO;

import java.util.List;

public interface RouteService extends IService<Route> {

    /**
     * 插入一条线路
     */
    void routeInsert(RouteDTO routeDTO);

    List<?> routeQuery(String routeName, Integer cityID, Boolean lazyLoad);

    void routeUpdate(RouteDTO routeDTO);

    void routeDelete(Long routeID);

    List<RouteVO> routeQueryByStation(Long staID);
}
