package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.ClubRules;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.function.Consumer;

/** หน้าเข้าสู่ระบบ — พื้นหลังเป็นเส้นสนามแบดมินตันจริงตามสัดส่วน */
public class LoginView extends StackPane {

    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label error = Ui.label("", "error-text");

    public LoginView(Club club, Consumer<User> onLogin) {
        getStyleClass().add("login-bg");

        Button login = Ui.button("เข้าสู่ระบบ", null, "btn-primary");
        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> club.login(username.getText(), password.getText())
                .ifPresentOrElse(onLogin, () -> error.setText("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง")));

        username.setPromptText("ชื่อผู้ใช้");
        password.setPromptText("รหัสผ่าน");
        VBox form = new VBox(10,
                Ui.label("ยินดีต้อนรับ", "login-title"),
                Ui.muted("เข้าสู่ระบบเพื่อเริ่มใช้งาน"),
                Ui.spacer(),
                Ui.fieldLabel("ชื่อผู้ใช้"), username,
                Ui.fieldLabel("รหัสผ่าน"), password,
                error, login,
                Ui.spacer(),
                Ui.muted("บัญชีทดลอง (คลิกเพื่อกรอก)"),
                new HBox(8, demoAccount("cashier"), demoAccount("admin")));
        form.getStyleClass().add("login-form");

        HBox card = new HBox(brandPanel(), form);
        card.getStyleClass().add("login-card");
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        getChildren().addAll(courtLines(), card);
    }

    private VBox brandPanel() {
        VBox facts = new VBox(12,
                fact(Ui.COURT, ClubRules.COURTS + " สนามมาตรฐาน"),
                fact(Ui.MONEY, Ui.money(ClubRules.HOURLY_RATE) + " / ชั่วโมง"),
                fact(Ui.CLOCK, "จันทร์–ศุกร์ 13:00–21:00"),
                fact(Ui.CALENDAR, "เสาร์–อาทิตย์ และวันหยุดนักขัตฤกษ์ 10:00–20:00"));
        VBox brand = new VBox(14, Ui.logo(64),
                Ui.label("Badminton Club", "login-brand-name"),
                Ui.label("ระบบจองสนามแบดมินตัน", "login-brand-sub"),
                Ui.spacer(), facts);
        brand.getStyleClass().add("login-brand");
        return brand;
    }

    private static Label fact(String glyph, String text) {
        Label fact = new Label(text, Ui.icon(glyph));
        fact.getStyleClass().add("login-fact");
        return fact;
    }

    private Button demoAccount(String name) {
        Button chip = Ui.button(name + " / 1234", null, "btn-chip");
        chip.setOnAction(e -> {
            username.setText(name);
            password.setText("1234");
            error.setText("");
        });
        return chip;
    }

    /** เส้นสนามแบดมินตันตามขนาดจริง 13.40 × 6.10 เมตร (1 เมตร = 70 px) */
    private static Node courtLines() {
        double s = 70, length = 13.4 * s, width = 6.1 * s, net = length / 2;
        double side = 0.46 * s, shortService = 1.98 * s, longService = 0.76 * s;
        String path = "M0 0 H%1$s V%2$s H0 Z".formatted(length, width)                                 // เส้นนอก (คู่)
                + " M0 %1$s H%2$s M0 %3$s H%2$s".formatted(side, length, width - side)               // เส้นข้างประเภทเดี่ยว
                + " M%1$s 0 V%2$s".formatted(net, width)                                             // ตาข่าย
                + " M%1$s 0 V%3$s M%2$s 0 V%3$s".formatted(net - shortService, net + shortService, width) // เส้นเสิร์ฟสั้น
                + " M%1$s 0 V%3$s M%2$s 0 V%3$s".formatted(longService, length - longService, width)    // เส้นเสิร์ฟยาวประเภทคู่
                + " M0 %1$s H%2$s M%3$s %1$s H%4$s".formatted(width / 2, net - shortService, net + shortService, length); // เส้นกลาง
        SVGPath court = new SVGPath();
        court.setContent(path);
        court.setFill(null);
        court.setStroke(Color.rgb(255, 255, 255, 0.10));
        court.setStrokeWidth(3);
        court.setRotate(-14);
        court.setScaleX(1.35);
        court.setScaleY(1.35);
        court.setMouseTransparent(true);
        return court;
    }
}
