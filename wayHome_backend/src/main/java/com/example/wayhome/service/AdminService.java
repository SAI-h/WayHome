package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.entity.Admin;
import com.example.wayhome.vo.AdminVO;

/**
 * 管理员业务接口。
 *
 * @author WayHome
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录，并返回管理员管理辖区信息。
     *
     * @param adminDTO 管理员登录信息
     * @return 管理员信息
     */
    AdminVO adminLogin(AdminDTO adminDTO);
}
