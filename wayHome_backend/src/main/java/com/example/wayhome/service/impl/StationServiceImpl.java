package com.example.wayhome.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private PointMapper pointMapper;

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
    public StationVO stationQuery(String staName) {
        Station station = stationMapper.stationQuery(staName);
        if (station == null) {
            throw new BusinessException(ResultCodeEnum.QUERY_ERROR);
        }
        return StationConvert.convertToVO(station);
    }

    @Override
    @Transactional
    public void stationUpdate(StationDTO stationDTO) {
        // 更新 point
        LambdaUpdateWrapper<Point> pointLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        pointLambdaUpdateWrapper.eq(Point::getPointID, stationDTO.getPointID())
                .set(Point::getPointLat, stationDTO.getStaLat())
                .set(Point::getPointLng, stationDTO.getStaLng())
                .set(Point::getEditTime, stationDTO.getEditTime());
        int resPointUpdate = pointMapper.update(pointLambdaUpdateWrapper);

        // 更新 station
        Station station = StationConvert.convertToDO(stationDTO);
        LambdaUpdateWrapper<Station> stationLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        stationLambdaUpdateWrapper.eq(Station::getStaID, station.getStaID())
                .set(Station::getStaName, station.getStaName())
                .set(Station::getStaAddress, station.getStaAddress())
                .set(Station::getEditTime, station.getEditTime())
                .set(Station::getRemarks, station.getRemarks());
        int resStationUpdate = stationMapper.update(stationLambdaUpdateWrapper);

    }
}
