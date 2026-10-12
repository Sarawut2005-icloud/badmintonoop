package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.Product;
import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/** หน้าเช่า/ขายอุปกรณ์: เลือกสินค้าลงตะกร้า แล้วชำระเงินครั้งเดียว */
public class ShopPage extends Page {

    private final Map<Product, Integer> cart = new EnumMap<>(Product.class);
    private final VBox cartLines = new VBox(12);
    private final Label total = Ui.label("", "total-value");
    private final Button checkout = Ui.button("ชำระเงิน", Ui.CHECK, "btn-primary");

    public ShopPage(Club club, User user) {
        super(club, user);

        FlowPane rentals = new FlowPane(16, 16);
        FlowPane sales = new FlowPane(16, 16);
        for (Product product : Product.values()) {
            (product.isRental() ? rentals : sales).getChildren().add(productCard(product));
        }
        VBox catalog = new VBox(14,
                Ui.title("เช่าอุปกรณ์"), Ui.muted("คิดราคาต่อชิ้น ต่อครั้ง ต่อวัน"), rentals,
                Ui.title("สินค้า"), sales);
        catalog.getStyleClass().add("catalog");
        ScrollPane scroll = new ScrollPane(catalog);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        checkout.setMaxWidth(Double.MAX_VALUE);
        checkout.setOnAction(e -> checkout());
        Label cartTitle = new Label("ตะกร้า", Ui.icon(Ui.CART));
        cartTitle.getStyleClass().add("card-title");
        VBox cartCard = Ui.card(cartTitle, cartLines, Ui.spacer(), new Separator(), Ui.row(Ui.label("รวมทั้งหมด", "total-label"), total), checkout);
        cartCard.setPrefWidth(320);
        cartCard.setMinWidth(300);

        HBox body = new HBox(20, scroll, cartCard);
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().add(body);
    }

    @Override
    public void refresh() {
        renderCart();
    }

    private VBox productCard(Product product) {
        Label tag = Ui.label(product.isRental() ? "เช่า" : "ขาย", "tag", product.isRental() ? "tag-amber" : "tag-green");
        Label price = Ui.label(Ui.money(product.price()), "product-price");
        Label unit = Ui.muted(product.isRental() ? "/ ชิ้น / วัน" : "/ ชิ้น");
        HBox priceRow = new HBox(6, price, unit);
        priceRow.setAlignment(Pos.BASELINE_LEFT);

        Button add = Ui.button("เพิ่มลงตะกร้า", Ui.ADD, "btn-ghost");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(e -> {
            cart.merge(product, 1, Integer::sum);
            renderCart();
        });

        StackPane cover = new StackPane(photo(product, 172, 129, 12), tag);
        StackPane.setAlignment(tag, Pos.TOP_LEFT);
        StackPane.setMargin(tag, new Insets(8));

        VBox card = Ui.card(cover, Ui.label(product.label(), "product-name"), priceRow, Ui.spacer(), add);
        card.getStyleClass().add("product-card");
        return card;
    }

    private void renderCart() {
        cartLines.getChildren().clear();
        if (cart.isEmpty()) cartLines.getChildren().add(Ui.muted("ยังไม่มีสินค้าในตะกร้า"));
        cart.forEach((product, qty) -> {
            Button minus = Ui.iconButton(Ui.MINUS, "ลดจำนวน");
            minus.setOnAction(e -> {
                cart.computeIfPresent(product, (p, q) -> q > 1 ? q - 1 : null);
                renderCart();
            });
            Button plus = Ui.iconButton(Ui.ADD, "เพิ่มจำนวน");
            plus.setOnAction(e -> {
                cart.merge(product, 1, Integer::sum);
                renderCart();
            });
            Label name = Ui.label(product.toString(), "cart-name");
            name.setWrapText(true); // ชื่อยาวขึ้นบรรทัดใหม่แทนการตัดเป็น ...
            VBox info = new VBox(2, name, Ui.muted(Ui.money(product.price() * qty)));
            HBox.setHgrow(info, Priority.ALWAYS);
            HBox line = new HBox(10, photo(product, 44, 33, 6), info, minus, Ui.label(String.valueOf(qty), "cart-qty"), plus);
            line.setAlignment(Pos.CENTER_LEFT);
            cartLines.getChildren().add(line);
        });
        double sum = cart.entrySet().stream().mapToDouble(e -> e.getKey().price() * e.getValue()).sum();
        total.setText(Ui.money(sum));
        checkout.setDisable(cart.isEmpty());
    }

    /** ภาพสินค้าจาก resources/.../images/ชื่อ-enum.jpg แบบมุมโค้ง · ถ้าไม่มีไฟล์จะได้กรอบเทาแทน ไม่ทำให้หน้าพัง */
    private static Node photo(Product product, double width, double height, double radius) {
        URL url = ShopPage.class.getResource("/com/example/badmintonbookingsystem/images/" + product.name().toLowerCase() + ".jpg");
        if (url == null) {
            Region placeholder = new Region();
            placeholder.setPrefSize(width, height);
            placeholder.getStyleClass().add("photo-missing");
            return placeholder;
        }
        ImageView view = new ImageView(new Image(url.toExternalForm(), width * 2, height * 2, false, true)); // 2 เท่าให้คมบนจอ HiDPI
        view.setFitWidth(width);
        view.setFitHeight(height);
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        view.setClip(clip);
        return view;
    }

    private void checkout() {
        double sum = cart.entrySet().stream().mapToDouble(e -> e.getKey().price() * e.getValue()).sum();
        if (!Ui.confirm(this, "ยืนยันการชำระเงิน?", "รับเงิน " + Ui.money(sum) + " สำหรับ " + cart.size() + " รายการ")) return;
        Ui.submit(this, () -> {
            double paid = club.sell(cart, user);
            cart.clear();
            renderCart();
            return "ชำระเงินเรียบร้อย · " + Ui.money(paid);
        });
    }
}
