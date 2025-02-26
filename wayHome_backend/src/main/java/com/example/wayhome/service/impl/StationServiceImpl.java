package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.convert.StationConvert;
import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.entity.Point;
import com.example.wayhome.entity.Station;
import com.example.wayhome.exception.BusinessException;
import com.example.wayhome.mapper.PointMapper;
import com.example.wayhome.mapper.StationMapper;
import com.example.wayhome.service.StationService;
import com.example.wayhome.utils.ResultCodeEnum;
import com.example.wayhome.vo.StationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void stationInsert(StationDTO stationDTO) {
        // 先插入 point
        Point point = new Point();
        point.setPointLng(stationDTO.getStaLng());
        point.setPointLat(stationDTO.getStaLat());
        point.setEditTime(stationDTO.getEditTime());
        int resPointInsert = pointMapper.insert(point);

        if(resPointInsert != 1) {
            throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
        }

        Station station = StationConvert.convertToDO(stationDTO);
        station.setPointID(point.getPointID());

        // 再插入station
        int resStationInsert = stationMapper.stationInsert(station);

        if(resStationInsert != 1) {
            throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
        }

        // 更新 point 内的 staID 值
        LambdaUpdateWrapper<Point> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Point::getPointID, point.getPointID())
                        .set(Point::getStaID, station.getStaID());
        int resPointUpdate = pointMapper.update(lambdaUpdateWrapper);

        if(resPointUpdate != 1) {
            throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
        }
    }

    @Override
    public List<StationVO> stationQuery(String staName, Integer cityID) {
        List<Station> stations = stationMapper.stationQuery(staName, cityID);
        if (stations == null) {
            throw new BusinessException(ResultCodeEnum.QUERY_ERROR);
        }
        List<StationVO> stationVOList = new ArrayList<>();
        for(Station station: stations) {
            StationVO stationVO = StationConvert.convertToVO(station);
            stationVOList.add(stationVO);
        }
        return stationVOList;
    }

//    @Override
//    public List<StationVO> stationQueryAll(Integer cityID) {
//        List<Station> stations = stationMapper.stationQuery(null, cityID);
//        if(stations == null) {
//            throw new BusinessException(ResultCodeEnum.QUERY_ERROR);
//        }
//        List<StationVO> stationVOList = new ArrayList<>();
//        for(Station station: stations) {
//            StationVO stationVO = StationConvert.convertToVO(station);
//            stationVOList.add(stationVO);
//        }
//        return stationVOList;
//    }

    @Override
    @Transactional
    public void stationUpdate(StationDTO stationDTO) {
        // 更新 point
        LambdaUpdateWrapper<Point> pointLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        pointLambdaUpdateWrapper.eq(Point::getPointID, stationDTO.getPointID())
                .set(Point::getPointLat, stationDTO.getStaLat())
                .set(Point::getPointLng, stationDTO.getStaLng())
                .set(Point::getEditTime, stationDTO.getEditTime());
        pointMapper.update(pointLambdaUpdateWrapper);

        // 更新 station
        Station station = StationConvert.convertToDO(stationDTO);
        LambdaUpdateWrapper<Station> stationLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        stationLambdaUpdateWrapper.eq(Station::getStaID, station.getStaID())
                .set(Station::getStaName, station.getStaName())
                .set(Station::getStaAddress, station.getStaAddress())
                .set(Station::getEditTime, station.getEditTime())
                .set(Station::getRemarks, station.getRemarks());
        stationMapper.update(stationLambdaUpdateWrapper);
    }

    @Override
    @Transactional
    public void stationDelete(Long staID, Long pointID) {
        // 保证站点和线路不存在绑定关系时才能进行删除
        int resStationDelete = stationMapper.stationDelete(staID);
//        int resStationDelete = stationMapper.deleteById(staID);
        if(resStationDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }
        int resPointDelete = pointMapper.deleteById(pointID);
        if (resPointDelete == 0) {
            throw new BusinessException(ResultCodeEnum.DELETE_ERROR);
        }
    }
}
