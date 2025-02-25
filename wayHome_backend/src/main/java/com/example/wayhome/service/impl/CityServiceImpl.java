package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.convert.CityConvert;
import com.example.wayhome.entity.City;
import com.example.wayhome.mapper.CityMapper;
import com.example.wayhome.service.CityService;
import com.example.wayhome.vo.CityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CityServiceImpl extends ServiceImpl<CityMapper, City> implements CityService {

    @Autowired
    private CityMapper cityMapper;

    @Override
    public List<CityVO> getCityList() {
        List<City> cities = cityMapper.selectList(null);
        List<CityVO> cityVOS = new ArrayList<>();
        for (City city : cities) {
            cityVOS.add(CityConvert.convertToVO(city));
        }
        return cityVOS;
    }
}
