package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.entity.City;
import com.example.wayhome.vo.CityVO;

import java.util.List;

public interface CityService extends IService<City> {

    List<CityVO> getCityList();
}
