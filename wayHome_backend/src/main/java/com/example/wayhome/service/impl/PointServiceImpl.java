package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.convert.PointConvert;
import com.example.wayhome.dto.PointDTO;
import com.example.wayhome.entity.Point;
import com.example.wayhome.exception.BusinessException;
import com.example.wayhome.mapper.PointMapper;
import com.example.wayhome.service.PointService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.utils.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointServiceImpl extends ServiceImpl<PointMapper, Point> implements PointService {

    @Autowired
    private PointMapper pointMapper;

    @Override
    @Transactional
    public void pointInsert(PointDTO pointDTO) {
        Point point = PointConvert.convertToDO(pointDTO);
        int result = pointMapper.insert(point);
        if(result != 1) {
            throw new BusinessException(ResultCodeEnum.INSERT_ERROR);
        }
    }
}
