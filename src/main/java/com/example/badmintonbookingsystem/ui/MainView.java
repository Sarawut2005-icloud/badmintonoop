package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.Menu;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/** หน้าหลักหลังเข้าสู่ระบบ: แถบเมนูซ้าย + หัวเรื่อง + เนื้อหาของหน้าที่เลือก */
public class MainView extends StackPane {

    private final Club club;
    private final User user;
    private final Map<Menu, Page> pages = new EnumMap<>(Menu.class);
    private final Label title = Ui.label("", "page-title");
    private final Label subtitle = Ui.label("", "page-subtitle");
    private final StackPane content = new StackPane();

    public MainView(Club club, User user, Runnable onLogout) {
        this.club = club;
        this.user = user;

        VBox main = new VBox(header(), content);
        VBox.setVgrow(content, Priority.ALWAYS);
        BorderPane layout = new BorderPane(main);
        layout.setLeft(sidebar(onLogout));
        getChildren().add(layout);
    }

    private VBox sidebar(Runnable onLogout) {
        HBox brand = new HBox(12, Ui.logo(42), new VBox(0,
                Ui.label("Badminton Club", "brand-name"), Ui.label("ระบบจองสนามแบดมินตัน", "brand-sub")));
        brand.setAlignment(Pos.CENTER_LEFT);

        VBox nav = new VBox(4, Ui.label("เมนู", "nav-section"));
        ToggleGroup group = new ToggleGroup();
        for (Menu menu : user.menus()) {
            ToggleButton item = new ToggleButton(menu.title(), Ui.icon(iconOf(menu)));
            item.getStyleClass().add("nav-button");
            item.setMaxWidth(Double.MAX_VALUE);
            item.setToggleGroup(group);
            item.setOnAction(e -> show(menu));
            nav.getChildren().add(item);
        }
        // กดเมนูเดิมซ้ำต้องไม่ทำให้ไม่มีเมนูไหนถูกเลือก
        group.selectedToggleProperty().addListener((obs, before, now) -> {
            if (now == null) before.setSelected(true);
        });
        group.getToggles().getFirst().setSelected(true);
        show(user.menus().getFirst());

        Button logout = Ui.iconButton(Ui.LOGOUT, "ออกจากระบบ");
        logout.getStyleClass().add("logout");
        logout.setOnAction(e -> onLogout.run());
        HBox profile = new HBox(10, Ui.avatar(user.displayName(), 38),
                new VBox(1, Ui.label(user.displayName(), "profile-name"), Ui.label(user.roleName(), "profile-role")),
                Ui.spacer(), logout);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.getStyleClass().add("profile");

        VBox sidebar = new VBox(8, brand, nav, Ui.spacer(), profile);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private HBox header() {
        Label today = new Label(Ui.longDate(LocalDate.now()), Ui.icon(Ui.CALENDAR));
        today.getStyleClass().add("chip");
        HBox header = new HBox(new VBox(2, title, subtitle), Ui.spacer(), today);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        return header;
    }

    private void show(Menu menu) {
        Page page = pages.computeIfAbsent(menu, this::createPage);
        page.refresh();
        content.getChildren().setAll(page);
        title.setText(menu.title());
        subtitle.setText(menu.subtitle());
    }

    private Page createPage(Menu menu) {
        return switch (menu) {
            case BOOKING -> new BookingPage(club, user);
            case SEARCH -> new SearchPage(club, user);
            case MEMBERS -> new MemberPage(club, user);
            case SHOP -> new ShopPage(club, user);
            case REVENUE -> new RevenuePage(club, user);
            case HISTORY -> new HistoryPage(club, user);
        };
    }

    private static String iconOf(Menu menu) {
        return switch (menu) {
            case BOOKING -> Ui.CALENDAR;
            case SEARCH -> Ui.SEARCH;
            case MEMBERS -> Ui.PEOPLE;
            case SHOP -> Ui.SHOP;
            case REVENUE -> Ui.CHART;
            case HISTORY -> Ui.HISTORY;
        };
    }
}
