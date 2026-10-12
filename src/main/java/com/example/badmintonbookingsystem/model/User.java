package com.example.badmintonbookingsystem.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * ผู้ใช้ระบบ (abstract) — มีสองบทบาทคือ {@link Cashier} และ {@link Admin}
 * แต่ละบทบาทบอกเองว่าใช้เมนูอะไรได้ หน้าจอหลักจึงไม่ต้องมี if-else ตรวจสิทธิ์
 */
public abstract class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final String displayName;

    protected User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    public String username() {
        return username;
    }

    public String displayName() {
        return displayName;
    }

    public boolean passwordMatches(String input) {
        return password.equals(input);
    }

    /** ชื่อบทบาทที่แสดงบนหน้าจอ */
    public abstract String roleName();

    /** เมนูที่บทบาทนี้เข้าถึงได้ เรียงตามลำดับที่แสดงในแถบด้านซ้าย */
    public abstract List<Menu> menus();
}
