package com.example.badmintonbookingsystem;

import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import com.example.badmintonbookingsystem.ui.LoginView;
import com.example.badmintonbookingsystem.ui.MainView;
import com.example.badmintonbookingsystem.ui.Ui;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Locale;

/** จุดเริ่มโปรแกรม: โหลดข้อมูล แล้วสลับระหว่างหน้าเข้าสู่ระบบกับหน้าหลัก */
public class App extends Application {

    private Club club;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        Locale.setDefault(Ui.THAI); // ปฏิทินและชื่อเดือนเป็นภาษาไทย
        club = Club.load(Path.of("data"));

        scene = new Scene(new StackPane(), 1320, 840);
        scene.getStylesheets().add(Ui.STYLESHEET);
        showLogin();

        stage.setTitle("Badminton Club · ระบบจองสนามแบดมินตัน");
        stage.setMinWidth(1180);
        stage.setMinHeight(760);
        stage.setScene(scene);
        stage.show();
        club.loadWarning().ifPresent(message -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, message);
            alert.initOwner(stage);
            alert.setHeaderText("เปิดข้อมูลเดิมไม่ได้");
            alert.show();
        });
    }

    private void showLogin() {
        scene.setRoot(new LoginView(club, this::showMain));
    }

    private void showMain(User user) {
        scene.setRoot(new MainView(club, user, this::showLogin));
    }
}
