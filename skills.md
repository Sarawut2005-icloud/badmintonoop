# skills.md — วิธีทำงานกับโปรเจกต์นี้ (สำหรับ AI / คนที่มาทำต่อ)

> อ่านไฟล์นี้ก่อนแก้โค้ด แล้วอ่าน `HANDOVER.md` ต่อเพื่อดูสถานะล่าสุด
> **กฎเหล็ก:** จบงานทุกครั้งต้องอัปเดต `HANDOVER.md` → commit → push

## 1. โปรเจกต์นี้คืออะไร
งาน OOP ของมหาวิทยาลัย เป็นระบบจองสนามแบดมินตันแบบ GUI บน Windows ใช้ **Java 21 + JavaFX 21 + Maven** ไม่มีฐานข้อมูล
- โจทย์ต้นฉบับอยู่ใน `สิ่งที่ต้องทำ.png` และ `markdown.txt` ส่วนตารางเทียบโจทย์กับโค้ดอยู่ใน `README.md`
- เจ้าของงานต้องพรีเซ็นต์ และแชร์โค้ดให้เพื่อนผ่าน GitHub (public): https://github.com/Sarawut2005-icloud/badmintonoop

## 2. สถาปัตยกรรม (ห้ามทำลายการแบ่งชั้นนี้)
```
model/    ข้อมูล + กฎธุรกิจ (ห้าม import javafx)
service/  Club = ที่เก็บข้อมูลเดียว ตรวจกฎก่อนบันทึก แล้วบันทึกลง data/club.dat ด้วย Java Serialization
ui/       หน้าจอ JavaFX เขียนด้วยโค้ดล้วน (ไม่ใช้ FXML) สไตล์อยู่ใน resources/.../style.css
```
- **กฎธุรกิจทุกข้อ** (ราคา เวลาเปิด-ปิด วันหยุด ส่วนลด) อยู่ใน `model/ClubRules.java` ห้ามกระจายไปที่อื่น
- **ทุกคำสั่งที่เปลี่ยนข้อมูล** ต้องผ่าน method ของ `Club` และถ้าผิดกฎให้ `throw new IllegalArgumentException("เหตุผลภาษาไทย")`
- **ฝั่ง UI** เรียกผ่าน `Ui.submit(this, () -> { ...; return "ข้อความสำเร็จ"; })` ซึ่งแสดง toast เขียว/แดงให้เอง
- **หน้าใหม่**: extends `Page` → implement `refresh()` → เพิ่มค่าใน `enum Menu` → เพิ่ม case ใน `MainView.createPage/iconOf` → ใส่ใน `menus()` ของบทบาทที่ใช้ได้
- **รายได้ประเภทใหม่**: ให้คลาสนั้น implement `Billable` แล้วรวมเข้า `Club.transactions()` โดยไม่ต้องแก้หน้ารายงาน
- **ถ้าแก้ field ของคลาสใน `model` หรือ `Club`**: ไฟล์ `data/club.dat` เดิมจะอ่านไม่ได้ ระบบจะสำรองไฟล์แล้วสร้างข้อมูลตัวอย่างใหม่ ถือเป็นเรื่องปกติของงานนี้

## 3. สไตล์โค้ด (ผู้ใช้ขอแบบ "humanizer" อ่านง่าย)
- ตั้งชื่อตัวแปรและเมธอดเป็นภาษาอังกฤษที่สื่อความหมาย ส่วน **คอมเมนต์และข้อความบนจอเป็นภาษาไทย**
- คอมเมนต์อธิบายแค่ "ทำไม" 1 บรรทัด และมี Javadoc 1 บรรทัดบนหัวทุกคลาส
- ใช้ `record` กับข้อมูลที่ไม่เปลี่ยนหลังบันทึก, `enum` กับค่าตายตัว, `switch` แบบ arrow
- ไม่เพิ่ม dependency ใหม่ ไม่เพิ่ม abstraction ที่ยังไม่มีใครใช้
- ใช้ CSS ของ JavaFX (ไม่ใช่ CSS เว็บ!) เช่น `-fx-background-color` ส่วน `repeating-linear-gradient` ใช้ไม่ได้ ต้องเขียน `linear-gradient(..., repeat, ...)`
- สีของธีมกำหนดไว้ที่ `.root` ใน style.css (`-primary`, `-ink`, `-muted` ...) ให้ใช้ชื่อเหล่านี้แทนการใส่รหัสสีใหม่
- ไอคอนใช้ฟอนต์ **Segoe MDL2 Assets** (มีเฉพาะ Windows) กำหนดค่าคงที่ไว้ใน `Ui.java` ในรูป `"\uE787"`

## 4. คำสั่งที่ใช้บ่อย (Windows)
```powershell
# เครื่องเจ้าของงานตั้ง JAVA_HOME ผิด (ชี้ไปที่ jdk-24\bin) ต้องตั้งใหม่ก่อนทุกครั้ง
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
.\mvnw.cmd -q compile          # คอมไพล์
.\mvnw.cmd test                # เทสต์ (ClubTest)
.\mvnw.cmd javafx:run          # เปิดแอป
```
- เพื่อนแค่ดับเบิลคลิก `run.bat` (หา JDK ให้เองและข้าม JAVA_HOME ที่ตั้งผิด)
- แผนภาพเป็น PlantUML: แก้ไฟล์ `docs/diagrams/*.puml` แล้วรัน
  `java -jar plantuml.jar -charset UTF-8 -Sdpi=150 docs/diagrams/*.puml`
  (โหลด jar ได้จาก Maven Central: `net.sourceforge.plantuml:plantuml:1.2024.8` · คลาสไดอะแกรมใช้ `!pragma layout smetana` จึงไม่ต้องลง Graphviz)

## 5. กับดักที่เคยเจอ (อย่าเจอซ้ำ)
| กับดัก | วิธีเลี่ยง |
|---|---|
| เครื่องมือ Write ของ AI แปลง `"\uE787"` ในซอร์สเป็นตัวอักษรจริง ทำให้มองไม่เห็นในโค้ด | หลังเขียนไฟล์ ให้แปลงอักขระ U+E000–U+F8FF กลับเป็น `\uXXXX` |
| IntelliJ รันเทสต์แล้วขึ้น `does not "opens" ...service` | `module-info.java` ต้องมี `opens com.example.badmintonbookingsystem.service;` |
| Maven 3.8 ใช้ surefire ตัวเก่าที่ไม่รู้จัก JUnit 5 | pom ล็อก `maven-surefire-plugin` เป็นเวอร์ชัน 3.2.5 แล้ว |
| ไฟล์ `.bat` ที่มีภาษาไทยทำให้ cmd อ่านพัง | ข้อความใน `.bat` ใช้ภาษาอังกฤษเท่านั้น และไฟล์ต้องเป็น CRLF (กำหนดไว้ใน `.gitattributes`) |
| `ScrollPane` / `TableView` มีพื้นหลังเทาจาก modena | ทำให้โปร่งใสไว้แล้วใน style.css |
| ทำ `.list-view/.list-cell` โปร่งใสแล้วดรอปดาวน์ ComboBox โปร่งใสตามไปด้วย | กฎ `.combo-box-popup` ต้องอยู่ **หลัง** กฎ list ทั่วไป และตั้งพื้นขาว ตรวจด้วยการถ่ายภาพ PopupWindow |
| ชื่อไทยขึ้นต้นด้วยสระหน้า (เ แ โ ใ ไ) ทำให้ตัวย่อใน avatar ผิด | `Ui.avatar` ข้ามสระหน้าให้แล้ว |

## 6. วิธีตรวจ UI โดยไม่ต้องเปิดแอปเอง
เขียนคลาสทดลองที่ `extends Application` สร้าง `Club.load(โฟลเดอร์ชั่วคราว)` แล้ว `scene.setRoot(new MainView(...))`
จากนั้น `scene.snapshot(null)` เพื่อบันทึก PNG แล้วเปิดดูภาพ
ภาพหน้าจอล่าสุดเก็บไว้ที่ `docs/screenshots/` (ให้ถ่ายใหม่ทุกครั้งที่แก้ UI)
