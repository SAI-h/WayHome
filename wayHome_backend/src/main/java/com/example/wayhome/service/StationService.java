package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.entity.Station;
import com.example.wayhome.vo.StationVO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface StationService extends IService<Station> {

    /**
     * 插入站点操作
     */
    void stationInsert(StationDTO stationDTO);

    /**
     * 根据站点名称返回站点信息
     */
    List<StationVO> stationQuery(String staName, Integer cityID);

//    /**
//     * 搜寻出所有的站点信息
//     */
//    List<StationVO> stationQueryAll(Integer cityID);

    /**
     * 对站点信息进行更新
     */
    void stationUpdate(StationDTO stationDTO);

    /**
     * 删除站点信息
     */
    void stationDelete(Long staID, Long pointID);
}
