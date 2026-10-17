package com.example.wayhome.convert;

import com.example.wayhome.entity.City;
import com.example.wayhome.vo.CityVO;

/**
 * 城市实体与 VO 转换器。
 *
 * @author WayHome
 */
public final class CityConvert {

    private CityConvert() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将城市实体转换为 VO。
     *
     * @param city 城市实体
     * @return 城市 VO
     */
    public static CityVO convertToVo(City city) {
        CityVO cityVO = new CityVO();
        cityVO.setCityID(city.getCityID());
        cityVO.setCityName(city.getCityName());
        cityVO.setCityLng(city.getCityLng());
        cityVO.setCityLat(city.getCityLat());
        return cityVO;
    }
}
