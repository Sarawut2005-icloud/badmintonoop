package com.example.badmintonbookingsystem.model;

import java.io.Serial;
import java.util.List;

/** ผู้ดูแลระบบ: ดูสรุปรายได้รายวัน/รายเดือน และประวัติการจองของสมาชิก */
public class Admin extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    public Admin(String username, String password, String displayName) {
        super(username, password, displayName);
    }

    @Override
    public String roleName() {
        return "ผู้ดูแลระบบ";
    }

    @Override
    public List<Menu> menus() {
        return List.of(Menu.REVENUE, Menu.HISTORY);
    }
}
