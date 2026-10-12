package com.example.badmintonbookingsystem.service;

import com.example.badmintonbookingsystem.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.example.badmintonbookingsystem.model.ClubRules.*;

/**
 * ศูนย์กลางข้อมูลของสนาม: เก็บผู้ใช้ สมาชิก การจอง และการขาย
 * ทุกคำสั่งที่เปลี่ยนข้อมูลจะตรวจกฎก่อน ถ้าผิดกฎจะโยน IllegalArgumentException พร้อมเหตุผลภาษาไทย
 */
public class Club implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "club.dat";
    private static final String SLIP_DIR = "slips";
    private static final Comparator<Booking> NEWEST_FIRST =
            Comparator.comparing(Booking::date).thenComparing(Booking::startHour).reversed();

    private final List<User> users = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<PackagePurchase> packages = new ArrayList<>();
    private final List<ShopSale> shopSales = new ArrayList<>();
    private transient Path dataDir; // null = ไม่บันทึกลงดิสก์ (ใช้ในเทสต์)
    private transient String loadWarning;

    Club() {
    }

    // ───────────── โหลด / บันทึก ─────────────

    /** โหลดข้อมูลจากโฟลเดอร์ ถ้ายังไม่มีจะสร้างข้อมูลตัวอย่างไว้สาธิต */
    public static Club load(Path dataDir) {
        Path file = dataDir.resolve(DATA_FILE);
        Club club = null;
        String warning = null;
        if (Files.exists(file)) {
            try (var in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
                club = (Club) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // อ่านไม่ได้ (ไฟล์เสียหรือโครงสร้างคลาสเปลี่ยน) → เก็บไฟล์เดิมไว้ ไม่เขียนทับ แล้วเริ่มด้วยข้อมูลตัวอย่าง
                warning = "อ่านไฟล์ข้อมูลเดิมไม่ได้ จึงเก็บสำรองไว้ที่ " + backup(file).getFileName()
                        + " แล้วเริ่มใหม่ด้วยข้อมูลตัวอย่าง";
            }
        }
        if (club == null) club = DemoData.create();
        club.dataDir = dataDir;
        club.loadWarning = warning;
        club.save();
        return club;
    }

    /** ข้อความเตือนถ้าตอนโหลดต้องทิ้งข้อมูลเดิม (หน้าจอจะแจ้งผู้ใช้ ไม่ให้ข้อมูลหายเงียบ ๆ) */
    public Optional<String> loadWarning() {
        return Optional.ofNullable(loadWarning);
    }

    private static Path backup(Path file) {
        try {
            return Files.move(file, file.resolveSibling(DATA_FILE + ".broken-" + System.currentTimeMillis()));
        } catch (IOException e) {
            throw new UncheckedIOException("ไฟล์ข้อมูลเสียและย้ายไปสำรองไม่ได้: " + file, e);
        }
    }

    /** เขียนไฟล์ชั่วคราวก่อนแล้วค่อยแทนที่ ไฟล์จริงจึงไม่เสียถ้าโปรแกรมปิดกลางคัน */
    public void save() {
        if (dataDir == null) return;
        try {
            Files.createDirectories(dataDir);
            Path temp = dataDir.resolve(DATA_FILE + ".tmp");
            try (var out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))) {
                out.writeObject(this);
            }
            Files.move(temp, dataDir.resolve(DATA_FILE),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("บันทึกข้อมูลไม่สำเร็จ: " + e.getMessage(), e);
        }
    }

    /** บันทึกลงไฟล์ ถ้าไม่สำเร็จให้ย้อนสิ่งที่เพิ่งเปลี่ยนในหน่วยความจำ — กดลองใหม่จะได้ไม่เกิดรายการซ้ำ */
    private void saveOrUndo(Runnable undo) {
        try {
            save();
        } catch (UncheckedIOException e) {
            undo.run();
            throw e;
        }
    }

    // ───────────── ผู้ใช้ ─────────────

    void addUser(User user) {
        users.add(user);
    }

    public Optional<User> login(String username, String password) {
        return users.stream()
                .filter(u -> u.username().equalsIgnoreCase(username.trim()) && u.passwordMatches(password))
                .findFirst();
    }

    // ───────────── สมาชิก ─────────────

    public List<Member> members() {
        return Collections.unmodifiableList(members);
    }

    /** สมาชิกที่ตรงกับคำค้น (ชื่อ รหัส หรือเบอร์) — ข้อความว่างคืนทุกคน */
    public List<Member> findMembers(String text) {
        return members.stream().filter(m -> m.matches(text)).toList();
    }

    public Member registerMember(String name, String phone) {
        String cleanName = name == null ? "" : name.trim();
        String cleanPhone = phone == null ? "" : phone.replaceAll("[\\s-]", "");
        require(!cleanName.isEmpty(), "กรุณากรอกชื่อสมาชิก");
        require(cleanPhone.matches("0\\d{8,9}"), "เบอร์โทรต้องเป็นตัวเลข 9–10 หลัก และขึ้นต้นด้วย 0");
        require(members.stream().noneMatch(m -> m.phone().equals(cleanPhone)), "เบอร์นี้สมัครสมาชิกไว้แล้ว");

        Member member = new Member("M%03d".formatted(members.size() + 1), cleanName, cleanPhone);
        members.add(member);
        saveOrUndo(() -> members.remove(member));
        return member;
    }

    public PackagePurchase buyPackage(Member member, User cashier) {
        require(member != null, "กรุณาเลือกสมาชิกก่อนซื้อแพกเกจ");
        PackagePurchase purchase = addPackage(member, LocalDate.now(), cashier.displayName());
        saveOrUndo(() -> {
            packages.remove(purchase);
            member.addPackageHours(-PACKAGE_HOURS);
        });
        return purchase;
    }

    PackagePurchase addPackage(Member member, LocalDate paidOn, String recordedBy) {
        member.addPackageHours(PACKAGE_HOURS);
        PackagePurchase purchase = new PackagePurchase(member, paidOn, recordedBy);
        packages.add(purchase);
        return purchase;
    }

    // ───────────── การจอง ─────────────

    /** บันทึกการจอง: ตรวจสมาชิก เวลาทำการ สนามว่าง และสลิป แล้วคิดราคาตามกฎ */
    public Booking book(Member member, int court, LocalDate date, int start, int hours, Path slipFile, User cashier) {
        require(member != null, "กรุณาเลือกสมาชิก (ต้องเป็นสมาชิกจึงจองได้)");
        require(court >= 1 && court <= COURTS, "หมายเลขสนามต้องอยู่ระหว่าง 1–" + COURTS);
        require(date != null && hours >= 1, "กรุณาเลือกวันและช่วงเวลา");
        require(start >= openHour(date) && start + hours <= closeHour(date),
                "นอกเวลาทำการ (%s เปิด %02d:00–%02d:00)".formatted(dayType(date), openHour(date), closeHour(date)));
        require(!LocalDateTime.of(date, LocalTime.of(start, 0)).isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)),
                "ช่วงเวลานี้เลยมาแล้ว เลือกได้ตั้งแต่ชั่วโมงปัจจุบันเป็นต้นไป");
        require(isFree(court, date, start, hours), "สนาม " + court + " ช่วงเวลานี้มีคนจองแล้ว");
        Quote quote = ClubRules.quote(member, hours);
        // จ่ายด้วยชั่วโมงแพกเกจทั้งหมดไม่มียอดต้องโอน จึงไม่ต้องมีสลิป
        require(quote.amount() == 0 || slipFile != null, "กรุณาแนบสลิปการชำระเงิน");

        String id = "B%05d".formatted(bookings.size() + 1);
        String slip = slipFile == null ? null : storeSlip(id, slipFile);
        Booking booking = addBooking(id, member, court, date, start, hours, slip, cashier.displayName(), LocalDateTime.now());
        saveOrUndo(() -> {
            bookings.remove(booking);
            member.refund(booking.quote());
        });
        return booking;
    }

    Booking addBooking(String id, Member member, int court, LocalDate date, int start, int hours,
                       String slip, String recordedBy, LocalDateTime createdAt) {
        Quote quote = ClubRules.quote(member, hours);
        member.charge(quote);
        Booking booking = new Booking(id, member, court, date, start, hours, quote, slip, recordedBy, createdAt);
        bookings.add(booking);
        return booking;
    }

    public boolean isFree(int court, LocalDate date, int start, int hours) {
        return bookings.stream().noneMatch(b -> b.overlaps(court, date, start, start + hours));
    }

    public List<Booking> bookingsOn(LocalDate date) {
        return bookings.stream().filter(b -> b.date().equals(date)).toList();
    }

    /** ค้นหาการจอง: ข้อความตรงกับรหัสการจอง/รหัสสมาชิก/ชื่อ/เบอร์ ส่วน date และ court เป็น null ได้ (= ทั้งหมด) */
    public List<Booking> search(String text, LocalDate date, Integer court) {
        String q = text == null ? "" : text.trim().toLowerCase();
        return bookings.stream()
                .filter(b -> date == null || b.date().equals(date))
                .filter(b -> court == null || b.court() == court)
                .filter(b -> b.id().toLowerCase().contains(q) || b.member().matches(q))
                .sorted(NEWEST_FIRST)
                .toList();
    }

    public List<Booking> historyOf(Member member) {
        return bookings.stream().filter(b -> b.member().id().equals(member.id())).sorted(NEWEST_FIRST).toList();
    }

    public double totalSpentBy(Member member) {
        return Stream.concat(historyOf(member).stream(), packages.stream().filter(p -> p.member().id().equals(member.id())))
                .mapToDouble(Billable::amount).sum();
    }

    public Optional<Path> slipOf(Booking booking) {
        if (booking.slip() == null || dataDir == null) return Optional.empty();
        return Optional.of(dataDir.resolve(SLIP_DIR).resolve(booking.slip())).filter(Files::exists);
    }

    private String storeSlip(String bookingId, Path source) {
        String fileName = source.getFileName().toString();
        // ใส่เวลาในชื่อไฟล์ ถ้ารหัสการจองเริ่มนับใหม่ (รีเซ็ตข้อมูล) จะได้ไม่ทับสลิปเก่า
        String name = bookingId + "-" + System.currentTimeMillis() + fileName.substring(Math.max(0, fileName.lastIndexOf('.')));
        if (dataDir == null) return name;
        try {
            Path dir = Files.createDirectories(dataDir.resolve(SLIP_DIR));
            Files.copy(source, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            return name;
        } catch (IOException e) {
            throw new UncheckedIOException("คัดลอกไฟล์สลิปไม่สำเร็จ: " + e.getMessage(), e);
        }
    }

    // ───────────── เช่า / ขายอุปกรณ์ ─────────────

    /** ขายตามตะกร้า (สินค้า → จำนวน) คืนยอดรวม */
    public double sell(Map<Product, Integer> cart, User cashier) {
        require(!cart.isEmpty(), "ตะกร้ายังว่างอยู่");
        require(cart.values().stream().allMatch(qty -> qty > 0), "จำนวนสินค้าต้องมากกว่า 0");
        List<ShopSale> added = new ArrayList<>();
        for (var line : cart.entrySet()) {
            added.add(addSale(line.getKey(), line.getValue(), LocalDate.now(), cashier.displayName()));
        }
        saveOrUndo(() -> shopSales.removeAll(added));
        return added.stream().mapToDouble(ShopSale::amount).sum();
    }

    ShopSale addSale(Product product, int quantity, LocalDate paidOn, String recordedBy) {
        ShopSale sale = new ShopSale(product, quantity, paidOn, recordedBy);
        shopSales.add(sale);
        return sale;
    }

    // ───────────── รายงาน ─────────────

    public List<Booking> bookings() {
        return Collections.unmodifiableList(bookings);
    }

    /** ทุกรายการที่มีรายได้ — การจอง แพกเกจ และร้านค้า มองเป็น Billable เหมือนกันหมด */
    public List<Billable> transactions() {
        return Stream.<List<? extends Billable>>of(bookings, packages, shopSales)
                .<Billable>flatMap(List::stream)
                .toList();
    }

    /** รายได้ตั้งแต่ from ถึง to (รวมทั้งสองวัน) แยกตามประเภท */
    public Map<RevenueType, Double> revenue(LocalDate from, LocalDate to) {
        Map<RevenueType, Double> sum = new EnumMap<>(RevenueType.class);
        for (RevenueType type : RevenueType.values()) sum.put(type, 0.0);
        for (Billable b : transactions()) {
            if (!b.paidOn().isBefore(from) && !b.paidOn().isAfter(to)) sum.merge(b.type(), b.amount(), Double::sum);
        }
        return sum;
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalArgumentException(message);
    }
}
