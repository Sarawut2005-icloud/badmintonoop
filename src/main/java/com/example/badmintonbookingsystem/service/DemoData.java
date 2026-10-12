package com.example.badmintonbookingsystem.service;

import com.example.badmintonbookingsystem.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static com.example.badmintonbookingsystem.model.ClubRules.*;

/**
 * ข้อมูลตัวอย่างย้อนหลัง 3 เดือน สร้างครั้งแรกที่เปิดโปรแกรม เพื่อให้กราฟและตารางมีข้อมูลตอนสาธิต
 * (ลบโฟลเดอร์ data เพื่อเริ่มข้อมูลใหม่)
 */
final class DemoData {

    private static final String CASHIER = "Jack";

    private DemoData() {
    }

    static Club create() {
        Club club = new Club();
        club.addUser(new Cashier("cashier", "1234", CASHIER));
        club.addUser(new Admin("admin", "1234", "นุ"));

        List<Member> members = List.of(
                club.registerMember("สมชาย ตีไกล", "0812345678"),
                club.registerMember("วิภาวดี สแมชแรง", "0898765432"),
                club.registerMember("ณัฐพล ลูกยาว", "0861112233"),
                club.registerMember("กมลชนก ดรอปสวย", "0954445566"),
                club.registerMember("ธนากร หยอดหน้าเน็ต", "0827778899"),
                club.registerMember("พิมพ์ชนก ตบเร็ว", "0633334444"),
                club.registerMember("อรรถพล วิ่งไว", "0915556677"),
                club.registerMember("ศิริพร เสิร์ฟแม่น", "0848889900"),
                club.registerMember("เจษฎา คู่ผสม", "0802223344"),
                club.registerMember("ปวีณา รับลูกดี", "0976667788"),
                club.registerMember("ภานุวัฒน์ แบ็คแฮนด์", "0817654321"),
                club.registerMember("สุดารัตน์ ลูกโด่ง", "0891234567"),
                club.registerMember("ชยพล ตาข่าย", "0869876543"),
                club.registerMember("รัตนาภรณ์ ไม้เบา", "0951237890"));

        Random random = new Random(2026);
        LocalDate today = LocalDate.now();
        int nextId = 1;
        for (LocalDate day = today.minusDays(90); !day.isAfter(today.plusDays(3)); day = day.plusDays(1)) {
            LocalDate paidOn = day.isAfter(today) ? today : day; // จองล่วงหน้า = จ่ายวันนี้

            if (!day.isAfter(today) && random.nextInt(7) == 0) {
                club.addPackage(members.get(random.nextInt(members.size())), day, CASHIER);
            }

            int open = openHour(day), close = closeHour(day);
            int attempts = day.isAfter(today) ? 4 : isWeekend(day) || isHoliday(day) ? 14 : 8;
            for (int i = 0; i < attempts; i++) {
                int hours = 1 + random.nextInt(3);
                int court = 1 + random.nextInt(COURTS);
                int start = open + random.nextInt(close - open - hours + 1);
                if (!club.isFree(court, day, start, hours)) continue;
                Member member = members.get(random.nextInt(members.size()));
                club.addBooking("B%05d".formatted(nextId++), member, court, day, start, hours,
                        null, CASHIER, paidOn.atTime(Math.min(start, 20), 0).minusHours(1));
            }

            if (!day.isAfter(today)) {
                for (Product product : Product.values()) {
                    int qty = random.nextInt(product == Product.RACKET ? 8 : 4) - (product == Product.RACKET ? 6 : 1);
                    if (qty > 0) club.addSale(product, qty, day, CASHIER);
                }
            }
        }
        return club;
    }
}
