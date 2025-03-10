package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.dto.PointDTO;
import com.example.wayhome.entity.Point;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointMapper extends BaseMapper<Point> {

    @Insert("<script>" +
                "INSERT INTO point (pointLat, pointLng) VALUES " +
                "<foreach collection='points' item='point' separator=','>" +
                    "(#{point.pointLat}, #{point.pointLng})" +
                "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "pointID")
    int insertBatch(@Param("points") List<PointDTO> points);

}
