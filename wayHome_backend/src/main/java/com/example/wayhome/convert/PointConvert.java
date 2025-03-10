package com.example.wayhome.convert;

import com.example.wayhome.dto.PointDTO;
import com.example.wayhome.entity.Point;

public class PointConvert {

    public static Point convertToDO(PointDTO pointDTO) {
        Point point = new Point();
        point.setPointLng(pointDTO.getPointLng());
        point.setPointLat(pointDTO.getPointLat());
        point.setEditTime(pointDTO.getEditTime());
        return point;
    }

}
