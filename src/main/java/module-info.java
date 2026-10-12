module com.example.badmintonbookingsystem {
    requires javafx.controls;

    exports com.example.badmintonbookingsystem;
    // ให้ JUnit สร้างคลาสเทสต์ในแพ็กเกจนี้ได้ (IntelliJ รันเทสต์แบบ module เต็มรูปแบบ)
    opens com.example.badmintonbookingsystem.service;
}
