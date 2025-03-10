package com.example.wayhome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wayhome.convert.AdminConvert;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.entity.Admin;
import com.example.wayhome.exception.BusinessException;
import com.example.wayhome.mapper.AdminMapper;
import com.example.wayhome.service.AdminService;
import com.example.wayhome.utils.MD5Util;
import com.example.wayhome.utils.ResultCodeEnum;
import com.example.wayhome.vo.AdminVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Override
    @Transactional
    public AdminVO adminLogin(AdminDTO adminDTO) {
        // 数据加密
        adminDTO.setPassword(MD5Util.encrypt(adminDTO.getPassword()));
        Admin admin = adminMapper.adminLogin(adminDTO);
        if(admin == null) {
            throw new BusinessException(ResultCodeEnum.LOGIN_ERROR);
        }
        return AdminConvert.convertToVO(admin);
    }
}
