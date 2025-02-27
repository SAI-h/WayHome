package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.MedianValueSerialization.ListSerialization;
import com.example.wayhome.convert.RouteConvert;
import com.example.wayhome.dto.PointDTO;
import com.example.wayhome.dto.RouteDTO;
import com.example.wayhome.entity.Route;
import com.example.wayhome.entity.RoutePoint;
import com.example.wayhome.exception.BusinessException;
import com.example.wayhome.mapper.PointMapper;
import com.example.wayhome.mapper.RouteMapper;
import com.example.wayhome.mapper.RoutePointMapper;
import com.example.wayhome.service.RouteService;
import com.example.wayhome.utils.ResultCodeEnum;
import com.example.wayhome.vo.PointVO;
import com.example.wayhome.vo.RouteQStationVO;
import com.example.wayhome.vo.RouteVO;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private RoutePointMapper routePointMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Value("${redis.expiration}")
    private Long expireTime;

    @Override
    @Transactional
    public void routeInsert(RouteDTO routeDTO) {
        // 插入线路
        Route route = RouteConvert.convertToDO(routeDTO);
        int resRouteInsert = routeMapper.insert(route);

        if(resRouteInsert != 1) {
            throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
        }

        // 绑定线路和绘制点
        List<PointDTO> points = routeDTO.getPoints();
        List<PointDTO> drawPoints = points.stream().filter(point -> point.getPointID() == null).toList();

        // 批量插入绘图点集(pointID为空的才需要插入)
        if(!drawPoints.isEmpty()) {
            int resPointInsert = pointMapper.insertBatch(drawPoints);

            if (resPointInsert != drawPoints.size()) {
                throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
            }
        }

        int drawPointIdx = 0;
        for (PointDTO point : points) {
            if(point.getPointID() == null) {
                point.setPointID(drawPoints.get(drawPointIdx).getPointID());
                drawPointIdx++;
            }
        }

        // 向route_point表插入数据
        List<RoutePoint> routePoints = new ArrayList<>();
        for (PointDTO point : points) {
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteID(route.getRouteID());
            routePoint.setPointID(point.getPointID());
            routePoint.setEditTime(routeDTO.getEditTime());
            routePoints.add(routePoint);
        }

        int resRoutePointInsert = routePointMapper.insertBatch(routePoints);

        if(resRoutePointInsert != routePoints.size()) {
            throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
        }
    }

    @Override
    @Transactional
    public List<?> routeQuery(String routeName, Integer cityID, Boolean lazyLoad) {
        if (lazyLoad) { // 懒加载，只获取路线名
            // 缓存读取数据
            if(redisTemplate.hasKey("routeNameList")) {
                ListSerialization<String> routeNameList = (ListSerialization<String>) redisTemplate.opsForValue().get("routeNameList");
                return routeNameList.get_data();
            }

            // 数据库加载数据
            LambdaQueryWrapper<Route> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Route::getRouteName)
                    .eq(Route::getCityID, cityID);
            List<Route> routes = routeMapper.selectList(queryWrapper);
            List<String> list = routes.stream().map(Route::getRouteName).toList();
            ListSerialization<String> listSerialization = new ListSerialization<>();
            listSerialization.set_data(list);
            // 同步缓存数据
            redisTemplate.opsForValue().set("routeNameList", listSerialization);
            redisTemplate.expire("routeNameList", expireTime, java.util.concurrent.TimeUnit.SECONDS);
            return list;
        }

        // 查询线路信息

        // 先查缓存
        String routeKey = "route:routeName:" + routeName + "CityID:" + cityID;
        if ((routeName == null || routeName.isEmpty())) {
            routeKey = "route:CityID:" + cityID;
        }

        if(redisTemplate.hasKey(routeKey)) {
            List<Object> routeList = redisTemplate.opsForList().range(routeKey, 0, -1);
            List<RouteVO> routeVOList = new ArrayList<>();
            routeList.forEach(route -> {
                RouteVO routeVO =  (RouteVO) route;
                routeVOList.add(routeVO);
            });
            return routeVOList;
        }

        LambdaUpdateWrapper<Route> routeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        routeLambdaUpdateWrapper.eq(StringUtils.isNotBlank(routeName), Route::getRouteName, routeName)
                .eq(Route::getCityID, cityID)
                .eq(Route::getIsDeleted, false);
        List<Route> routes = routeMapper.selectList(routeLambdaUpdateWrapper);

        List<RouteVO> routeVOList = new ArrayList<>();
        for(Route route : routes) {
            routeVOList.add(RouteConvert.convertToVO(route));
        }

        // 将所有的routeID信息存储为一个搜寻列表
        List<Long> routeIDList = new ArrayList<>();
        for(RouteVO routeVO : routeVOList) {
            routeIDList.add(routeVO.getRouteID());
        }
        Map<Long, List<PointVO>> groupedByRouteID = routePointMapper.pointInRouteQuery(routeIDList)
                .stream().collect(Collectors.groupingBy(PointVO::getRouteID));
        for(RouteVO routeVO : routeVOList) {
            routeVO.setPoints(groupedByRouteID.get(routeVO.getRouteID()));
            redisTemplate.opsForList().rightPush(routeKey, routeVO);
        }
        if(!routeVOList.isEmpty()) {
            redisTemplate.expire(routeKey, expireTime, java.util.concurrent.TimeUnit.SECONDS);
        }
        return routeVOList;
    }

    @Override
    @Transactional
    public void routeUpdate(RouteDTO routeDTO) {
        // 更新线路信息
        LambdaUpdateWrapper<Route> routeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        routeLambdaUpdateWrapper.eq(Route::getRouteID, routeDTO.getRouteID())
                .set(Route::getRouteName, routeDTO.getRouteName())
                .set(StringUtils.isNotBlank(routeDTO.getRemarks()), Route::getRemarks, routeDTO.getRemarks())
                .set(Route::getEditTime, routeDTO.getEditTime());
        routeMapper.update(routeLambdaUpdateWrapper);

        // 删除原有线路和绘制点之间的联系
        LambdaUpdateWrapper<RoutePoint> routePointLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        routePointLambdaUpdateWrapper.eq(RoutePoint::getRouteID, routeDTO.getRouteID())
                .set(RoutePoint::getIsDeleted, true);
        int resRoutePointDelete = routePointMapper.update(routePointLambdaUpdateWrapper);
        if(resRoutePointDelete == 0) {
            throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
        }

        // 重新建立路线和绘制点之间的联系
        List<PointDTO> points = routeDTO.getPoints();

        List<PointDTO> newDrawPoints = points.stream().filter(point -> point.getPointID() == null).toList();
        if(!newDrawPoints.isEmpty()) {
            int resPointInsert = pointMapper.insertBatch(newDrawPoints);
            if(resPointInsert != newDrawPoints.size()) {
                throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
            }
        }
        int newDrawPointIdx = 0;
        for (PointDTO point : points) {
            if(point.getPointID() == null) {
                point.setPointID(newDrawPoints.get(newDrawPointIdx).getPointID());
                newDrawPointIdx++;
            }
        }

        // 向route_point表插入数据
        List<RoutePoint> routePoints = new ArrayList<>();
        for (PointDTO point : points) {
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRouteID(routeDTO.getRouteID());
            routePoint.setPointID(point.getPointID());
            routePoint.setEditTime(routeDTO.getEditTime());
            routePoints.add(routePoint);
        }

        int resRoutePointInsert = routePointMapper.insertBatch(routePoints);
        if(resRoutePointInsert != routePoints.size()) {
            throw new BusinessException(ResultCodeEnum.UPDATE_ERROR);
        }


        // 清空缓存
        Long routeID = routeDTO.getRouteID();
        List<Object> stations = redisTemplate.opsForList().range("routeStationMap:" + routeID, 0, -1);
        // 找到和当前route相关的站点的路径
        stations.forEach(staID -> {
            String routeStationKey = "route:staID:" + staID;
            redisTemplate.delete(routeStationKey);
        });
        Integer cityID = routeDTO.getCityID();
        String routeName = routeDTO.getRouteName();
        String routeKey_1 = "route:routeName:" + routeName + "CityID:" + cityID;
        String routeKey_2 = "route:CityID:" + cityID;
        redisTemplate.delete(routeKey_1);
        redisTemplate.delete(routeKey_2);
    }

    @Override
    @Transactional
    public void routeDelete(Long routeID) {
        // 逻辑删除线路和绘制点关联信息
        LambdaUpdateWrapper<RoutePoint> routePointLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        routePointLambdaUpdateWrapper.eq(RoutePoint::getRouteID, routeID)
                .set(RoutePoint::getIsDeleted, true);
        int resRoutePointDelete = routePointMapper.update(routePointLambdaUpdateWrapper);
        if(resRoutePointDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }
        // 逻辑删除线路信息
        int resRouteDelete = routeMapper.deleteById(routeID);
        if(resRouteDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }
    }

    @Override
    @Transactional
    public List<RouteVO> routeQueryByStation(Long staID) {

        String routeStationKey = "route:staID:" + staID;
        if(redisTemplate.hasKey(routeStationKey)) {
            List<Object> routeStation = redisTemplate.opsForList().range(routeStationKey, 0, -1);
            List<RouteVO> routeVOList = new ArrayList<>();
            routeStation.forEach(route -> {
                RouteVO routeVO = (RouteVO) route;
                routeVOList.add(routeVO);
            });
            return routeVOList;
        }

        List<RouteQStationVO> routeQStationVOS = routePointMapper.routeQueryByStation(staID);
        Map<Long, RouteVO> routeMap = new HashMap<>();
        List<RouteVO> routeVOS = new ArrayList<>();

        for(RouteQStationVO routeQStationVO : routeQStationVOS) {
            Long routeID = routeQStationVO.getRouteID();
            RouteVO routeVO = routeMap.get(routeID);
            if(routeVO == null) {
                routeVO = new RouteVO();
                routeVO.setRouteID(routeQStationVO.getRouteID());
                routeVO.setRouteName(routeQStationVO.getRouteName());
                routeVO.setPoints(new ArrayList<>());
                routeVOS.add(routeVO);
                routeMap.put(routeID, routeVO);
            }
            PointVO pointVO = new PointVO();
            pointVO.setPointID(routeQStationVO.getPointID());
            pointVO.setPointLat(routeQStationVO.getPointLat());
            pointVO.setPointLng(routeQStationVO.getPointLng());
            pointVO.setStaID(routeQStationVO.getStaID());
            pointVO.setStaName(routeQStationVO.getStaName());
            routeVO.getPoints().add(pointVO);
        }

        if(!routeVOS.isEmpty()) {
            for(RouteVO routeVO: routeVOS) {
                redisTemplate.opsForList().rightPush(routeStationKey, routeVO);
                String routeStationMap = "routeStationMap:" + routeVO.getRouteID();
                redisTemplate.opsForList().rightPush(routeStationMap, staID);
                redisTemplate.expire(routeStationMap, expireTime, TimeUnit.SECONDS);
            }
            redisTemplate.expire(routeStationKey, expireTime, TimeUnit.SECONDS);
        }

        return routeVOS;
    }
}
