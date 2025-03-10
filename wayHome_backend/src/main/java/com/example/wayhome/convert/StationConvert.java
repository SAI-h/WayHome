package com.example.wayhome.convert;

import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.entity.Station;
import com.example.wayhome.vo.StationVO;

import java.time.LocalTime;

public class StationConvert {

    public static Station convertToDO(StationDTO stationDTO) {
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

    public static StationVO convertToVO(Station station) {
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
