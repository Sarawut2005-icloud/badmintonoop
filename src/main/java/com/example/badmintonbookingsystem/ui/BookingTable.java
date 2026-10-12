package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.Booking;
import com.example.badmintonbookingsystem.model.Quote;
import com.example.badmintonbookingsystem.service.Club;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

/** ตารางรายการจอง ใช้ร่วมกันในหน้า "ค้นหาการจอง" และ "ประวัติสมาชิก" */
public class BookingTable extends TableView<Booking> {

    public BookingTable(Club club, boolean showMember) {
        add("รหัส", Booking::id);
        add("วันที่เล่น", b -> Ui.shortDate(b.date()));
        add("เวลา", Booking::timeRange);
        add("สนาม", Booking::court);
        if (showMember) {
            add("สมาชิก", b -> b.member().name());
            add("เบอร์โทร", b -> b.member().phone());
        }
        add("ชม.", Booking::hours);
        add("ชำระ", b -> Ui.money(b.amount()) + paymentNote(b.quote()));
        add("ผู้บันทึก", Booking::recordedBy);
        getColumns().add(slipColumn(club));

        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setPlaceholder(Ui.muted("ไม่พบรายการจอง"));
    }

    private void add(String title, Function<Booking, ?> value) {
        getColumns().add(Ui.column(title, value));
    }

    private static String paymentNote(Quote quote) {
        if (quote.packageHours() > 0) return "  · แพกเกจ";
        if (quote.discountedHours() > 0) return "  · ลด 70%";
        return "";
    }

    private static TableColumn<Booking, Booking> slipColumn(Club club) {
        TableColumn<Booking, Booking> column = new TableColumn<>("สลิป");
        column.setSortable(false);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(col -> new TableCell<>() {
            private final Button view = Ui.button("ดูสลิป", Ui.EYE, "btn-link");

            @Override
            protected void updateItem(Booking booking, boolean empty) {
                super.updateItem(booking, empty);
                Optional<Path> file = empty || booking == null ? Optional.empty() : club.slipOf(booking);
                view.setOnAction(e -> file.ifPresent(path -> Ui.showImage(this, path, "สลิปการจอง " + booking.id())));
                setGraphic(file.isPresent() ? view : empty ? null : Ui.label("—", "muted"));
            }
        });
        return column;
    }
}
