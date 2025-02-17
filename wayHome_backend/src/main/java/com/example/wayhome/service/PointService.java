package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.PointDTO;
import com.example.wayhome.entity.Point;
import com.example.wayhome.utils.Result;

public interface PointService extends IService<Point> {

    /**
     * 插入绘点操作
     */
    void pointInsert(PointDTO pointDTO);

}
