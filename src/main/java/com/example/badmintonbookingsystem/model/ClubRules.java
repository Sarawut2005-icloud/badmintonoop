package com.example.badmintonbookingsystem.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

/** กฎทางธุรกิจทั้งหมดของสนามรวมไว้ที่เดียว: จำนวนสนาม ราคา เวลาเปิด-ปิด และการคิดส่วนลด */
public final class ClubRules {

    public static final int COURTS = 12;
    public static final double HOURLY_RATE = 180;
    public static final int PACKAGE_HOURS = 15;
    public static final double PACKAGE_PRICE = 2600;
    public static final int LOYALTY_EVERY = 20;     // จองครบ 19 ชม. → ชั่วโมงที่ 20 ได้ลด
    public static final double LOYALTY_RATE = 0.30; // จ่ายเพียง 30% ของราคาปกติ

    // shortcut: มีเฉพาะวันหยุดที่วันที่ตายตัว ไม่รวมวันหยุดตามจันทรคติ (มาฆบูชา ฯลฯ) — ถ้าต้องการครบให้แอดมินเพิ่มวันหยุดเองได้
    private static final Set<MonthDay> HOLIDAYS = Set.of(
            MonthDay.of(1, 1), MonthDay.of(4, 6), MonthDay.of(4, 13), MonthDay.of(4, 14), MonthDay.of(4, 15),
            MonthDay.of(5, 1), MonthDay.of(5, 4), MonthDay.of(6, 3), MonthDay.of(7, 28), MonthDay.of(8, 12),
            MonthDay.of(10, 13), MonthDay.of(10, 23), MonthDay.of(12, 5), MonthDay.of(12, 10), MonthDay.of(12, 31));

    private ClubRules() {
    }

    public static boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(MonthDay.from(date));
    }

    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** วันธรรมดา 13:00–21:00 · เสาร์-อาทิตย์และวันหยุดนักขัตฤกษ์ 10:00–20:00 */
    public static int openHour(LocalDate date) {
        return isWeekend(date) || isHoliday(date) ? 10 : 13;
    }

    public static int closeHour(LocalDate date) {
        return isWeekend(date) || isHoliday(date) ? 20 : 21;
    }

    public static String dayType(LocalDate date) {
        if (isHoliday(date)) return "วันหยุดนักขัตฤกษ์";
        return isWeekend(date) ? "วันเสาร์-อาทิตย์" : "วันธรรมดา";
    }

    /**
     * คิดราคาการจอง {@code hours} ชั่วโมงของสมาชิก
     * ใช้ชั่วโมงแพกเกจก่อน ที่เหลือจ่ายเงินสด และทุกชั่วโมงเงินสดที่ 20, 40, 60, ... คิดเพียง 30%
     * (ชั่วโมงแพกเกจไม่นับสะสม เพราะได้ราคาพิเศษไปแล้ว)
     */
    public static Quote quote(Member member, int hours) {
        int fromPackage = Math.min(hours, member.packageHours());
        int discounted = 0;
        for (int h = 1; h <= hours - fromPackage; h++) {
            if ((member.paidHours() + h) % LOYALTY_EVERY == 0) discounted++;
        }
        return new Quote(fromPackage, hours - fromPackage - discounted, discounted);
    }
}
