package com.example.badmintonbookingsystem.model;

import java.io.Serial;
import java.util.List;

/** พนักงานแคชเชียร์: บันทึกการจอง ค้นหาการจอง ดูแลสมาชิก และขาย/ให้เช่าอุปกรณ์ */
public class Cashier extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    public Cashier(String username, String password, String displayName) {
        super(username, password, displayName);
    }

    @Override
    public String roleName() {
        return "พนักงานแคชเชียร์";
    }

    @Override
    public List<Menu> menus() {
        return List.of(Menu.BOOKING, Menu.SEARCH, Menu.MEMBERS, Menu.SHOP);
    }
}
