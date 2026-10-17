package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.constant.RedisKeyConstant;
import com.example.wayhome.convert.CityConvert;
import com.example.wayhome.entity.City;
import com.example.wayhome.mapper.CityMapper;
import com.example.wayhome.service.CityService;
import com.example.wayhome.vo.CityVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 城市业务实现，带缓存读取。
 *
 * @author WayHome
 */
@Service
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {

    private final CityMapper cityMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.expiration}")
    private Long expireTime;

    public CityServiceImpl(CityMapper cityMapper, RedisTemplate<String, Object> redisTemplate) {
        this.cityMapper = cityMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<CityVO> getCityList() {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        List<CityVO> cachedList = readFromCache(ops);
        if (!cachedList.isEmpty()) {
            return cachedList;
        }

        List<City> cities = cityMapper.selectList(null);
        List<CityVO> cityVOList = new ArrayList<>();
        for (City city : cities) {
            CityVO cityVO = CityConvert.convertToVo(city);
            cityVOList.add(cityVO);
            ops.rightPush(RedisKeyConstant.CITY_LIST, cityVO);
        }
        if (!cityVOList.isEmpty()) {
            redisTemplate.expire(RedisKeyConstant.CITY_LIST, expireTime, TimeUnit.SECONDS);
        }
        return cityVOList;
    }

    /**
     * 从缓存读取城市列表。
     *
     * @param ops 列表操作对象
     * @return 城市列表
     */
    @SuppressWarnings("unchecked")
    private List<CityVO> readFromCache(ListOperations<String, Object> ops) {
        if (ops.size(RedisKeyConstant.CITY_LIST) <= 0) {
            return new ArrayList<>();
        }
        List<Object> cached = ops.range(RedisKeyConstant.CITY_LIST, 0, -1);
        List<CityVO> cityVOList = new ArrayList<>();
        for (Object item : cached) {
            cityVOList.add((CityVO) item);
        }
        return cityVOList;
    }
}
