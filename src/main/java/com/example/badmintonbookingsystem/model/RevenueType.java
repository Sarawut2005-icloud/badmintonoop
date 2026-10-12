package com.example.badmintonbookingsystem.model;

/** หมวดของรายได้ ใช้แยกสีในกราฟและคอลัมน์ในตารางสรุป */
public enum RevenueType {
    COURT("ค่าสนาม"),
    PACKAGE("แพกเกจ"),
    RENTAL("เช่าอุปกรณ์"),
    SALE("ขายสินค้า");

    private final String label;

    RevenueType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
