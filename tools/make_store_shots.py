#!/usr/bin/env python3
"""Frame phone screenshots (1080x2412) as 1080x1920 Play Store images.
Usage: make_store_shots.py OUT_DIR name=path ...   (names are the keys of SHOTS)
Crops away the status bar and the ad banner."""
import sys, os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FONTS = os.path.join(ROOT, "app/src/main/res/font")
W, H = 1080, 1920
CREAM, SOFT = (255, 244, 222), (232, 224, 244)
# name: headline, subline, gradient top, gradient bottom, crop bottom
SHOTS = {
    "play":     ("Slide. Solve. Travel.", "Number puzzles set in world cities", (122, 74, 140), (36, 22, 66), 2225),
    "tour":     ("Tour the world", "New cities, bigger boards", (52, 112, 186), (24, 30, 74), 2225),
    "win":      ("Earn three stars", "Fewer moves, better score", (140, 82, 128), (34, 22, 62), 2225),
    "home":     ("A new puzzle daily", "Keep your streak and open chests", (62, 132, 204), (26, 38, 86), 2068),
    "passport": ("Fill your passport", "Stamp 21 cities", (168, 56, 88), (44, 20, 56), 2225),
    "rewards":  ("Unlock rewards", "Achievements, ranks and chests", (176, 112, 40), (48, 28, 58), 2225),
    "colours":  ("Bright, smooth tiles", "Undo, hints and free moves", (30, 138, 146), (20, 34, 72), 2225),
}

def font(name, size):
    return ImageFont.truetype(os.path.join(FONTS, name), size)

def fit(draw, text, name, size, width):
    f = font(name, size)
    while draw.textlength(text, font=f) > width:
        size -= 2
        f = font(name, size)
    return f

def make(name, src, out):
    head, sub, top, bottom, cut = SHOTS[name]
    grad = Image.new("RGB", (1, H))
    for y in range(H):
        t = y / (H - 1)
        grad.putpixel((0, y), tuple(round(a + (b - a) * t) for a, b in zip(top, bottom)))
    img = grad.resize((W, H)).convert("RGBA")
    d = ImageDraw.Draw(img)
    d.text((W / 2, 128), head, font=fit(d, head, "lilita_one.ttf", 100, 980), fill=CREAM, anchor="mm")
    d.text((W / 2, 232), sub, font=fit(d, sub, "nunito_bold.ttf", 46, 960), fill=SOFT, anchor="mm")

    shot = Image.open(src).convert("RGB").crop((0, 112, 1080, cut))
    scale = min(1550 / shot.height, 900 / shot.width)
    shot = shot.resize((round(shot.width * scale), round(shot.height * scale)), Image.LANCZOS)
    x, y, r, b = (W - shot.width) // 2, 312 + (1550 - shot.height) // 2, 48, 12

    box = (x - b, y - b, x + shot.width + b, y + shot.height + b)
    shadow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle((box[0], box[1] + 18, box[2], box[3] + 18), r + b, fill=(8, 4, 24, 150))
    img.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(24)))
    ImageDraw.Draw(img).rounded_rectangle(box, r + b, fill=(255, 214, 140, 255))
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, shot.width - 1, shot.height - 1), r, fill=255)
    img.paste(shot, (x, y), mask)
    img.convert("RGB").save(out, "PNG", optimize=True)

if __name__ == "__main__":
    out_dir = sys.argv[1]
    os.makedirs(out_dir, exist_ok=True)
    for i, arg in enumerate(sys.argv[2:], 1):
        name, src = arg.split("=", 1)
        make(name, src, os.path.join(out_dir, f"{i:02d}-{name}.png"))
