package com.example.badmintonbookingsystem.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * ทุกอย่างที่ทำให้สนามมีรายได้ (การจอง, แพกเกจ, เช่า/ขายอุปกรณ์)
 * รายงานรายได้รวมยอดผ่าน interface นี้ตัวเดียว โดยไม่ต้องรู้ว่าแต่ละรายการเป็นคลาสอะไร
 */
public interface Billable extends Serializable {

    /** วันที่รับเงิน */
    LocalDate paidOn();

    RevenueType type();

    /** จำนวนเงินที่รับจริง (บาท) */
    double amount();
}
