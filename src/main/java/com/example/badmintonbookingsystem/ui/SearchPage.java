package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.ClubRules;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** หน้าค้นหาการจอง — กรองทันทีที่พิมพ์หรือเปลี่ยนตัวกรอง */
public class SearchPage extends Page {

    private final TextField query = new TextField();
    private final DatePicker date = Ui.datePicker(null);
    private final ComboBox<String> court = new ComboBox<>();
    private final Label count = Ui.label("", "chip");
    private final BookingTable table;

    public SearchPage(Club club, User user) {
        super(club, user);
        table = new BookingTable(club, true);

        date.setPromptText("ทุกวัน");
        Button clearDate = Ui.iconButton(Ui.CLEAR, "ล้างวันที่ (ทุกวัน)");
        clearDate.setOnAction(e -> date.setValue(null));
        court.getItems().add("ทุกสนาม");
        for (int c = 1; c <= ClubRules.COURTS; c++) court.getItems().add("สนาม " + c);
        court.getSelectionModel().selectFirst();

        query.textProperty().addListener(obs -> refresh());
        date.valueProperty().addListener(obs -> refresh());
        court.valueProperty().addListener(obs -> refresh());

        HBox search = Ui.searchBox(query, "ค้นหาชื่อ เบอร์โทร รหัสสมาชิก หรือรหัสการจอง");
        HBox.setHgrow(search, Priority.ALWAYS);
        HBox filters = new HBox(10, search, date, clearDate, court);
        filters.setAlignment(Pos.CENTER_LEFT);

        VBox results = Ui.card(Ui.row(Ui.title("ผลการค้นหา"), count), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(results, Priority.ALWAYS);
        getChildren().addAll(Ui.card(filters), results);
    }

    @Override
    public void refresh() {
        int index = court.getSelectionModel().getSelectedIndex();
        table.getItems().setAll(club.search(query.getText(), date.getValue(), index <= 0 ? null : index));
        count.setText("พบ " + table.getItems().size() + " รายการ");
    }
}
