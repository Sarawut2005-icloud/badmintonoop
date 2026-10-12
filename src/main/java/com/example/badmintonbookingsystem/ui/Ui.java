package com.example.badmintonbookingsystem.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** ชิ้นส่วนหน้าจอที่ใช้ซ้ำทุกหน้า: ไอคอน การ์ด ปุ่ม คอลัมน์ตาราง การแจ้งเตือน และรูปแบบเงิน/วันที่ */
public final class Ui {

    public static final String STYLESHEET =
            Objects.requireNonNull(Ui.class.getResource("/com/example/badmintonbookingsystem/style.css")).toExternalForm();
    public static final Locale THAI = Locale.forLanguageTag("th-TH");

    // ไอคอนจากฟอนต์ Segoe MDL2 Assets ที่มากับ Windows — ไม่ต้องมีไฟล์รูป
    public static final String CALENDAR = "\uE787", SEARCH = "\uE721", PEOPLE = "\uE716", SHOP = "\uE719",
            CHART = "\uE9D2", HISTORY = "\uE81C", LOGOUT = "\uE7E8", ATTACH = "\uE723", CHECK = "\uE73E",
            WARNING = "\uE7BA", ADD = "\uE710", MINUS = "\uE738", CLOCK = "\uE823", EYE = "\uE890",
            LEFT = "\uE76B", RIGHT = "\uE76C", CLEAR = "\uE711", CART = "\uE7BF", MONEY = "\uE8C7", COURT = "\uE80A", TICKET = "\uE7B8";

    private static final DateTimeFormatter LONG_DATE =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", THAI).withChronology(ThaiBuddhistChronology.INSTANCE);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", THAI).withChronology(ThaiBuddhistChronology.INSTANCE);
    private static final DateTimeFormatter SHORT_DATE =
            DateTimeFormatter.ofPattern("d MMM yy", THAI).withChronology(ThaiBuddhistChronology.INSTANCE);
    private static final DateTimeFormatter MONTH =
            DateTimeFormatter.ofPattern("MMMM yyyy", THAI).withChronology(ThaiBuddhistChronology.INSTANCE);
    private static final DateTimeFormatter MONTH_SHORT = DateTimeFormatter.ofPattern("MMM", THAI);

    private Ui() {
    }

    // ───────────── รูปแบบข้อความ ─────────────

    public static String money(double amount) {
        return "%,.0f ฿".formatted(amount);
    }

    public static String hour(int hour) {
        return "%02d:00".formatted(hour);
    }

    public static String longDate(LocalDate date) {
        return LONG_DATE.format(date);
    }

    public static String date(LocalDate date) {
        return DATE.format(date);
    }

    public static String shortDate(LocalDate date) {
        return SHORT_DATE.format(date);
    }

    public static String month(YearMonth month) {
        return MONTH.format(month.atDay(1));
    }

    public static String monthShort(YearMonth month) {
        return MONTH_SHORT.format(month);
    }

    // ───────────── ชิ้นส่วนพื้นฐาน ─────────────

    public static Label icon(String glyph) {
        Label icon = new Label(glyph);
        icon.getStyleClass().add("icon");
        return icon;
    }

    public static Label label(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    public static Label title(String text) {
        return label(text, "card-title");
    }

    public static Label muted(String text) {
        return label(text, "muted");
    }

    /** ข้อความรองที่ตัดบรรทัดได้ ใช้กับคำอธิบายยาวในการ์ดแคบ */
    public static Label note(String text) {
        Label label = muted(text);
        label.setWrapText(true);
        return label;
    }

    public static Label fieldLabel(String text) {
        return label(text, "field-label");
    }

    public static VBox card(Node... children) {
        VBox card = new VBox(14, children);
        card.getStyleClass().add("card");
        return card;
    }

    public static Button button(String text, String glyph, String style) {
        Button button = new Button(text, glyph == null ? null : icon(glyph));
        button.getStyleClass().addAll("btn", style);
        button.setMinWidth(Region.USE_PREF_SIZE);
        return button;
    }

    public static Button iconButton(String glyph, String tooltip) {
        Button button = new Button(null, icon(glyph));
        button.getStyleClass().addAll("btn", "btn-ghost", "btn-icon");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /** แถว "ชื่อ ........ ค่า" ใช้ในสรุปราคาและโปรไฟล์ */
    public static HBox row(Node left, Node right) {
        HBox row = new HBox(8, left, spacer(), right);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** ช่องค้นหาที่มีไอคอนแว่นขยายอยู่ด้านหน้า */
    public static HBox searchBox(TextField field, String prompt) {
        field.setPromptText(prompt);
        HBox box = new HBox(8, icon(SEARCH), field);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("search-box");
        HBox.setHgrow(field, Priority.ALWAYS);
        return box;
    }

    /** ตัวเลือกวันที่แสดงเป็น พ.ศ. ภาษาไทย (เลือกจากปฏิทินเท่านั้น ไม่ต้องพิมพ์) */
    public static DatePicker datePicker(LocalDate value) {
        DatePicker picker = new DatePicker(value);
        picker.setChronology(ThaiBuddhistChronology.INSTANCE);
        picker.setEditable(false);
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date(date);
            }

            @Override
            public LocalDate fromString(String text) {
                return text == null || text.isBlank() ? null : DATE.parse(text, LocalDate::from);
            }
        });
        return picker;
    }

    /** คอลัมน์ตารางจากฟังก์ชันอ่านค่า เช่น column("รหัส", Booking::id) */
    public static <T> TableColumn<T, Object> column(String title, Function<T, ?> value) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setSortable(false);
        column.setReorderable(false);
        return column;
    }

    /** การ์ดตัวเลขสรุป (KPI) พร้อมไอคอนสี */
    public static HBox kpi(String glyph, String tone, String caption, String value) {
        StackPane tile = new StackPane(icon(glyph));
        tile.getStyleClass().addAll("icon-tile", tone);
        HBox kpi = new HBox(14, tile, new VBox(2, muted(caption), label(value, "kpi-value")));
        kpi.setAlignment(Pos.CENTER_LEFT);
        kpi.getStyleClass().addAll("card", "kpi");
        kpi.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(kpi, Priority.ALWAYS);
        return kpi;
    }

    /** วงกลมตัวอักษรแรกของชื่อ สีคงที่ต่อชื่อ */
    public static StackPane avatar(String name, double size) {
        String[] palette = {"#059669", "#4f46e5", "#d97706", "#db2777", "#0891b2", "#7c3aed"};
        // ข้ามสระหน้า เ แ โ ใ ไ เพื่อให้ได้พยัญชนะตัวแรก
        String initial = name.codePoints().filter(c -> c < 0x0E40 || c > 0x0E44).limit(1)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        Label letter = label(initial, "avatar-letter");
        letter.setStyle("-fx-font-size: " + size * 0.42 + "px;");
        StackPane avatar = new StackPane(letter);
        avatar.getStyleClass().add("avatar");
        avatar.setStyle("-fx-background-color: " + palette[Math.floorMod(name.hashCode(), palette.length)] + ";");
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);
        return avatar;
    }

    /** โลโก้ลูกขนไก่ วาดด้วย SVG path บนพื้นไล่สี */
    public static StackPane logo(double size) {
        SVGPath feathers = new SVGPath();
        feathers.setContent("M5 2 H19 L14.5 14 H9.5 Z");
        feathers.setFill(Color.WHITE);
        SVGPath ribs = new SVGPath();
        ribs.setContent("M9.6 2 L11 14 M14.4 2 L13 14 M6.6 6.5 H17.4 M8.2 10.5 H15.8");
        ribs.setStroke(Color.web("#10b981"));
        ribs.setStrokeWidth(0.9);
        ribs.setFill(null);
        SVGPath cork = new SVGPath();
        cork.setContent("M9.5 14.6 H14.5 A2.5 2.5 0 0 1 9.5 14.6 Z");
        cork.setFill(Color.web("#fbbf24"));
        Group shuttle = new Group(feathers, ribs, cork);
        shuttle.setRotate(-35);
        shuttle.setScaleX(size / 34);
        shuttle.setScaleY(size / 34);

        StackPane logo = new StackPane(new Group(shuttle));
        logo.getStyleClass().add("logo");
        logo.setMinSize(size, size);
        logo.setMaxSize(size, size);
        return logo;
    }

    // ───────────── การแจ้งเตือน ─────────────

    /** แถบข้อความลอยด้านล่างจอ หายไปเอง */
    public static void toast(Node owner, String message, boolean success) {
        if (owner.getScene() == null || !(owner.getScene().getRoot() instanceof StackPane root)) return;
        root.getChildren().removeIf(node -> node.getStyleClass().contains("toast")); // อันใหม่แทนอันเก่า ไม่ซ้อนทับกัน
        Label toast = new Label(message, icon(success ? CHECK : WARNING));
        toast.getStyleClass().addAll("toast", success ? "toast-ok" : "toast-error");
        toast.setMouseTransparent(true);
        toast.setWrapText(true);
        toast.setMaxWidth(640);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 32, 0));
        root.getChildren().add(toast);

        FadeTransition fade = new FadeTransition(Duration.millis(400), toast);
        fade.setDelay(Duration.seconds(success ? 2.5 : 4));
        fade.setToValue(0);
        fade.setOnFinished(e -> root.getChildren().remove(toast));
        fade.play();
    }

    /**
     * รันคำสั่งบันทึกข้อมูล: สำเร็จแสดงข้อความที่คืนมา, ผิดกฎแสดงเหตุผลจาก Club
     * ทุกฟอร์มใช้เมธอดนี้ จึงจัดการข้อผิดพลาดแบบเดียวกันทั้งแอป
     */
    public static boolean submit(Node owner, Supplier<String> action) {
        try {
            toast(owner, action.get(), true);
            return true;
        } catch (IllegalArgumentException | UncheckedIOException e) {
            toast(owner, e.getMessage(), false);
            return false;
        }
    }

    public static boolean confirm(Node owner, String header, String content) {
        ButtonType ok = new ButtonType("ยืนยัน", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ok, new ButtonType("ยกเลิก", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.initOwner(owner.getScene().getWindow());
        alert.setTitle("ยืนยัน");
        alert.setHeaderText(header);
        alert.getDialogPane().getStylesheets().add(STYLESHEET);
        return alert.showAndWait().filter(ok::equals).isPresent();
    }

    public static void showImage(Node owner, Path file, String title) {
        ImageView view = new ImageView(new Image(file.toUri().toString()));
        view.setPreserveRatio(true);
        view.setFitWidth(460);
        view.setFitHeight(680);
        StackPane pane = new StackPane(view);
        pane.getStyleClass().add("image-viewer");

        Stage stage = new Stage();
        stage.initOwner(owner.getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(title);
        stage.setScene(new Scene(pane));
        stage.getScene().getStylesheets().add(STYLESHEET);
        stage.show();
    }
}
