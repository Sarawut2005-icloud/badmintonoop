package com.example.badmintonbookingsystem.model;

/** สินค้าและอุปกรณ์ให้เช่าที่หน้าเคาน์เตอร์ (ราคาสินค้าขายกำหนดเอง) */
public enum Product {
    SHOES_RENTAL("รองเท้าแบดมินตัน", 50, true),   // เช่า 50 บาท/ครั้ง/วัน
    RACKET_RENTAL("ไม้แบดมินตัน", 50, true),
    SHUTTLECOCK("ลูกขนไก่", 80, false),
    GRIP_TAPE("เทปพันด้ามไม้", 60, false),
    RACKET("ไม้แบดมินตัน", 1290, false);

    private final String label;
    private final double price;
    private final boolean rental;

    Product(String label, double price, boolean rental) {
        this.label = label;
        this.price = price;
        this.rental = rental;
    }

    public String label() {
        return label;
    }

    public double price() {
        return price;
    }

    public boolean isRental() {
        return rental;
    }

    public RevenueType type() {
        return rental ? RevenueType.RENTAL : RevenueType.SALE;
    }

    @Override
    public String toString() {
        return (rental ? "เช่า" : "ซื้อ") + label;
    }
}
