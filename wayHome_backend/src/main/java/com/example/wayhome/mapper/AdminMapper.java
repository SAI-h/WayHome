package com.example.wayhome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.entity.Admin;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT  a.adminID, a.account, a.password, " +
                    "c.cityID, c.cityName, c.cityLng, c.cityLat " +
            "FROM admin a " +
            "JOIN city c ON a.cityID = c.cityID " +
            "WHERE account = #{adminDTO.account} AND password = #{adminDTO.password}")
    @Results(id = "adminResultMap", value = {
            @Result(property = "city.cityID", column = "cityID"),
            @Result(property = "city.cityName", column = "cityName"),
            @Result(property = "city.cityLng", column = "cityLng"),
            @Result(property = "city.cityLat", column = "cityLat"),
    })
    Admin adminLogin(@Param("adminDTO") AdminDTO adminDTO);

}
