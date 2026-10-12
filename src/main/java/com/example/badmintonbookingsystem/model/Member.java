package com.example.badmintonbookingsystem.model;

import java.io.Serial;
import java.io.Serializable;

/** สมาชิก — เฉพาะสมาชิกเท่านั้นที่จองสนามได้ */
public class Member implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String phone;
    private int paidHours;     // ชั่วโมงที่จ่ายเงินสดสะสม ใช้นับสิทธิ์ "ชั่วโมงที่ 20 ลด 70%"
    private int packageHours;  // ชั่วโมงแพกเกจเหมาจ่ายที่ยังเหลือ

    public Member(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String phone() {
        return phone;
    }

    public int paidHours() {
        return paidHours;
    }

    public int packageHours() {
        return packageHours;
    }

    /** ต้องจองอีกกี่ชั่วโมงจึงถึงชั่วโมงลดราคา (1 = ชั่วโมงถัดไปได้ลดเลย) */
    public int hoursUntilDiscount() {
        return ClubRules.LOYALTY_EVERY - paidHours % ClubRules.LOYALTY_EVERY;
    }

    public void addPackageHours(int hours) {
        packageHours += hours;
    }

    /** ตรงกับคำค้นไหม: รหัสหรือชื่อ (ไม่สนตัวพิมพ์) หรือเบอร์ (ไม่สนขีดและช่องว่าง เช่น 081-234) */
    public boolean matches(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        String digits = q.replaceAll("[\\s-]", "");
        return q.isEmpty() || id.toLowerCase().contains(q) || name.toLowerCase().contains(q)
                || !digits.isEmpty() && phone.contains(digits);
    }

    /** ตัดชั่วโมงแพกเกจและสะสมชั่วโมงเงินสดตามใบเสนอราคาที่จองสำเร็จ */
    public void charge(Quote quote) {
        packageHours -= quote.packageHours();
        paidHours += quote.cashHours();
    }

    /** ย้อน {@link #charge} เมื่อบันทึกการจองไม่สำเร็จ */
    public void refund(Quote quote) {
        packageHours += quote.packageHours();
        paidHours -= quote.cashHours();
    }

    @Override
    public String toString() {
        return id + "  " + name + "  · " + phone;
    }
}
