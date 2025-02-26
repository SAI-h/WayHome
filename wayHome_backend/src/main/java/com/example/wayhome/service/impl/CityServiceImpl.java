package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.convert.CityConvert;
import com.example.wayhome.entity.City;
import com.example.wayhome.mapper.CityMapper;
import com.example.wayhome.service.CityService;
import com.example.wayhome.vo.CityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.expiration}")
    private Long expireTime;

    @Override
    @Transactional
    public List<CityVO> getCityList() {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        if (ops.size("cityList") > 0) {
            List<Object> cityList = ops.range("cityList", 0, -1);
            List<CityVO> cityVOS = new ArrayList<>();
            for (Object o : cityList) {
                CityVO cityVO = (CityVO) o;
                cityVOS.add(cityVO);
            }
            return cityVOS;
        }

        List<City> cities = cityMapper.selectList(null);
        if(!cities.isEmpty()) {
            List<CityVO> cityVOS = new ArrayList<>();
            for (City city : cities) {
                CityVO cityVO = CityConvert.convertToVO(city);
                cityVOS.add(cityVO);
                ops.rightPush("cityList", cityVO);
            }
            redisTemplate.expire("cityList", expireTime, java.util.concurrent.TimeUnit.SECONDS);
            return cityVOS;
        }
        return null;
    }
}
