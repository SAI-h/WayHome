package com.example.wayhome.controller;

import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.service.AdminService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.AdminVO;
import jakarta.validation.Valid;
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

    @PostMapping("/login")
    public Result<AdminVO> adminLogin(@Valid @RequestBody AdminDTO adminDTO) {
        AdminVO adminVO = adminService.adminLogin(adminDTO);
        return Result.ok(adminVO);
    }

}
