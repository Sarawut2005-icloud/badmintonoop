package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.Booking;
import com.example.badmintonbookingsystem.model.Member;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** หน้าประวัติสมาชิก (แอดมิน): เลือกสมาชิกทางซ้าย ดูโปรไฟล์และประวัติการจองทางขวา */
public class HistoryPage extends Page {

    private final TextField filter = new TextField();
    private final ListView<Member> list = new ListView<>();
    private final VBox profile = new VBox(16);
    private final BookingTable table;

    public HistoryPage(Club club, User user) {
        super(club, user);
        table = new BookingTable(club, false);

        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Member member, boolean empty) {
                super.updateItem(member, empty);
                if (empty || member == null) {
                    setGraphic(null);
                    return;
                }
                HBox row = new HBox(10, Ui.avatar(member.name(), 34),
                        new VBox(0, Ui.label(member.name(), "list-name"), Ui.muted(member.id() + " · " + member.phone())));
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
        list.getSelectionModel().selectedItemProperty().addListener(obs -> showMember());
        filter.textProperty().addListener(obs -> refresh());

        VBox members = Ui.card(Ui.searchBox(filter, "ค้นหาสมาชิก"), list);
        members.setPrefWidth(300);
        members.setMinWidth(280);
        VBox.setVgrow(list, Priority.ALWAYS);

        profile.getStyleClass().addAll("card", "profile-card");
        VBox history = Ui.card(Ui.title("ประวัติการจองสนาม"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(history, Priority.ALWAYS);
        VBox right = new VBox(20, profile, history);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox body = new HBox(20, members, right);
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().add(body);
    }

    @Override
    public void refresh() {
        Member selected = list.getSelectionModel().getSelectedItem();
        list.getItems().setAll(club.findMembers(filter.getText()));
        if (selected != null && list.getItems().contains(selected)) list.getSelectionModel().select(selected);
        else list.getSelectionModel().selectFirst();
        showMember();
    }

    private void showMember() {
        Member member = list.getSelectionModel().getSelectedItem();
        if (member == null) {
            profile.getChildren().setAll(Ui.muted("เลือกสมาชิกเพื่อดูประวัติ"));
            table.getItems().clear();
            return;
        }
        List<Booking> history = club.historyOf(member);
        int hours = history.stream().mapToInt(Booking::hours).sum();
        HBox identity = new HBox(16, Ui.avatar(member.name(), 56),
                new VBox(2, Ui.label(member.name(), "profile-title"), Ui.muted(member.id() + " · โทร " + member.phone())));
        identity.setAlignment(Pos.CENTER_LEFT);
        HBox stats = new HBox(12,
                stat("จองทั้งหมด", history.size() + " ครั้ง"),
                stat("ชั่วโมงรวม", hours + " ชม."),
                stat("แพกเกจคงเหลือ", member.packageHours() + " ชม."),
                stat("ยอดใช้จ่ายรวม", Ui.money(club.totalSpentBy(member))));
        profile.getChildren().setAll(identity, stats);
        table.getItems().setAll(history);
    }

    private static VBox stat(String caption, String value) {
        VBox stat = new VBox(2, Ui.muted(caption), Ui.label(value, "stat-value"));
        stat.getStyleClass().add("stat");
        stat.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(stat, Priority.ALWAYS);
        return stat;
    }
}
