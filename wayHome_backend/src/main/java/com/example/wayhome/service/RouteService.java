package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.entity.Route;
import com.example.wayhome.vo.RouteVO;

import java.util.List;

/**
 * 线路业务接口。
 *
 * @author WayHome
 */
public interface RouteService extends IService<Route> {

    /**
     * 新增线路。
     *
     * @param routeDTO 线路信息
     */
    void routeInsert(RouteDTO routeDTO);

    /**
     * 查询线路。
     *
     * @param routeName 线路名称（可选）
     * @param cityId    城市 ID
     * @param lazyLoad  是否懒加载
     * @return 线路列表
     */
    List<?> routeQuery(String routeName, Integer cityId, Boolean lazyLoad);

    /**
     * 更新线路。
     *
     * @param routeDTO 线路信息
     */
    void routeUpdate(RouteDTO routeDTO);

    /**
     * 删除线路。
     *
     * @param routeId 线路 ID
     */
    void routeDelete(Long routeId);

    /**
     * 根据站点查询经过的线路。
     *
     * @param staId 站点 ID
     * @return 线路列表
     */
    List<RouteVO> routeQueryByStation(Long staId);
}
