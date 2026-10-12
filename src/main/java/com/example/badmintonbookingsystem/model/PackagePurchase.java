package com.example.badmintonbookingsystem.model;

import java.time.LocalDate;

/** การซื้อแพกเกจเหมาจ่าย 15 ชั่วโมง ราคา 2,600 บาท */
public record PackagePurchase(Member member, LocalDate paidOn, String recordedBy) implements Billable {

    @Override
    public RevenueType type() {
        return RevenueType.PACKAGE;
    }

    @Override
    public double amount() {
        return ClubRules.PACKAGE_PRICE;
    }
}
