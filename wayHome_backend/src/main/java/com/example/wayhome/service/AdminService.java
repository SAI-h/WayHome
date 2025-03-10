package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.entity.Admin;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.AdminVO;

public interface AdminService extends IService<Admin> {

    /**
     * 处理管理员的登录操作，并返回管理员的管理辖区
     */
    AdminVO adminLogin(AdminDTO adminDTO);

}
