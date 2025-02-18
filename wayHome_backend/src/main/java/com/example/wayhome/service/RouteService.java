package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.entity.Route;

public interface RouteService extends IService<Route> {

    /**
     * 插入一条xi
     */
    void routeInsert(RouteDTO routeDTO);

}
