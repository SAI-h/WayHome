package com.example.wayhome.convert;

import com.example.wayhome.entity.Admin;
import com.example.wayhome.vo.AdminVO;

/**
 * 管理员实体与 VO 转换器。
 *
 * @author WayHome
 */
public final class AdminConvert {

    private AdminConvert() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将管理员实体转换为 VO。
     *
     * @param admin 管理员实体
     * @return 管理员 VO
     */
    public static AdminVO convertToVo(Admin admin) {
        AdminVO adminVO = new AdminVO();
        adminVO.setAdminID(admin.getAdminID());
        adminVO.setAccount(admin.getAccount());
        adminVO.setCity(admin.getCity());
        return adminVO;
    }
}
