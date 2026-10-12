package com.example.badmintonbookingsystem.ui;

import com.example.badmintonbookingsystem.model.User;
import com.example.badmintonbookingsystem.service.Club;
import javafx.scene.layout.VBox;

/** หน้าจอหนึ่งหน้าของแอป (abstract) — หน้าหลักเรียก {@link #refresh()} ทุกครั้งที่เปิดหน้านี้ */
public abstract class Page extends VBox {

    protected final Club club;
    protected final User user;

    protected Page(Club club, User user) {
        super(20);
        this.club = club;
        this.user = user;
        getStyleClass().add("page");
    }

    /** โหลดข้อมูลใหม่ เพื่อให้ตรงกับสิ่งที่หน้าอื่นเพิ่งบันทึก */
    public abstract void refresh();
}
