package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.Member;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Function;

import static com.example.badmintonbookingsystem.model.ClubRules.*;

/** หน้าสมาชิก: สมัครสมาชิกใหม่ ขายแพกเกจ และดูสิทธิ์ส่วนลดของแต่ละคน */
public class MemberPage extends Page {

    private final TextField name = new TextField();
    private final TextField phone = new TextField();
    private final TextField filter = new TextField();
    private final TableView<Member> table = new TableView<>();

    public MemberPage(Club club, User user) {
        super(club, user);

        name.setPromptText("เช่น สมชาย ใจดี");
        phone.setPromptText("เช่น 0812345678");
        Button register = Ui.button("สมัครสมาชิก", Ui.ADD, "btn-primary");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(e -> register());
        name.setOnAction(e -> phone.requestFocus()); // กด Enter ไปช่องถัดไป แล้ว Enter อีกครั้งเพื่อสมัคร
        phone.setOnAction(e -> register());
        VBox registerCard = Ui.card(Ui.title("สมัครสมาชิกใหม่"),
                Ui.fieldLabel("ชื่อ-นามสกุล"), name, Ui.fieldLabel("เบอร์โทรศัพท์"), phone, register);

        Button buy = Ui.button("ขายแพกเกจให้สมาชิกที่เลือก", Ui.TICKET, "btn-light");
        buy.setMaxWidth(Double.MAX_VALUE);
        buy.setOnAction(e -> buyPackage());
        VBox packageCard = new VBox(6,
                Ui.label("แพกเกจเหมาจ่าย", "package-caption"),
                Ui.label(PACKAGE_HOURS + " ชั่วโมง", "package-hours"),
                Ui.label(Ui.money(PACKAGE_PRICE), "package-price"),
                Ui.label("เฉลี่ย %s/ชม. · ประหยัด %s".formatted(Ui.money(PACKAGE_PRICE / PACKAGE_HOURS),
                        Ui.money(PACKAGE_HOURS * HOURLY_RATE - PACKAGE_PRICE)), "package-note"),
                Ui.spacer(), buy);
        packageCard.getStyleClass().add("package-card");

        VBox left = new VBox(20, registerCard, packageCard);
        left.setPrefWidth(320);
        left.setMinWidth(300);

        add("รหัส", Member::id);
        add("ชื่อ-นามสกุล", Member::name);
        add("เบอร์โทร", Member::phone);
        add("ชม. สะสม", Member::paidHours);
        add("แพกเกจคงเหลือ", m -> m.packageHours() + " ชม.");
        add("สิทธิ์ลด 70%", m -> m.hoursUntilDiscount() == 1 ? "ชั่วโมงถัดไปได้ลด!" : "อีก " + m.hoursUntilDiscount() + " ชม.");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(Ui.muted("ยังไม่มีสมาชิก"));
        filter.textProperty().addListener(obs -> refresh());

        VBox listCard = Ui.card(Ui.row(Ui.title("รายชื่อสมาชิก"), Ui.searchBox(filter, "ค้นหาชื่อหรือเบอร์")), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(listCard, Priority.ALWAYS);

        HBox body = new HBox(20, left, listCard);
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().add(body);
    }

    private void add(String title, Function<Member, ?> value) {
        table.getColumns().add(Ui.column(title, value));
    }

    @Override
    public void refresh() {
        Member selected = table.getSelectionModel().getSelectedItem();
        table.getItems().setAll(club.findMembers(filter.getText()));
        table.getSelectionModel().select(selected);
    }

    private void register() {
        Ui.submit(this, () -> {
            Member member = club.registerMember(name.getText(), phone.getText());
            name.clear();
            phone.clear();
            filter.clear(); // ไม่งั้นสมาชิกใหม่อาจถูกตัวกรองซ่อนไว้
            refresh();
            table.getSelectionModel().select(member);
            table.scrollTo(member);
            return "สมัครสมาชิกสำเร็จ · " + member.id() + " " + member.name();
        });
    }

    private void buyPackage() {
        Member member = table.getSelectionModel().getSelectedItem();
        if (member == null) {
            Ui.toast(this, "เลือกสมาชิกในตารางก่อน", false);
            return;
        }
        if (!Ui.confirm(this, "ขายแพกเกจให้ " + member.name() + "?",
                "%d ชั่วโมง ราคา %s".formatted(PACKAGE_HOURS, Ui.money(PACKAGE_PRICE)))) return;
        Ui.submit(this, () -> {
            club.buyPackage(member, user);
            refresh();
            return "ขายแพกเกจแล้ว · " + member.name() + " มีชั่วโมงแพกเกจ " + member.packageHours() + " ชม.";
        });
    }
}
