package com.example.wayhome.convert;

import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.entity.Station;
import com.example.wayhome.vo.StationVO;

/**
 * 站点 DTO、实体与 VO 转换器。
 *
 * @author WayHome
 */
public final class StationConvert {

    private StationConvert() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将站点 DTO 转换为实体。
     *
     * @param stationDTO 站点 DTO
     * @return 站点实体
     */
    public static Station convertToDo(StationDTO stationDTO) {
        Station station = new Station();
        station.setStaID(stationDTO.getStaID());
        station.setStaName(stationDTO.getStaName());
        station.setStaAddress(stationDTO.getStaAddress());
        station.setRemarks(stationDTO.getRemarks());
        station.setEditTime(stationDTO.getEditTime());
        station.setCityID(stationDTO.getCityID());
        station.setPointID(stationDTO.getPointID());
        return station;
    }

    /**
     * 将站点实体转换为 VO。
     *
     * @param station 站点实体
     * @return 站点 VO
     */
    public static StationVO convertToVo(Station station) {
        StationVO stationVO = new StationVO();
        stationVO.setStaID(station.getStaID());
        stationVO.setStaName(station.getStaName());
        stationVO.setStaAddress(station.getStaAddress());
        stationVO.setCityID(station.getCityID());
        stationVO.setPointID(station.getPointID());
        stationVO.setStaLat(station.getPoint().getPointLat());
        stationVO.setStaLng(station.getPoint().getPointLng());
        stationVO.setRemarks(station.getRemarks());
        stationVO.setEditTime(station.getEditTime());
        return stationVO;
    }
}
