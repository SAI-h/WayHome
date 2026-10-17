package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.entity.City;
import com.example.wayhome.vo.CityVO;

import java.util.List;

/**
 * 城市业务接口。
 *
 * @author WayHome
 */
public interface CityService extends IService<City> {

    /**
     * 获取城市列表。
     *
     * @return 城市列表
     */
    List<CityVO> getCityList();
}
