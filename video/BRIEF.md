---
workflow: general-video
flow: automation
storyboard: no
message: "Badminton Club ทำครบทุกข้อตามโจทย์ ด้วยหน้าจอสวยและออกแบบ OOP อย่างมีเหตุผล"
destination: เปิดบนโปรเจกเตอร์ตอนพรีเซ็นต์งานในห้องเรียน
aspect: "16:9"
language: th
audience: อาจารย์และเพื่อนร่วมชั้นวิชา OOP
length: 60-90s
---

## Intent
คลิปโชว์แอป 72 วินาที **มีเสียงพากย์ AI ภาษาไทย** (เดิมไม่มีเสียง ผู้ใช้ขอเพิ่มภายหลัง) ใช้ภาพหน้าจอจริงของแอป + ข้อความไทยบนจอ + การเคลื่อนไหว
ใช้เปิดก่อนหรือหลังพรีเซ็นต์ โดยเจ้าของงานพูดสดตามสคริปต์ `docs/สคริปต์นำเสนอ.md`

## Assets
- ภาพหน้าจอจาก `docs/screenshots/` และ class diagram จาก `docs/diagrams/`
- ฟอนต์ Prompt (Google Fonts, OFL) ใน `assets/fonts/` สำหรับอักษรไทย

## Notes
- ธีมสีเดียวกับแอป: เขียวมรกตเข้ม + เขียวอ่อน + เหลืองอำพันสำหรับส่วนลด
- รอบ 2: ผู้ใช้ขอเพิ่มเสียงพากย์ AI → Microsoft Edge TTS เสียงผู้ชาย th-TH-NiwatNeural 7 ท่อน (ฉากละ 1 ท่อน) สร้างด้วย `narration.py` · ไม่มีเพลงประกอบ
