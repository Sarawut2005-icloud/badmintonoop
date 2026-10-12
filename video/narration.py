"""สร้างเสียงพากย์ไทยด้วย Microsoft Edge TTS (เสียง Niwat) แล้วตรวจว่าแต่ละท่อนพอดีกับฉากของมัน

รัน:  python narration.py   (ต้องมี: python -m pip install edge-tts และ ffprobe)
ถ้าท่อนไหนยาวเกินช่วงเวลาของฉาก สคริปต์จะพูดเร็วขึ้นทีละนิดจนพอดี แล้วพิมพ์ data-start / data-duration ให้นำไปใส่ใน index.html
"""
import asyncio, json, subprocess
from pathlib import Path

import edge_tts

VOICE = "th-TH-NiwatNeural"
OUT = Path(__file__).parent / "assets" / "voice"

# (id, เริ่มพูดวินาทีที่, ต้องจบก่อนวินาทีที่, ข้อความ) — ช่วงเวลาตรงกับฉากใน index.html
LINES = [
    ("vo1", 0.8, 7.2, "แบดมินตันคลับ ระบบจองสนามแบดมินตัน เขียนด้วยจาวา และจาวาเอฟเอ็กซ์ ออกแบบด้วยหลักโอโอพีครับ"),
    ("vo2", 8.0, 16.2, "สนามมีสิบสองสนาม ชั่วโมงละร้อยแปดสิบบาท มีแพกเกจเหมาจ่าย และชั่วโมงที่ยี่สิบ จ่ายเพียงสามสิบเปอร์เซ็นต์"),
    ("vo3", 17.2, 28.0, "แคชเชียร์เลือกสนามจากตารางได้ทันที ระบบรู้เองว่าวันนี้เป็นวันหยุดหรือไม่ และคิดราคาให้ก่อนกดยืนยัน"),
    ("vo4", 29.2, 38.2, "เช่น สมาชิกที่สะสมไว้สิบแปดชั่วโมง จองเพิ่มสามชั่วโมง ชั่วโมงที่ยี่สิบลดเจ็ดสิบเปอร์เซ็นต์ ยอดรวมสี่ร้อยสิบสี่บาท"),
    ("vo5", 39.2, 49.2, "นอกจากจองสนาม ยังเช่าและขายอุปกรณ์ ค้นหาการจอง และดูแลสมาชิก ได้ครบในที่เดียว"),
    ("vo6", 50.2, 59.0, "ฝั่งผู้ดูแลระบบ ดูรายได้รายวันและรายเดือน แยกตามประเภท พร้อมประวัติการจองของสมาชิกทุกคน"),
    ("vo7", 60.2, 71.2, "ทุกส่วนใช้โอโอพีอย่างมีเหตุผล ทั้งอินเทอร์เฟซ และแอบสแตร็กต์คลาส ตรงตามโจทย์ครบทุกข้อครับ"),
]


def duration(path: Path) -> float:
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(path)],
                         capture_output=True, text=True, check=True).stdout
    return float(out.strip())


async def synthesize(text: str, rate: int, path: Path) -> float:
    await edge_tts.Communicate(text, VOICE, rate=f"{rate:+d}%").save(str(path))
    return duration(path)


async def main():
    OUT.mkdir(parents=True, exist_ok=True)
    timing = []
    for line_id, start, must_end, text in LINES:
        path, rate = OUT / f"{line_id}.mp3", 0
        length = await synthesize(text, rate, path)
        while start + length > must_end and rate < 30:  # ยาวเกินฉาก → พูดเร็วขึ้นทีละ 5%
            rate += 5
            length = await synthesize(text, rate, path)
        assert start + length <= must_end, f"{line_id} ยาวเกินฉากแม้พูดเร็วสุดแล้ว — ตัดข้อความให้สั้นลง"
        timing.append({"id": line_id, "start": start, "duration": round(length, 2), "rate": rate})
        print(f"{line_id}: start {start}s · {length:.2f}s · rate {rate:+d}% · ends {start + length:.2f}s (limit {must_end}s)")
    (OUT / "timing.json").write_text(json.dumps(timing, ensure_ascii=False, indent=2), encoding="utf-8")


asyncio.run(main())
