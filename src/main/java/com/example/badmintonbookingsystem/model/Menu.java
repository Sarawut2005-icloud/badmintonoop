package com.example.badmintonbookingsystem.model;

/** เมนูทั้งหมดของระบบ ผู้ใช้แต่ละบทบาทจะเห็นเฉพาะเมนูที่ {@link User#menus()} คืนมา */
public enum Menu {
    BOOKING("จองสนาม", "เลือกสนามและช่วงเวลาจากตาราง แล้วบันทึกการจองพร้อมสลิป"),
    SEARCH("ค้นหาการจอง", "ค้นหาด้วยชื่อ เบอร์โทร รหัส วันที่ หรือหมายเลขสนาม"),
    MEMBERS("สมาชิก", "สมัครสมาชิกใหม่ และขายแพกเกจเหมาจ่าย 15 ชั่วโมง"),
    SHOP("เช่า / ซื้ออุปกรณ์", "เช่ารองเท้าและไม้แบด หรือซื้ออุปกรณ์การเล่น"),
    REVENUE("สรุปรายได้", "รายได้รายวันและรายเดือน แยกตามประเภท"),
    HISTORY("ประวัติสมาชิก", "ประวัติการจองสนามของสมาชิกแต่ละคน");

    private final String title;
    private final String subtitle;

    Menu(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }
}
