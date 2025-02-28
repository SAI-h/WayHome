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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.expiration}")
    private Long expireTime;

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

        // 删除缓存
        redisTemplate.delete("station:cityID:" + stationDTO.getCityID());
    }

    @Override
    public List<StationVO> stationQuery(String staName, Integer cityID) {
        String stationKey = "station:staName:" + staName + "cityID:" + cityID;
        if(staName == null) {
            stationKey = "station:cityID:" + cityID;
        }
        if(redisTemplate.hasKey(stationKey)) {
            List<Object> stationObjs = redisTemplate.opsForList().range(stationKey, 0, -1);
            List<StationVO> stationVOList = new ArrayList<>();
            stationObjs.forEach(stationObj -> {
                stationVOList.add((StationVO) stationObj);
            });
            return stationVOList;
        }

        List<Station> stations = stationMapper.stationQuery(staName, cityID);
        if (stations == null) {
            throw new BusinessException(ResultCodeEnum.QUERY_ERROR);
        }
        List<StationVO> stationVOList = new ArrayList<>();
        if(!stations.isEmpty()) {
            for (Station station : stations) {
                StationVO stationVO = StationConvert.convertToVO(station);
                stationVOList.add(stationVO);
                redisTemplate.opsForList().rightPush(stationKey, stationVO);
                redisTemplate.opsForValue().set("stationMap:staID:" + station.getStaID(), station.getStaName() + ":" + station.getCityID());
                redisTemplate.expire("stationMap:staID:" + station.getStaID(), expireTime, TimeUnit.SECONDS);
            }
            redisTemplate.expire(stationKey, expireTime, java.util.concurrent.TimeUnit.SECONDS);
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

        // 删除缓存
        String stationDelKey_1 = "station:staName:" + stationDTO.getStaName() + "cityID:" + stationDTO.getCityID();
        String stationDelKey_2 = "station:cityID:" + stationDTO.getCityID();
        redisTemplate.delete(stationDelKey_1);
        redisTemplate.delete(stationDelKey_2);
        redisTemplate.delete("stationMap:staID:" + stationDTO.getStaID());

        // 清除和线路的关联信息缓存
        Set<String> keys = redisTemplate.keys("route*");
        if(keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
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

        // 删除缓存(不用删除route的全部缓存是因为,若站点和route绑定无法完成删除操作)
        // 能删除的站点,一定和路线不存在关系
        String nameCityObj = (String) redisTemplate.opsForValue().get("stationMap:staID:" + staID);
        String[] split = nameCityObj.split(":");
        String stationDelKey_1 = "station:staName:" + split[0] + "cityID:" + split[1];
        String stationDelKey_2 = "station:cityID:" + split[1];
        redisTemplate.delete(stationDelKey_1);
        redisTemplate.delete(stationDelKey_2);
        redisTemplate.delete("stationMap:staID:" + staID);
    }
}
