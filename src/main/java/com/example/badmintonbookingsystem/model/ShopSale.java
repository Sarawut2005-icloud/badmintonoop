package com.example.badmintonbookingsystem.model;

import java.time.LocalDate;

/** การเช่าหรือซื้ออุปกรณ์หนึ่งรายการ */
public record ShopSale(Product product, int quantity, LocalDate paidOn, String recordedBy) implements Billable {

    @Override
    public RevenueType type() {
        return product.type();
    }

    @Override
    public double amount() {
        return product.price() * quantity;
    }
}
