package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.StationDTO;
import com.example.wayhome.entity.Station;
import com.example.wayhome.vo.StationVO;

import java.util.List;

/**
 * 站点业务接口。
 *
 * @author WayHome
 */
public interface StationService extends IService<Station> {

    /**
     * 新增站点。
     *
     * @param stationDTO 站点信息
     */
    void stationInsert(StationDTO stationDTO);

    /**
     * 根据站点名称（可选）查询站点。
     *
     * @param staName 站点名称
     * @param cityId  城市 ID
     * @return 站点列表
     */
    List<StationVO> stationQuery(String staName, Integer cityId);

    /**
     * 更新站点。
     *
     * @param stationDTO 站点信息
     */
    void stationUpdate(StationDTO stationDTO);

    /**
     * 删除站点。
     *
     * @param staId   站点 ID
     * @param pointId 坐标点 ID
     */
    void stationDelete(Long staId, Long pointId);
}
