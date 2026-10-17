package com.example.wayhome.controller;

import com.example.wayhome.config.JwtAdminConfig;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.service.AdminService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.AdminVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员登录接口。
 *
 * @author WayHome
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    private final JwtAdminConfig jwtAdminConfig;

    public AdminController(AdminService adminService, JwtAdminConfig jwtAdminConfig) {
        this.adminService = adminService;
        this.jwtAdminConfig = jwtAdminConfig;
    }

    /**
     * 管理员登录，校验成功后返回 JWT 令牌。
     *
     * @param adminDTO 管理员登录信息
     * @return JWT 令牌
     */
    @PostMapping
    public Result<String> adminLogin(@Valid @RequestBody AdminDTO adminDTO) {
        AdminVO adminVO = adminService.adminLogin(adminDTO);
        return Result.ok(jwtAdminConfig.generateJwtToken(adminVO));
    }
}
