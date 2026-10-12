package com.example.badmintonbookingsystem.model;

import java.io.Serializable;

import static com.example.badmintonbookingsystem.model.ClubRules.HOURLY_RATE;
import static com.example.badmintonbookingsystem.model.ClubRules.LOYALTY_RATE;

/**
 * ผลการคิดราคาการจองหนึ่งครั้ง แยกชั่วโมงออกเป็น 3 แบบ
 *
 * @param packageHours    ชั่วโมงที่หักจากแพกเกจ (ไม่ต้องจ่ายเพิ่ม)
 * @param fullHours       ชั่วโมงราคาปกติ
 * @param discountedHours ชั่วโมงที่ 20 ที่คิดเพียง 30%
 */
public record Quote(int packageHours, int fullHours, int discountedHours) implements Serializable {

    public int cashHours() {
        return fullHours + discountedHours;
    }

    public double fullAmount() {
        return fullHours * HOURLY_RATE;
    }

    public double discountedAmount() {
        return discountedHours * HOURLY_RATE * LOYALTY_RATE;
    }

    public double amount() {
        return fullAmount() + discountedAmount();
    }
}
