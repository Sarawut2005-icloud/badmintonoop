package com.example.badmintonbookingsystem.service;

import com.example.badmintonbookingsystem.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบกฎทางธุรกิจหลัก: เวลาเปิด-ปิด ราคา ส่วนลดชั่วโมงที่ 20 แพกเกจ และการจองซ้อน */
class ClubTest {

    // ใช้วันในอนาคตแบบตายตัว เพราะระบบไม่ให้จองย้อนหลัง (ถ้าเลยปี 2030 แล้วให้เลื่อนปีขึ้นไป)
    private static final LocalDate MONDAY = LocalDate.of(2030, 10, 14);
    private static final LocalDate SATURDAY = LocalDate.of(2030, 10, 19);
    private static final LocalDate CHULALONGKORN_DAY = LocalDate.of(2030, 10, 23); // วันพุธที่เป็นวันหยุด
    private static final Path SLIP = Path.of("slip.png");

    private final Club club = new Club(); // ไม่มี dataDir → ไม่เขียนไฟล์
    private final User cashier = new Cashier("c", "1", "แคชเชียร์");

    @Test
    void openingHoursDependOnDayType() {
        assertEquals(13, ClubRules.openHour(MONDAY));
        assertEquals(21, ClubRules.closeHour(MONDAY));
        assertEquals(10, ClubRules.openHour(SATURDAY));
        assertEquals(20, ClubRules.closeHour(SATURDAY));
        assertEquals(10, ClubRules.openHour(CHULALONGKORN_DAY));
        assertEquals(10, ClubRules.openHour(LocalDate.of(2026, 3, 3)), "มาฆบูชาเป็นวันหยุดตามจันทรคติ");
    }

    @Test
    void twentiethPaidHourCostsThirtyPercent() {
        Member member = club.registerMember("ทดสอบ", "0811111111");
        club.book(member, 1, MONDAY, 13, 3, SLIP, cashier);
        club.book(member, 2, MONDAY, 13, 8, SLIP, cashier);
        club.book(member, 3, MONDAY, 13, 8, SLIP, cashier); // สะสมครบ 19 ชม.
        assertEquals(1, member.hoursUntilDiscount());

        Quote quote = ClubRules.quote(member, 2); // ชม.ที่ 20 ลด + ชม.ที่ 21 ราคาปกติ
        assertEquals(1, quote.discountedHours());
        assertEquals(180 + 54, quote.amount(), 0.001);
    }

    @Test
    void packageHoursAreUsedFirstAndNeedNoSlip() {
        Member member = club.registerMember("ทดสอบ", "0811111111");
        club.buyPackage(member, cashier);

        Booking booking = club.book(member, 1, MONDAY, 13, 2, null, cashier);
        assertEquals(0, booking.amount());
        assertEquals(ClubRules.PACKAGE_HOURS - 2, member.packageHours());
        assertEquals(0, member.paidHours(), "ชั่วโมงแพกเกจไม่นับสะสมส่วนลด");
    }

    @Test
    void rejectsInvalidBookings() {
        Member member = club.registerMember("ทดสอบ", "0811111111");
        club.book(member, 5, MONDAY, 18, 2, SLIP, cashier);

        assertThrows(IllegalArgumentException.class, () -> club.book(member, 5, MONDAY, 19, 1, SLIP, cashier), "จองซ้อน");
        assertThrows(IllegalArgumentException.class, () -> club.book(member, 1, MONDAY, 10, 1, SLIP, cashier), "ก่อนเปิด");
        assertThrows(IllegalArgumentException.class, () -> club.book(member, 1, MONDAY, 20, 2, SLIP, cashier), "เกินเวลาปิด");
        assertThrows(IllegalArgumentException.class, () -> club.book(member, 13, MONDAY, 13, 1, SLIP, cashier), "ไม่มีสนาม 13");
        assertThrows(IllegalArgumentException.class, () -> club.book(null, 1, MONDAY, 13, 1, SLIP, cashier), "ไม่ใช่สมาชิก");
        assertThrows(IllegalArgumentException.class, () -> club.book(member, 1, MONDAY, 13, 1, null, cashier), "ไม่แนบสลิป");
        assertDoesNotThrow(() -> club.book(member, 5, MONDAY, 20, 1, SLIP, cashier), "ต่อท้ายพอดีได้");
    }

    @Test
    void revenueSumsEveryBillableByType() {
        Member member = club.registerMember("ทดสอบ", "0811111111");
        club.book(member, 1, MONDAY, 13, 1, SLIP, cashier);
        club.buyPackage(member, cashier);
        club.addSale(Product.SHOES_RENTAL, 2, LocalDate.now(), "c");

        var revenue = club.revenue(LocalDate.now(), LocalDate.now());
        assertEquals(180, revenue.get(RevenueType.COURT), 0.001);
        assertEquals(2600, revenue.get(RevenueType.PACKAGE), 0.001);
        assertEquals(100, revenue.get(RevenueType.RENTAL), 0.001);
        assertEquals(0, revenue.get(RevenueType.SALE), 0.001);
    }

    @Test
    void searchFindsByNamePhoneAndId() {
        Member member = club.registerMember("สมหญิง ตบแรง", "0899999999");
        Booking booking = club.book(member, 4, MONDAY, 13, 1, SLIP, cashier);

        assertEquals(1, club.search("ตบแรง", null, null).size());
        assertEquals(1, club.search("0899", MONDAY, 4).size());
        assertEquals(1, club.search(booking.id().toLowerCase(), null, null).size());
        assertTrue(club.search("ไม่มีชื่อนี้", null, null).isEmpty());
    }

    @Test
    void rejectsTimeThatHasAlreadyPassed() {
        Member member = club.registerMember("ทดสอบ", "0811111111");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> club.book(member, 1, yesterday, ClubRules.openHour(yesterday), 1, SLIP, cashier));
    }

    @Test
    void phoneSearchIgnoresDashesAndSpaces() {
        Member member = club.registerMember("สมหญิง ตบแรง", "0899999999");
        club.book(member, 4, MONDAY, 13, 1, SLIP, cashier);

        assertEquals(1, club.search("089-999", null, null).size());
        assertEquals(1, club.findMembers("089 999 9999").size());
    }

    @Test
    void saveAndLoadKeepsEverything(@TempDir Path dir) throws IOException {
        Path slip = Files.writeString(dir.resolve("slip.png"), "fake image");
        Club first = Club.load(dir);
        Member member = first.registerMember("บันทึก แล้วโหลด", "0822222222");
        Booking booking = first.book(member, 1, MONDAY, 13, 2, slip, cashier);

        Club reloaded = Club.load(dir);
        assertTrue(reloaded.loadWarning().isEmpty());
        assertEquals(first.members().size(), reloaded.members().size());
        Booking again = reloaded.search(booking.id(), null, null).getFirst();
        Member sameMember = reloaded.findMembers("0822222222").getFirst();
        assertSame(sameMember, again.member(), "หลังโหลด การจองต้องชี้ไปที่สมาชิกตัวเดียวกับในรายชื่อ");
        assertEquals(member.paidHours(), sameMember.paidHours());
        assertTrue(reloaded.slipOf(again).isPresent(), "ไฟล์สลิปต้องถูกคัดลอกเก็บไว้");
    }

    @Test
    void corruptedFileIsBackedUpNotLost(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("club.dat"), "not a real data file");

        Club club = Club.load(dir);
        assertTrue(club.loadWarning().isPresent());
        try (var files = Files.list(dir)) {
            assertTrue(files.anyMatch(f -> f.getFileName().toString().startsWith("club.dat.broken-")));
        }
    }
}
