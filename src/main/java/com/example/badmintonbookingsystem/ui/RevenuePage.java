package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.Billable;
import com.example.badmintonbookingsystem.model.Booking;
import com.example.badmintonbookingsystem.model.RevenueType;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** หน้าสรุปรายได้ (แอดมิน): KPI ด้านบน + กราฟแท่งซ้อนตามประเภท + กราฟวงกลม + ตารางสรุปรายวัน/รายเดือน */
public class RevenuePage extends Page {

    /** หนึ่งแถวของรายงาน: ช่วงเวลา (วันหรือเดือน) กับรายได้แยกตามประเภท */
    private record Row(String axisLabel, String fullLabel, Map<RevenueType, Double> byType) {
        double total() {
            return sum(byType);
        }
    }

    // สีชิ้นวงกลมตามประเภท ให้ตรงกับ .default-colorN ของกราฟแท่งใน style.css (ข้ามชิ้นที่เป็น 0 แล้วสีจะไม่เลื่อน)
    private static final String[] TYPE_COLORS = {"#059669", "#6366f1", "#f59e0b", "#ec4899"};

    private final HBox kpis = new HBox(16);
    private final ToggleButton daily = new ToggleButton("รายวัน");
    private final ToggleButton monthly = new ToggleButton("รายเดือน");
    private final ComboBox<YearMonth> monthBox = new ComboBox<>();
    private final ComboBox<Integer> yearBox = new ComboBox<>();
    private final Label periodTotal = Ui.label("", "chip", "chip-green");
    private final CategoryAxis xAxis = new CategoryAxis();
    private final StackedBarChart<String, Number> bars = new StackedBarChart<>(xAxis, new NumberAxis());
    private final PieChart pie = new PieChart();
    private final TableView<Row> table = new TableView<>();

    public RevenuePage(Club club, User user) {
        super(club, user);

        ToggleGroup mode = new ToggleGroup();
        daily.setToggleGroup(mode);
        monthly.setToggleGroup(mode);
        daily.setSelected(true);
        mode.selectedToggleProperty().addListener((obs, before, now) -> {
            if (now == null) before.setSelected(true);
            else refresh();
        });
        HBox segment = new HBox(4, daily, monthly);
        segment.getStyleClass().add("segment");

        monthBox.setConverter(converter(Ui::month));
        yearBox.setConverter(converter(year -> "ปี " + (year + 543)));
        monthBox.valueProperty().addListener(obs -> showReport());
        yearBox.valueProperty().addListener(obs -> showReport());
        monthBox.visibleProperty().bind(daily.selectedProperty());
        monthBox.managedProperty().bind(daily.selectedProperty());
        yearBox.visibleProperty().bind(monthly.selectedProperty());
        yearBox.managedProperty().bind(monthly.selectedProperty());

        bars.setAnimated(false);
        bars.setCategoryGap(6);
        bars.setLegendVisible(true);
        pie.setAnimated(false);
        pie.setLegendVisible(false);
        pie.setLabelsVisible(true);
        pie.setPrefWidth(320);

        table.getColumns().add(Ui.column("ช่วงเวลา", Row::fullLabel));
        for (RevenueType type : RevenueType.values()) {
            table.getColumns().add(Ui.column(type.toString(), row -> Ui.money(row.byType().get(type))));
        }
        table.getColumns().add(Ui.column("รวม", row -> Ui.money(row.total())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(Ui.muted("ไม่มีข้อมูล"));

        HBox toolbar = new HBox(10, Ui.title("รายงานรายได้"), Ui.spacer(), periodTotal, monthBox, yearBox, segment);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox charts = new HBox(12, bars, pie);
        HBox.setHgrow(bars, Priority.ALWAYS);
        VBox report = Ui.card(toolbar, charts, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(report, Priority.ALWAYS);

        getChildren().addAll(kpis, report);
    }

    @Override
    public void refresh() {
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        int hoursThisMonth = club.bookings().stream()
                .filter(b -> YearMonth.from(b.date()).equals(thisMonth)).mapToInt(Booking::hours).sum();
        kpis.getChildren().setAll(
                Ui.kpi(Ui.MONEY, "tone-green", "รายได้วันนี้", Ui.money(sum(club.revenue(today, today)))),
                Ui.kpi(Ui.CHART, "tone-indigo", "รายได้เดือนนี้", Ui.money(sum(club.revenue(thisMonth.atDay(1), today)))),
                Ui.kpi(Ui.CLOCK, "tone-amber", "ชั่วโมงสนามที่จองเดือนนี้", hoursThisMonth + " ชม."),
                Ui.kpi(Ui.PEOPLE, "tone-rose", "สมาชิกทั้งหมด", club.members().size() + " คน"));

        // ตัวเลือกช่วงเวลา: ตั้งแต่เดือนแรกที่มีรายได้ จนถึงเดือนปัจจุบัน (ใหม่สุดก่อน)
        YearMonth first = club.transactions().stream().map(Billable::paidOn).map(YearMonth::from)
                .min(YearMonth::compareTo).orElse(thisMonth);
        List<YearMonth> months = new ArrayList<>();
        for (YearMonth m = thisMonth; !m.isBefore(first); m = m.minusMonths(1)) months.add(m);
        List<Integer> years = months.stream().map(YearMonth::getYear).distinct().toList();
        YearMonth month = monthBox.getValue();
        Integer year = yearBox.getValue();
        monthBox.getItems().setAll(months);
        yearBox.getItems().setAll(years);
        monthBox.setValue(month != null && months.contains(month) ? month : thisMonth);
        yearBox.setValue(year != null && years.contains(year) ? year : thisMonth.getYear());
        showReport();
    }

    private void showReport() {
        if (monthBox.getValue() == null || yearBox.getValue() == null) return;
        List<Row> rows = daily.isSelected() ? dailyRows(monthBox.getValue()) : monthlyRows(yearBox.getValue());

        xAxis.setCategories(FXCollections.observableArrayList(rows.stream().map(Row::axisLabel).toList()));
        bars.getData().clear();
        for (RevenueType type : RevenueType.values()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(type.toString());
            rows.forEach(row -> series.getData().add(new XYChart.Data<>(row.axisLabel(), row.byType().get(type))));
            bars.getData().add(series);
        }

        double grandTotal = rows.stream().mapToDouble(Row::total).sum();
        pie.getData().clear();
        for (RevenueType type : RevenueType.values()) {
            double value = rows.stream().mapToDouble(row -> row.byType().get(type)).sum();
            if (value == 0) continue; // ชิ้นที่เป็น 0 ทำให้ป้ายชื่อซ้อนกัน
            PieChart.Data slice = new PieChart.Data(type + " %.0f%%".formatted(value * 100 / grandTotal), value);
            pie.getData().add(slice);
            slice.getNode().setStyle("-fx-pie-color: " + TYPE_COLORS[type.ordinal()] + ";");
        }

        periodTotal.setText("รวม " + Ui.money(grandTotal));
        table.getItems().setAll(rows.reversed());
    }

    /** ทุกวันของเดือน (ถึงวันนี้ถ้าเป็นเดือนปัจจุบัน) */
    private List<Row> dailyRows(YearMonth month) {
        List<Row> rows = new ArrayList<>();
        for (LocalDate day = month.atDay(1); !day.isAfter(month.atEndOfMonth()) && !day.isAfter(LocalDate.now()); day = day.plusDays(1)) {
            rows.add(new Row(String.valueOf(day.getDayOfMonth()), Ui.date(day), club.revenue(day, day)));
        }
        return rows;
    }

    private List<Row> monthlyRows(int year) {
        List<Row> rows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth month = YearMonth.of(year, m);
            if (month.isAfter(YearMonth.now())) break;
            rows.add(new Row(Ui.monthShort(month), Ui.month(month), club.revenue(month.atDay(1), month.atEndOfMonth())));
        }
        return rows;
    }

    private static double sum(Map<RevenueType, Double> byType) {
        return byType.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private static <T> StringConverter<T> converter(Function<T, String> toText) {
        return new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : toText.apply(value);
            }

            @Override
            public T fromString(String text) {
                throw new UnsupportedOperationException("ตัวเลือกนี้เลือกจากรายการเท่านั้น");
            }
        };
    }
}
