package com.example.wayhome.convert;

import com.example.wayhome.dto.PointDTO;
import com.example.wayhome.entity.Point;

/**
 * 坐标点 DTO 与实体转换器。
 *
 * @author WayHome
 */
public final class PointConvert {

    private PointConvert() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将坐标点 DTO 转换为实体。
     *
     * @param pointDTO 坐标点 DTO
     * @return 坐标点实体
     */
    public static Point convertToDo(PointDTO pointDTO) {
        Point point = new Point();
        point.setPointLng(pointDTO.getPointLng());
        point.setPointLat(pointDTO.getPointLat());
        point.setEditTime(pointDTO.getEditTime());
        return point;
    }
}
