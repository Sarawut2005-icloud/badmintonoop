package com.example.badmintonbookingsystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * การจองสนามหนึ่งครั้ง (ข้อมูลไม่เปลี่ยนแปลงหลังบันทึก)
 *
 * @param slip       ชื่อไฟล์สลิปในโฟลเดอร์ data/slips หรือ null ถ้าใช้ชั่วโมงแพกเกจทั้งหมด
 * @param recordedBy ชื่อแคชเชียร์ที่บันทึก
 */
public record Booking(String id, Member member, int court, LocalDate date, int startHour, int hours,
                      Quote quote, String slip, String recordedBy, LocalDateTime createdAt) implements Billable {

    public int endHour() {
        return startHour + hours;
    }

    public String timeRange() {
        return "%02d:00 – %02d:00".formatted(startHour, endHour());
    }

    /** ช่วง [start, end) ของสนามและวันเดียวกันทับกับการจองนี้หรือไม่ */
    public boolean overlaps(int court, LocalDate date, int start, int end) {
        return this.court == court && this.date.equals(date) && start < endHour() && startHour < end;
    }

    @Override
    public LocalDate paidOn() {
        return createdAt.toLocalDate();
    }

    @Override
    public RevenueType type() {
        return RevenueType.COURT;
    }

    @Override
    public double amount() {
        return quote.amount();
    }
}
