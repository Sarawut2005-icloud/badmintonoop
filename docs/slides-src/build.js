// สร้างสไลด์นำเสนอ Badminton Club (16:9) — node build.js
const pptxgen = require("pptxgenjs");
const path = require("path");
const { applyTheme } = require("C:/Users/User/.claude/skills/synced/72448460-7ad7-4488-8dc8-6b724c21f045_829fa16f-a894-4c61-979e-8e3233ac0079/pptx/scripts/apply_theme.js");

const ROOT = "C:/BadmintonBookingSystem/BadmintonBookingSystem";
const OUT = path.join(ROOT, "docs", "Badminton-Club-Presentation.pptx");
const shot = (n) => `${ROOT}/docs/screenshots/${n}.png`;
const diagram = (n) => `${ROOT}/docs/diagrams/${n}.png`;

const FONT = "Leelawadee UI"; // มีทั้งอักษรไทยและอังกฤษ มากับ Windows ทุกเครื่อง
const CODE_FONT = "Consolas";
const THEME = {
  name: "Badminton Club",
  headFontFace: FONT,
  bodyFontFace: FONT,
  colors: {
    dk1: "0F172A", lt1: "FFFFFF", dk2: "064E3B", lt2: "F1F5F3",
    accent1: "059669", accent2: "4F46E5", accent3: "D97706", accent4: "DB2777",
    accent5: "0891B2", accent6: "7C3AED", hlink: "059669", folHlink: "4F46E5",
  },
};
const HEX = { dark: "052E25", ink: "0F172A", muted: "64748B", soft: "D1FAE5", mint: "A7F3D0", line: "E2E8F0", code: "0F172A" };

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9"; // 10 × 5.625 นิ้ว
pres.theme = { headFontFace: FONT, bodyFontFace: FONT };
pres.title = "Badminton Club — ระบบจองสนามแบดมินตัน";
const C = pres.SchemeColor;

// ───── layouts ─────
pres.defineSlideMaster({
  title: "DARK",
  background: { color: HEX.dark },
  objects: [
    { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 1.55, w: 5.3, h: 1.3, fontSize: 40, bold: true, color: "FFFFFF", fontFace: FONT, valign: "bottom", align: "left", margin: 0 }, text: "" } },
    { placeholder: { options: { name: "body", type: "body", x: 0.6, y: 2.95, w: 5.3, h: 1.4, fontSize: 16, color: HEX.mint, fontFace: FONT, valign: "top", margin: 0 }, text: "" } },
  ],
});
pres.defineSlideMaster({
  title: "CONTENT",
  background: { color: "FFFFFF" },
  objects: [
    { placeholder: { options: { name: "title", type: "title", x: 0.5, y: 0.3, w: 9, h: 0.7, fontSize: 28, bold: true, color: THEME.colors.dk2, fontFace: FONT, valign: "middle", align: "left", margin: 0 }, text: "" } },
    { text: { text: "Badminton Club · OOP Project", options: { x: 0.5, y: 5.2, w: 5, h: 0.3, fontSize: 10, color: HEX.muted, fontFace: FONT, margin: 0 } } },
  ],
  slideNumber: { x: 9.0, y: 5.2, w: 0.5, h: 0.3, fontSize: 10, color: HEX.muted, fontFace: FONT, align: "right" },
});

// ───── ชิ้นส่วนที่ใช้ซ้ำ (สร้าง options ใหม่ทุกครั้ง เพราะ pptxgenjs แก้ค่า options ในที่) ─────
const shadow = () => ({ type: "outer", color: "0F172A", opacity: 0.12, blur: 8, offset: 2, angle: 90 });
function card(slide, x, y, w, h, fill = C.background1, name = "card") {
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w, h, rectRadius: 0.12, fill: { color: fill }, line: { color: HEX.line, width: 0.75 }, shadow: shadow(), objectName: name });
}
function text(slide, value, opts) {
  slide.addText(value, { isTextBox: true, fontFace: FONT, margin: 0, color: C.text1, ...opts });
}
function code(slide, lines, x, y, w, h) {
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w, h, rectRadius: 0.1, fill: { color: HEX.code }, line: { color: HEX.code }, objectName: "code-bg" });
  slide.addText(lines, { isTextBox: true, x: x + 0.25, y: y + 0.18, w: w - 0.5, h: h - 0.36, fontFace: CODE_FONT, fontSize: 12, color: "E2E8F0", valign: "top", margin: 0, objectName: "code" });
}
function badge(slide, label, x, y, color) { // วงกลมตัวเลข/ตัวอักษรนำหน้าหัวข้อ — motif ของทั้งเด็ค
  slide.addShape(pres.shapes.OVAL, { x, y, w: 0.42, h: 0.42, fill: { color }, line: { color }, objectName: "badge" });
  text(slide, label, { x, y, w: 0.42, h: 0.42, fontSize: 14, bold: true, color: C.background1, align: "center", valign: "middle" });
}
function picture(slide, file, x, y, w, ratio) {
  slide.addImage({ path: file, x, y, w, h: w / ratio, rounding: false, shadow: shadow(), objectName: path.basename(file) });
}
const SHOT_RATIO = 1320 / 840;

// ═════ 1. หน้าปก ═════
pres.addSection({ title: "เปิดตัว" });
let s = pres.addSlide({ masterName: "DARK", sectionTitle: "เปิดตัว" });
s.addText("Badminton Club", { placeholder: "title" });
s.addText("ระบบจองสนามแบดมินตัน\nJava 21 · JavaFX · OOP", { placeholder: "body" });
picture(s, shot("01-login"), 6.1, 1.45, 3.5, SHOT_RATIO);
s.addNotes("แนะนำตัว: ระบบจองสนามแบดมินตันที่เขียนด้วย Java และ JavaFX เป็นโปรแกรม GUI บน Windows ใช้ OOP ครบทั้ง interface, abstract class, inheritance, polymorphism และ encapsulation");

// ═════ 2. โจทย์ ═════
pres.addSection({ title: "โจทย์" });
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "โจทย์" });
s.addText("โจทย์ในตัวเลข", { placeholder: "title" });
const facts = [
  ["12", "สนาม", "ราคาชั่วโมงละ 180 บาท"],
  ["13–21", "วันธรรมดา", "ส.–อา. และวันหยุด 10–20 น."],
  ["15 ชม.", "แพกเกจเหมาจ่าย", "ราคา 2,600 บาท"],
  ["30%", "ชั่วโมงที่ 20", "จองครบ 19 ชม. ชม.ที่ 20 จ่าย 30%"],
  ["50 ฿", "เช่าอุปกรณ์", "รองเท้า / ไม้ ต่อครั้งต่อวัน"],
  ["2", "ผู้ใช้ระบบ", "แคชเชียร์ และ ผู้ดูแลระบบ"],
];
facts.forEach(([big, label, note], i) => {
  const x = 0.5 + (i % 3) * 3.05, y = 1.25 + Math.floor(i / 3) * 1.9;
  card(s, x, y, 2.85, 1.7);
  text(s, big, { x: x + 0.25, y: y + 0.2, w: 2.4, h: 0.65, fontSize: 30, bold: true, color: C.accent1 });
  text(s, label, { x: x + 0.25, y: y + 0.88, w: 2.4, h: 0.32, fontSize: 15, bold: true });
  text(s, note, { x: x + 0.25, y: y + 1.2, w: 2.4, h: 0.32, fontSize: 11, color: HEX.muted });
});
s.addNotes("อ่านโจทย์ทีละข้อจากการ์ด: 12 สนาม 180 บาท, เวลาเปิด, แพกเกจ, ส่วนลดชั่วโมงที่ 20, เช่าอุปกรณ์ และผู้ใช้ระบบ 2 บทบาท");

// ═════ 3. ใครคือใคร ═════
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "โจทย์" });
s.addText("โจทย์แยกคน 3 กลุ่มชัดเจน", { placeholder: "title" });
const people = [
  ["ผู้ใช้ระบบ", "แคชเชียร์ (Jack) · ผู้ดูแลระบบ (นุ)", "ล็อกอินเข้าโปรแกรม\nเป็นคลาส User → Cashier, Admin", C.accent1],
  ["สมาชิก", "ลูกค้าที่แคชเชียร์สมัครให้", "จองสนามและซื้อแพกเกจได้\nเป็นข้อมูล Member (ไม่ล็อกอิน)", C.accent2],
  ["ผู้เล่น", "ใครก็ได้ที่มาเล่น", "เช่า/ซื้ออุปกรณ์ได้\nไม่ต้องเป็นสมาชิก", C.accent3],
];
people.forEach(([title, who, detail, color], i) => {
  const x = 0.5 + i * 3.05;
  card(s, x, 1.25, 2.85, 3.0);
  badge(s, String(i + 1), x + 0.25, 1.5, color);
  text(s, title, { x: x + 0.8, y: 1.5, w: 1.9, h: 0.42, fontSize: 18, bold: true, valign: "middle" });
  text(s, who, { x: x + 0.25, y: 2.15, w: 2.4, h: 0.6, fontSize: 13, bold: true, color: C.text2 });
  text(s, detail, { x: x + 0.25, y: 2.85, w: 2.4, h: 1.2, fontSize: 13, color: HEX.muted, valign: "top" });
});
text(s, "บรีฟ: \"พนักงานแคชเชียร์เป็นผู้กรอกข้อมูลการจองให้\" — ลูกค้าจึงไม่ต้องมี role ของตัวเอง", { x: 0.5, y: 4.5, w: 9, h: 0.4, fontSize: 13, italic: true, color: C.text2 });
s.addNotes("ตอบคำถามว่าทำไมไม่มี role ลูกค้า: บรีฟระบุผู้ใช้ระบบคือแคชเชียร์กับผู้ดูแลระบบ และแคชเชียร์กรอกการจองให้ สมาชิกเป็นข้อมูล ผู้เล่นเช่า/ซื้อได้โดยไม่ต้องเป็นสมาชิก");

// ═════ 4. โครงสร้าง 3 ชั้น ═════
pres.addSection({ title: "ออกแบบ OOP" });
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "ออกแบบ OOP" });
s.addText("แบ่งโค้ดเป็น 3 ชั้น", { placeholder: "title" });
const layers = [
  ["ui", "หน้าจอ JavaFX", "LoginView · MainView · Page ทุกหน้า · style.css", C.accent2],
  ["service", "Club — ศูนย์กลางข้อมูล", "ตรวจกฎก่อนบันทึกทุกครั้ง · บันทึกไฟล์ · ย้อนข้อมูลถ้าบันทึกพลาด", C.accent1],
  ["model", "ข้อมูล + กฎธุรกิจ", "Member · Booking · Quote · ClubRules (กฎทุกข้อในไฟล์เดียว)", C.text2],
];
layers.forEach(([pkg, title, detail, color], i) => {
  const y = 1.2 + i * 1.25;
  card(s, 0.5, y, 9, 1.05);
  s.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: 0.7, y: y + 0.2, w: 1.5, h: 0.65, rectRadius: 0.1, fill: { color }, line: { color }, objectName: "layer-chip" });
  text(s, pkg, { x: 0.7, y: y + 0.2, w: 1.5, h: 0.65, fontSize: 18, bold: true, color: C.background1, align: "center", valign: "middle" });
  text(s, title, { x: 2.45, y: y + 0.15, w: 6.8, h: 0.4, fontSize: 17, bold: true });
  text(s, detail, { x: 2.45, y: y + 0.55, w: 6.8, h: 0.35, fontSize: 13, color: HEX.muted });
});
s.addNotes("model ไม่มีโค้ดหน้าจอเลย service คือ Club ที่ทุกคำสั่งต้องผ่าน ui คือหน้าจอ กฎทุกข้ออยู่ใน ClubRules แก้ราคาที่เดียว");

// ═════ 5. Class diagram ═════
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "ออกแบบ OOP" });
s.addText("Class Diagram", { placeholder: "title" });
s.addImage({ path: diagram("class-diagram"), x: 0.5, y: 1.1, w: 4.3, h: 4.3 * 1985 / 2136, objectName: "class-diagram" });
const keys = [
  ["I", "interface Billable", "การจอง แพกเกจ และร้านค้า ใช้รวมรายได้", C.accent2],
  ["A", "abstract User", "Cashier และ Admin บอกเมนูของตัวเอง", C.accent1],
  ["A", "abstract Page", "ทุกหน้าจอมี refresh() ร่วมกัน", C.accent1],
  ["R", "record", "Booking · Quote ข้อมูลที่ไม่เปลี่ยนหลังบันทึก", C.accent3],
];
keys.forEach(([mark, title, detail, color], i) => {
  const y = 1.25 + i * 0.95;
  badge(s, mark, 5.2, y, color);
  text(s, title, { x: 5.8, y: y - 0.02, w: 3.7, h: 0.35, fontSize: 15, bold: true });
  text(s, detail, { x: 5.8, y: y + 0.33, w: 3.7, h: 0.45, fontSize: 12, color: HEX.muted, valign: "top" });
});
s.addNotes("ชี้ให้ดู interface Billable, abstract User และ abstract Page รวมถึง record ที่ใช้กับข้อมูลที่ไม่เปลี่ยน");

// ═════ 6. Interface ═════
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "ออกแบบ OOP" });
s.addText("Interface: รวมรายได้ด้วยลูปเดียว", { placeholder: "title" });
code(s, "public interface Billable {\n    LocalDate paidOn();\n    RevenueType type();\n    double amount();\n}\n\n// Club.revenue()\nfor (Billable b : transactions())\n    sum.merge(b.type(), b.amount(), ...);", 0.5, 1.2, 5.0, 3.7);
const billables = [["Booking", "ค่าสนาม"], ["PackagePurchase", "แพกเกจ"], ["ShopSale", "เช่า / ขาย"]];
billables.forEach(([cls, label], i) => {
  const y = 1.2 + i * 0.85;
  card(s, 5.9, y, 3.6, 0.7, C.background2);
  text(s, cls, { x: 6.1, y, w: 2.1, h: 0.7, fontSize: 14, bold: true, valign: "middle", fontFace: CODE_FONT });
  text(s, label, { x: 8.1, y, w: 1.25, h: 0.7, fontSize: 13, color: C.accent1, bold: true, align: "right", valign: "middle" });
});
text(s, "เพิ่มรายได้ประเภทใหม่ = implement Billable\nหน้ารายงานไม่ต้องแก้ (polymorphism)", { x: 5.9, y: 3.9, w: 3.6, h: 0.9, fontSize: 13, color: C.text2, bold: true, valign: "top" });
s.addNotes("อะไรก็ตามที่ทำให้มีรายได้จะ implement Billable รายงานวนลูปเดียวเรียก amount() โดยไม่ต้องรู้ว่าเป็นคลาสไหน เพิ่มประเภทใหม่ก็ไม่ต้องแก้รายงาน");

// ═════ 7. Abstract ═════
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "ออกแบบ OOP" });
s.addText("Abstract class: ไม่มี if-else เช็กสิทธิ์", { placeholder: "title" });
code(s, "public abstract class User {\n    public abstract List<Menu> menus();\n}\n\nclass Cashier extends User {\n    menus() → จอง, ค้นหา, สมาชิก, ร้าน\n}\nclass Admin extends User {\n    menus() → รายได้, ประวัติสมาชิก\n}", 0.5, 1.2, 5.0, 3.7);
picture(s, shot("06-revenue"), 5.9, 1.2, 3.6, SHOT_RATIO);
text(s, "หน้าหลักสร้างเมนูจาก user.menus()\nสลับหน้าแล้วเรียก page.refresh() ได้ทุกหน้า", { x: 5.9, y: 3.65, w: 3.6, h: 0.9, fontSize: 13, color: C.text2, bold: true, valign: "top" });
s.addNotes("แต่ละบทบาทบอกเองว่าใช้เมนูอะไร หน้าหลักจึงไม่มี if-else ตรวจสิทธิ์ ส่วน abstract Page บังคับให้ทุกหน้ามี refresh()");

// ═════ 8. คิดราคา ═════
pres.addSection({ title: "หัวใจของระบบ" });
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "หัวใจของระบบ" });
s.addText("คิดราคาอัตโนมัติตามกฎ", { placeholder: "title" });
text(s, "ตัวอย่าง: สมาชิกสะสมไว้ 18 ชม. จองเพิ่ม 3 ชม.", { x: 0.5, y: 1.2, w: 9, h: 0.4, fontSize: 15, color: HEX.muted });
const hours = [["ชม.ที่ 19", "180 ฿", "ราคาปกติ", C.background2, C.text1], ["ชม.ที่ 20", "54 ฿", "ลด 70%", C.accent1, C.background1], ["ชม.ที่ 21", "180 ฿", "ราคาปกติ", C.background2, C.text1]];
hours.forEach(([label, price, note, fill, ink], i) => {
  const x = 0.5 + i * 2.15;
  card(s, x, 1.8, 1.95, 1.9, fill);
  text(s, label, { x: x + 0.2, y: 1.95, w: 1.55, h: 0.35, fontSize: 13, bold: true, color: ink });
  text(s, price, { x: x + 0.2, y: 2.35, w: 1.55, h: 0.7, fontSize: 30, bold: true, color: ink });
  text(s, note, { x: x + 0.2, y: 3.1, w: 1.55, h: 0.35, fontSize: 12, color: ink });
});
card(s, 7.05, 1.8, 2.45, 1.9, C.text2);
text(s, "ยอดชำระ", { x: 7.25, y: 1.95, w: 2.05, h: 0.35, fontSize: 13, bold: true, color: HEX.mint });
text(s, "414 ฿", { x: 7.25, y: 2.35, w: 2.05, h: 0.7, fontSize: 34, bold: true, color: C.background1 });
text(s, "ClubRules.quote()", { x: 7.25, y: 3.1, w: 2.05, h: 0.35, fontSize: 11, color: HEX.mint, fontFace: CODE_FONT });
text(s, "ใช้ชั่วโมงแพกเกจก่อน → ที่เหลือจ่ายเงินสด → ชั่วโมงเงินสดที่ 20, 40, 60, … คิด 30%\nถ้าจ่ายด้วยแพกเกจทั้งหมด ยอดเป็น 0 บาท จึงไม่ต้องแนบสลิป", { x: 0.5, y: 4.0, w: 9, h: 0.9, fontSize: 14, valign: "top" });
s.addNotes("ตัวเลขนี้ตรงกับเทสต์ใน ClubTest: 180 + 54 + 180 = 414 บาท แพกเกจถูกใช้ก่อน ชั่วโมงแพกเกจไม่นับสะสมส่วนลด");

// ═════ 9. หน้าจอจอง ═════
pres.addSection({ title: "สาธิต" });
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "สาธิต" });
s.addText("หน้าจองสนาม", { placeholder: "title" });
picture(s, shot("02-booking"), 0.5, 1.15, 6.0, SHOT_RATIO);
const callouts = [["1", "ตาราง 12 สนาม", "คลิกช่องว่าง คลิกต่อเพื่อขยายเวลา"], ["2", "รู้วันหยุดเอง", "เวลาเปิด-ปิดเปลี่ยนตามวัน"], ["3", "ราคาขึ้นทันที", "บอกว่าต้องแนบสลิปไหม"]];
callouts.forEach(([n, title, detail], i) => {
  const y = 1.2 + i * 1.25;
  badge(s, n, 6.85, y, C.accent1);
  text(s, title, { x: 7.4, y: y - 0.02, w: 2.1, h: 0.35, fontSize: 15, bold: true });
  text(s, detail, { x: 7.4, y: y + 0.35, w: 2.1, h: 0.6, fontSize: 12, color: HEX.muted, valign: "top" });
});
s.addNotes("สาธิต: เลือกช่อง ขยายเวลา พิมพ์เบอร์ค้นหาสมาชิก ดูราคา แนบสลิป ยืนยัน แล้วลองจองด้วยแพกเกจให้เห็นว่ายอดเป็น 0 บาท");

// ═════ 10. หน้าจออื่น ═════
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "สาธิต" });
s.addText("ครบทุกหน้าที่โจทย์ต้องการ", { placeholder: "title" });
const grid = [["05-shop", "เช่า / ซื้ออุปกรณ์"], ["03-search", "ค้นหาการจอง"], ["07-revenue-monthly", "รายได้รายวัน / รายเดือน"], ["08-history", "ประวัติสมาชิก"]];
grid.forEach(([file, label], i) => {
  const x = 0.5 + (i % 2) * 4.6, y = 1.1 + Math.floor(i / 2) * 2.05;
  picture(s, shot(file), x, y, 2.75, SHOT_RATIO);
  text(s, label, { x: x + 2.9, y: y + 0.55, w: 1.55, h: 0.7, fontSize: 14, bold: true, valign: "middle" });
});
s.addNotes("ร้านค้ามีภาพสินค้าจริงและตะกร้า, ค้นหาด้วยชื่อ/เบอร์/รหัส, แอดมินดูรายได้แยกประเภทและประวัติของสมาชิกแต่ละคน");

// ═════ 11. คุณภาพ ═════
pres.addSection({ title: "ปิดท้าย" });
s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "ปิดท้าย" });
s.addText("เชื่อถือได้ ไม่ใช่แค่สวย", { placeholder: "title" });
card(s, 0.5, 1.2, 2.6, 3.6, C.text2);
text(s, "10", { x: 0.7, y: 1.5, w: 2.2, h: 1.1, fontSize: 60, bold: true, color: C.background1 });
text(s, "เทสต์อัตโนมัติ\nผ่านทั้งหมด", { x: 0.7, y: 2.7, w: 2.2, h: 0.9, fontSize: 16, bold: true, color: HEX.mint, valign: "top" });
const checks = [
  "ส่วนลดชั่วโมงที่ 20 และการหักแพกเกจ",
  "กันจองซ้อน · กันจองนอกเวลาหรือเวลาที่ผ่านไปแล้ว",
  "วันหยุดนักขัตฤกษ์ รวมวันหยุดตามจันทรคติ",
  "บันทึกแล้วเปิดใหม่ ข้อมูลยังครบ",
  "ไฟล์เสียไม่ทำให้ข้อมูลหาย (เก็บสำรองให้)",
];
s.addText(checks.map((c, i) => ({ text: c, options: { bullet: true, breakLine: i < checks.length - 1 } })),
  { isTextBox: true, x: 3.45, y: 1.3, w: 6.05, h: 3.4, fontSize: 15, fontFace: FONT, color: C.text1, paraSpaceAfter: 10, valign: "top", margin: 0 });
s.addNotes("รัน ClubTest ใน IntelliJ ให้เห็นว่าผ่านทั้ง 10 ข้อ");

// ═════ 12. ปิด ═════
s = pres.addSlide({ masterName: "DARK", sectionTitle: "ปิดท้าย" });
s.addText("ตรงโจทย์ 13/13 ข้อ", { placeholder: "title" });
s.addText("ใช้ OOP อย่างมีเหตุผล · โค้ดอ่านง่าย แก้ง่าย\nขอบคุณครับ — มีคำถามไหมครับ", { placeholder: "body" });
picture(s, diagram("use-case"), 6.1, 1.35, 3.5, 1315 / 1088);
s.addNotes("สรุป: ครบทุกข้อตามโจทย์ ใช้ OOP เพราะจำเป็นจริง ไม่ได้ใส่ไว้แค่ให้มี เปิดรับคำถาม (ดูคำตอบเตรียมไว้ใน docs/สคริปต์นำเสนอ.md)");

(async () => {
  await pres.writeFile({ fileName: OUT });
  await applyTheme(OUT, THEME);
  console.log("wrote", OUT);
})();
