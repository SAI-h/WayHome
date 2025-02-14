package com.example.wayhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.entity.Admin;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.AdminVO;

public interface AdminService extends IService<Admin> {

    Result<AdminVO> adminLogin(AdminDTO adminDTO);

}
