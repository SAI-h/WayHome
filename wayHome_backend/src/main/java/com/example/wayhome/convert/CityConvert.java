package com.example.wayhome.convert;

import com.example.wayhome.entity.City;
import com.example.wayhome.vo.CityVO;

public class CityConvert {

    public static CityVO convertToVO(City city)
    {
        CityVO cityVO = new CityVO();
        cityVO.setCityID(city.getCityID());
        cityVO.setCityName(city.getCityName());
        cityVO.setCityLng(city.getCityLng());
        cityVO.setCityLat(city.getCityLat());
        return cityVO;
    }

}
