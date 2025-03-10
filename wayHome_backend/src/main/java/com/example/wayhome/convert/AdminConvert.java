package com.example.wayhome.convert;

import com.example.wayhome.entity.Admin;
import com.example.wayhome.vo.AdminVO;

public class AdminConvert {

    public static AdminVO convertToVO(Admin admin) {
        AdminVO adminVO = new AdminVO();
        adminVO.setAdminID(admin.getAdminID());
        adminVO.setAccount(admin.getAccount());
        adminVO.setCity(admin.getCity());
        return adminVO;
    }

}
