package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.*;
import com.example.badmintonbookingsystem.service.Club;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.example.badmintonbookingsystem.model.ClubRules.*;

/**
 * หน้าจองสนาม: ตาราง 12 สนาม × ชั่วโมงที่เปิด
 * คลิกช่องว่างเพื่อเลือก คลิกอีกช่องในสนามเดียวกันเพื่อขยายช่วงเวลา แล้วกรอกสมาชิกและแนบสลิปทางขวา
 */
public class BookingPage extends Page {

    private final DatePicker datePicker = Ui.datePicker(LocalDate.now());
    private final Label dayInfo = Ui.label("", "chip", "chip-green");
    private final Button prev = Ui.iconButton(Ui.LEFT, "วันก่อนหน้า");
    private final GridPane grid = new GridPane();
    // วาดตารางใหม่ทุก 30 วินาทีขณะเปิดหน้านี้ ช่องที่เลยเวลาจะได้ปิดเองโดยไม่ต้องกดอะไร
    private final Timeline clock = new Timeline(new KeyFrame(Duration.seconds(30), e -> redraw()));

    // ช่วงที่เลือก: สนาม selCourt ชั่วโมง [selStart, selEnd) · selCourt = 0 คือยังไม่เลือก
    private int selCourt, selStart, selEnd;

    private final Label selTitle = Ui.label("", "selection-title");
    private final Label selDetail = Ui.note("");
    private final TextField memberSearch = new TextField();
    private final ComboBox<Member> memberBox = new ComboBox<>();
    private final Label memberInfo = Ui.note("");
    private final VBox priceLines = new VBox(8);
    private final Label total = Ui.label("", "total-value");
    private final ImageView slipPreview = new ImageView();
    private final Label slipName = Ui.muted("");
    private final Label slipHint = Ui.label("", "slip-hint");
    private final Button confirm = Ui.button("ยืนยันการจอง", Ui.CHECK, "btn-primary");
    private Path slip;
    private Member slipOwner; // สลิปเป็นของสมาชิกคนไหน เปลี่ยนคนแล้วต้องแนบใหม่ (กันสลิปติดไปกับลูกค้าคนถัดไป)

    public BookingPage(Club club, User user) {
        super(club, user);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        datePicker.valueProperty().addListener(obs -> {
            selCourt = 0;
            redraw();
        });
        memberSearch.textProperty().addListener(obs -> filterMembers());
        memberBox.valueProperty().addListener(obs -> updateForm());

        clock.setCycleCount(Animation.INDEFINITE);
        sceneProperty().addListener((obs, before, scene) -> {
            if (scene == null) clock.stop();
            else clock.play();
        });

        HBox body = new HBox(20, scheduleCard(), formCard());
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().add(body);
        clearSlip();
    }

    /** เรียกตอนเปิดหน้า: โหลดรายชื่อสมาชิกใหม่ (อาจเพิ่งสมัครจากหน้าสมาชิก) แล้ววาดตาราง */
    @Override
    public void refresh() {
        filterMembers();
        redraw();
    }

    private void redraw() {
        LocalDate today = LocalDate.now();
        if (datePicker.getValue().isBefore(today)) {
            datePicker.setValue(today); // ข้ามเที่ยงคืนขณะเปิดหน้าไว้ — listener จะวาดใหม่ให้
            return;
        }
        LocalDate date = datePicker.getValue();
        if (selCourt > 0 && (isPast(date, selStart) || !club.isFree(selCourt, date, selStart, selEnd - selStart))) {
            selCourt = 0;
        }
        prev.setDisable(!date.isAfter(today));
        dayInfo.setText(dayType(date) + "  ·  เปิด " + Ui.hour(openHour(date)) + " – " + Ui.hour(closeHour(date)));
        buildGrid(date);
        updateForm();
    }

    // ───────────── ตารางสนาม ─────────────

    private VBox scheduleCard() {
        prev.setOnAction(e -> datePicker.setValue(datePicker.getValue().minusDays(1)));
        Button next = Ui.iconButton(Ui.RIGHT, "วันถัดไป");
        next.setOnAction(e -> datePicker.setValue(datePicker.getValue().plusDays(1)));
        Button today = Ui.button("วันนี้", null, "btn-ghost");
        today.setOnAction(e -> datePicker.setValue(LocalDate.now()));

        dayInfo.setMinWidth(Region.USE_PREF_SIZE);
        HBox toolbar = new HBox(8, prev, datePicker, next, today, Ui.spacer(), dayInfo);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox legend = new HBox(18, legend("slot-legend-free", "ว่าง"), legend("slot-legend-selected", "ที่เลือก"),
                legend("slot-legend-booked", "จองแล้ว"), legend("slot-legend-past", "เลยเวลาแล้ว"));

        grid.setHgap(4);
        grid.setVgap(4);
        grid.getStyleClass().add("court-grid");
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox card = Ui.card(toolbar, scroll, legend);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private static HBox legend(String style, String text) {
        Region dot = new Region();
        dot.getStyleClass().addAll("legend-dot", style);
        HBox legend = new HBox(6, dot, Ui.muted(text));
        legend.setAlignment(Pos.CENTER_LEFT);
        return legend;
    }

    private void buildGrid(LocalDate date) {
        int open = openHour(date), close = closeHour(date);
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().setAll(new RowConstraints());
        for (int court = 1; court <= COURTS; court++) grid.getRowConstraints().add(new RowConstraints(40));
        grid.getColumnConstraints().add(new ColumnConstraints(62));
        for (int h = open; h < close; h++) {
            ColumnConstraints column = new ColumnConstraints(48, 90, Double.MAX_VALUE);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
            grid.add(Ui.label(Ui.hour(h), "grid-head"), h - open + 1, 0);
        }

        List<Booking> bookings = club.bookingsOn(date);
        for (int court = 1; court <= COURTS; court++) {
            grid.add(Ui.label("สนาม " + court, "court-label"), 0, court);
            for (int h = open; h < close; ) {
                Optional<Booking> booked = bookingAt(bookings, court, h);
                int span;
                Node cell;
                if (booked.isPresent()) {
                    span = booked.get().endHour() - h;
                    cell = bookingBlock(booked.get());
                } else if (court == selCourt && h == selStart) {
                    span = selEnd - selStart;
                    cell = selectionBlock();
                } else {
                    span = 1;
                    cell = freeSlot(court, h, isPast(date, h));
                }
                grid.add(cell, h - open + 1, court, span, 1);
                h += span;
            }
        }
    }

    private static Optional<Booking> bookingAt(List<Booking> bookings, int court, int hour) {
        return bookings.stream().filter(b -> b.court() == court && b.startHour() <= hour && hour < b.endHour()).findFirst();
    }

    /** ชั่วโมงที่ผ่านไปแล้วจองไม่ได้ (ชั่วโมงปัจจุบันยังจองได้สำหรับลูกค้า walk-in) — ตรงกับกฎใน Club.book */
    private static boolean isPast(LocalDate date, int hour) {
        LocalDate today = LocalDate.now();
        return date.isBefore(today) || date.equals(today) && hour < LocalTime.now().getHour();
    }

    private Button freeSlot(int court, int hour, boolean past) {
        Button slot = new Button();
        slot.getStyleClass().add(past ? "slot-past" : "slot");
        slot.setDisable(past);
        slot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        slot.setOnAction(e -> select(court, hour));
        return slot;
    }

    private Button selectionBlock() {
        Button block = new Button(Ui.hour(selStart) + " – " + Ui.hour(selEnd));
        block.getStyleClass().add("selection-block");
        block.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        block.setTooltip(new Tooltip("คลิกเพื่อยกเลิกการเลือก"));
        block.setOnAction(e -> {
            selCourt = 0;
            redraw();
        });
        return block;
    }

    private static VBox bookingBlock(Booking booking) {
        String firstName = booking.member().name().split(" ")[0]; // ช่อง 1 ชั่วโมงแคบ ใช้ชื่อต้นพอ ชื่อเต็มอยู่ใน tooltip
        VBox block = new VBox(0, Ui.label(firstName, "booking-name"),
                Ui.label(booking.id() + " · " + booking.hours() + " ชม.", "booking-meta"));
        block.getStyleClass().add("booking-block");
        block.setAlignment(Pos.CENTER_LEFT);
        block.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Tooltip.install(block, new Tooltip("%s\n%s · %s\nสนาม %d · %s".formatted(booking.id(), booking.member().name(),
                booking.member().phone(), booking.court(), booking.timeRange())));
        return block;
    }

    private void select(int court, int hour) {
        LocalDate date = datePicker.getValue();
        if (isPast(date, hour)) { // ช่องอาจเลยเวลาไปแล้วระหว่างรอวาดตารางรอบถัดไป
            redraw();
            return;
        }
        int start = Math.min(selStart, hour), end = Math.max(selEnd, hour + 1);
        if (court == selCourt && club.isFree(court, date, start, end - start)) {
            selStart = start;
            selEnd = end;
        } else {
            selCourt = court;
            selStart = hour;
            selEnd = hour + 1;
        }
        redraw();
    }

    // ───────────── ฟอร์มด้านขวา ─────────────

    private VBox formCard() {
        memberBox.setPromptText("เลือกสมาชิก");
        memberBox.setMaxWidth(Double.MAX_VALUE);
        VBox selection = new VBox(4, selTitle, selDetail);
        selection.getStyleClass().add("selection-box");

        Button attach = Ui.button("แนบสลิป", Ui.ATTACH, "btn-ghost");
        attach.setOnAction(e -> chooseSlip());
        slipPreview.setFitWidth(56);
        slipPreview.setFitHeight(56);
        slipPreview.setPreserveRatio(true);
        StackPane preview = new StackPane(Ui.icon(Ui.ATTACH), slipPreview);
        preview.getStyleClass().add("slip-preview");
        HBox slipRow = new HBox(12, preview, new VBox(4, attach, slipName));
        slipRow.setAlignment(Pos.CENTER_LEFT);

        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setOnAction(e -> confirmBooking());

        VBox card = Ui.card(
                Ui.title("รายละเอียดการจอง"), selection,
                Ui.fieldLabel("สมาชิก"), Ui.searchBox(memberSearch, "พิมพ์ชื่อ เบอร์ หรือรหัสสมาชิก"), memberBox, memberInfo,
                Ui.fieldLabel("ค่าบริการ"), priceLines,
                new Separator(), Ui.row(Ui.label("ยอดชำระ", "total-label"), total),
                Ui.row(Ui.fieldLabel("สลิปการชำระเงิน"), slipHint), slipRow,
                Ui.spacer(), confirm);
        card.setPrefWidth(310);
        card.setMinWidth(300);
        return card;
    }

    /** กรองรายชื่อตามช่องค้นหา ถ้าเหลือคนเดียวเลือกให้เลย */
    private void filterMembers() {
        Member selected = memberBox.getValue();
        List<Member> found = club.findMembers(memberSearch.getText());
        memberBox.getItems().setAll(found);
        if (found.size() == 1) memberBox.setValue(found.getFirst());
        else memberBox.setValue(found.contains(selected) ? selected : null);
    }

    private void updateForm() {
        boolean selected = selCourt > 0;
        LocalDate date = datePicker.getValue();
        selTitle.setText(selected ? "สนาม " + selCourt : "ยังไม่ได้เลือกเวลา");
        selDetail.setText(selected
                ? Ui.date(date) + "\n" + Ui.hour(selStart) + " – " + Ui.hour(selEnd) + "  (" + (selEnd - selStart) + " ชั่วโมง)"
                : "คลิกช่องว่างในตารางเพื่อเลือก\nคลิกช่องถัดไปในสนามเดียวกันเพื่อขยายเวลา");

        Member member = memberBox.getValue();
        if (slip != null && member != null) {
            if (slipOwner == null) slipOwner = member;
            else if (slipOwner != member) clearSlip();
        }
        memberInfo.setText(member == null ? "ต้องเป็นสมาชิกจึงจองได้ · สมัครได้ที่เมนู สมาชิก"
                : "สะสม %d ชม. · แพกเกจเหลือ %d ชม. · อีก %d ชม. ได้ลด 70%%"
                .formatted(member.paidHours(), member.packageHours(), member.hoursUntilDiscount()));

        priceLines.getChildren().clear();
        Quote quote = selected && member != null ? ClubRules.quote(member, selEnd - selStart) : null;
        if (quote != null) {
            if (quote.packageHours() > 0)
                priceLines.getChildren().add(priceLine("หักชั่วโมงแพกเกจ " + quote.packageHours() + " ชม.", 0, "tag-green"));
            if (quote.fullHours() > 0)
                priceLines.getChildren().add(priceLine(quote.fullHours() + " ชม. × " + Ui.money(HOURLY_RATE), quote.fullAmount(), null));
            if (quote.discountedHours() > 0)
                priceLines.getChildren().add(priceLine("ชั่วโมงที่ 20 ลด 70% · " + quote.discountedHours() + " ชม.", quote.discountedAmount(), "tag-amber"));
        } else {
            priceLines.getChildren().add(Ui.muted("เลือกเวลาและสมาชิกเพื่อดูราคา"));
        }
        double amount = quote == null ? 0 : quote.amount();
        total.setText(Ui.money(amount));

        // บอกล่วงหน้าว่าต้องแนบสลิปไหม แทนที่จะรอให้กดยืนยันแล้วค่อยแจ้ง error
        boolean slipNeeded = amount > 0;
        boolean slipMissing = slipNeeded && slip == null;
        slipHint.setText(quote == null ? "" : slipNeeded ? (slip == null ? "จำเป็น" : "แนบแล้ว ✓") : "ไม่ต้องแนบ (ใช้แพกเกจ)");
        slipHint.getStyleClass().removeAll("slip-hint-required", "slip-hint-ok");
        slipHint.getStyleClass().add(slipMissing ? "slip-hint-required" : "slip-hint-ok");
        confirm.setDisable(quote == null || slipMissing);
    }

    private static HBox priceLine(String text, double amount, String tag) {
        Label label = new Label(text);
        if (tag != null) label.getStyleClass().addAll("tag", tag);
        return Ui.row(label, new Label(Ui.money(amount)));
    }

    private void chooseSlip() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("เลือกไฟล์สลิปการชำระเงิน");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("รูปสลิป (PNG, JPG)", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file == null) return;
        slip = file.toPath();
        slipOwner = memberBox.getValue();
        slipName.setText(file.getName());
        slipPreview.setImage(new Image(file.toURI().toString(), 112, 112, true, true));
        updateForm();
    }

    private void clearSlip() {
        slip = null;
        slipOwner = null;
        slipName.setText("ยังไม่ได้แนบไฟล์");
        slipPreview.setImage(null);
    }

    private void confirmBooking() {
        Member member = memberBox.getValue();
        int hours = selEnd - selStart;
        double amount = ClubRules.quote(member, hours).amount();
        if (!Ui.confirm(this, "ยืนยันการจองสนาม " + selCourt + "?", "%s\n%s · %s – %s (%d ชม.)\nยอดชำระ %s".formatted(
                member.name(), Ui.date(datePicker.getValue()), Ui.hour(selStart), Ui.hour(selEnd), hours, Ui.money(amount)))) {
            return;
        }
        boolean saved = Ui.submit(this, () -> {
            Booking b = club.book(member, selCourt, datePicker.getValue(), selStart, hours, slip, user);
            return "จองสำเร็จ %s · สนาม %d · %s · %s".formatted(b.id(), b.court(), b.timeRange(), Ui.money(b.amount()));
        });
        if (!saved) return;
        selCourt = 0;
        clearSlip();
        redraw();
    }
}
