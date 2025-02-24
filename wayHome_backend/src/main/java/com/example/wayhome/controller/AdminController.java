package com.example.wayhome.controller;

import com.example.wayhome.config.JwtAdminConfig;
import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.service.AdminService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.AdminVO;
import jakarta.validation.Valid;
import org.apache.tomcat.Jar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtAdminConfig jwtUtil;

    @PostMapping
    public Result<String> adminLogin(@Valid @RequestBody AdminDTO adminDTO) {
        AdminVO adminVO = adminService.adminLogin(adminDTO);
        String jwtToken = jwtUtil.generateJwtToken(adminVO);
        return Result.ok(jwtToken);
    }

}
