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
import org.springframework.stereotype.Service;

/**
 * 管理员业务实现。
 *
 * @author WayHome
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final AdminMapper adminMapper;

    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public AdminVO adminLogin(AdminDTO adminDTO) {
        String encryptedPassword = MD5Util.encrypt(adminDTO.getPassword());
        adminDTO.setPassword(encryptedPassword);
        Admin admin = adminMapper.adminLogin(adminDTO);
        if (admin == null) {
            throw new BusinessException(ResultCodeEnum.LOGIN_ERROR);
        }
        return AdminConvert.convertToVo(admin);
    }
}
