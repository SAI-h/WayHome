package com.example.wayhome.controller;

import com.example.wayhome.dto.AdminDTO;
import com.example.wayhome.service.AdminService;
import com.example.wayhome.utils.Result;
import com.example.wayhome.vo.AdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("login")
    public Result<AdminVO> adminLogin(@RequestBody AdminDTO adminDTO) {
        return adminService.adminLogin(adminDTO);
    }

}
