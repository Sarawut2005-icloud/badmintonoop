"""วางไฟล์เสียงพากย์ (assets/voice/vo1–vo7.wav) ลงใน index.html ตามเวลาเริ่มของแต่ละฉาก

รัน:  python place_voice.py   (หลังสร้างเสียงด้วย Gemini แล้ว)
ตรวจว่าทุกท่อนจบก่อนฉากถัดไปเริ่ม ถ้าไม่พอดีจะหยุดและบอกว่าท่อนไหนต้องตัดข้อความให้สั้นลง
"""
import re, subprocess
from pathlib import Path

HERE = Path(__file__).parent
# (id, เริ่มพูดวินาทีที่, ต้องจบก่อนวินาทีที่) — ตรงกับช่วงฉากใน index.html
SLOTS = [("vo1", 0.5, 7.9), ("vo2", 8.0, 16.2), ("vo3", 17.2, 28.0), ("vo4", 29.0, 38.2),
         ("vo5", 39.2, 49.2), ("vo6", 50.2, 59.2), ("vo7", 60.2, 71.5)]


def duration(path: Path) -> float:
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(path)],
                         capture_output=True, text=True, check=True).stdout
    return round(float(out), 2)


tags = []
for line_id, start, must_end in SLOTS:
    wav = HERE / "assets" / "voice" / f"{line_id}.wav"
    assert wav.exists(), f"ยังไม่มี {wav.name} — สร้างเสียงก่อน"
    length = duration(wav)
    assert start + length <= must_end, f"{line_id} ยาว {length}s เกินฉาก (ต้องจบก่อน {must_end}s) — ตัดข้อความให้สั้นลง"
    tags.append(f'      <audio id="{line_id}" src="assets/voice/{line_id}.wav" data-start="{start}" data-duration="{length}" data-track-index="9"></audio>')
    print(f"{line_id}: {start}s → {start + length:.2f}s (limit {must_end}s)")

page = HERE / "index.html"
html = page.read_text(encoding="utf-8")
html, count = re.subn(r'      <audio id="vo\d".*?</audio>\n', "", html)
assert count == 7, f"คาดว่าจะเจอแท็กเสียงเดิม 7 อัน แต่เจอ {count}"
marker = "      <!-- 1 เปิดตัว -->"
html = html.replace(marker, "\n".join(tags) + "\n\n" + marker, 1)
html = html.replace("<!-- เสียงพากย์ AI (Edge TTS · th-TH-NiwatNeural) สร้างจาก narration.py -->",
                    "<!-- เสียงพากย์ AI (Gemini TTS · เสียง Charon) สร้างจาก audio_request.json แล้ววางด้วย place_voice.py -->")
page.write_text(html, encoding="utf-8", newline="\n")
print("placed", len(tags), "voice clips")
