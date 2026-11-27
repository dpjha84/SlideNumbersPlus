#!/usr/bin/env python3
"""Vertical (1080x1920, 30 fps) promo reel from phone screenshots (1080x2412) and a music track.

Usage: make_promo.py OUT.mp4 --music TRACK.wav game=.. win=.. tour=.. home=.. passport=.. [--end "line"] [--stills DIR]
`game` must be the London level 2 screen (board 6 2 3 / 4 1 8 / 5 7 _), which the reel solves for real."""
import sys, os, math, random, subprocess
from collections import deque
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
W, H, FPS = 1080, 1920, 30
CREAM, GOLD = (255, 244, 222), (255, 190, 60)
START = (6, 2, 3, 4, 1, 8, 5, 7, 0)
GOAL = (1, 2, 3, 4, 5, 6, 7, 8, 0)

args = sys.argv[1:]
out = args.pop(0)
end_line, stills, music = "Coming soon on Google Play", None, None
paths = {}
while args:
    a = args.pop(0)
    if a == "--end": end_line = args.pop(0)
    elif a == "--stills": stills = args.pop(0)
    elif a == "--music": music = args.pop(0)
    else:
        k, v = a.split("=", 1)
        paths[k] = Image.open(v).convert("RGB")

def font(size, name="lilita_one.ttf"):
    return ImageFont.truetype(os.path.join(ROOT, "app/src/main/res/font", name), size)

def solve(start):
    prev, q = {start: None}, deque([start])
    while q:
        s = q.popleft()
        if s == GOAL: break
        g = s.index(0)
        for c in range(9):
            if c == g or (c // 3 != g // 3 and c % 3 != g % 3): continue
            step = (1 if c > g else -1) if c // 3 == g // 3 else (3 if c > g else -3)
            n, at = list(s), g
            while at != c:
                n[at] = n[at + step]; at += step
            n[c] = 0
            n = tuple(n)
            if n not in prev:
                prev[n] = s; q.append(n)
    path = [GOAL]
    while prev[path[-1]] is not None: path.append(prev[path[-1]])
    return path[::-1]

STATES = solve(START)

# ---- board drawn over the real game screen ----
XS, YS, TW, TH = [96, 399, 702], [1036, 1339, 1642], 282, 280
game = paths["game"]
COLORS = {v: game.getpixel((XS[i % 3] + 55, YS[i // 3] + 190)) for i, v in enumerate(START) if v}

def tile(v, home):
    col = COLORS[v]
    im = Image.new("RGBA", (TW, TH), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    dark = tuple(int(c * 0.68) for c in col)
    d.rounded_rectangle((0, 14, TW - 1, TH - 1), 52, fill=dark)
    grad = Image.new("RGB", (1, TH - 16))
    for y in range(TH - 16):
        k = 1.18 - 0.28 * y / (TH - 16)
        grad.putpixel((0, y), tuple(min(255, int(c * k)) for c in col))
    mask = Image.new("L", (TW, TH - 16), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, TW - 1, TH - 17), 52, fill=255)
    im.paste(grad.resize((TW, TH - 16)), (0, 0), mask)
    gloss = Image.new("RGBA", (TW, TH), (0, 0, 0, 0))
    ImageDraw.Draw(gloss).rounded_rectangle((20, 10, TW - 20, 105), 40, fill=(255, 255, 255, 48))
    im.alpha_composite(gloss)
    d = ImageDraw.Draw(im)
    if home:
        d.rounded_rectangle((3, 3, TW - 4, TH - 19), 50, outline=(255, 255, 255), width=8)
        d.ellipse((TW - 52, 24, TW - 30, 46), fill=(255, 255, 255))
    d.text((TW / 2, TH / 2 + 2), str(v), font=font(160), fill=dark, anchor="mm")
    d.text((TW / 2, TH / 2 - 8), str(v), font=font(160), fill=(255, 255, 255), anchor="mm")
    return im

TILES = {(v, h): tile(v, h) for v in range(1, 9) for h in (False, True)}
base = game.copy()
_d = ImageDraw.Draw(base)
_d.rectangle((XS[0] - 14, YS[0] - 14, XS[2] + TW + 14, YS[2] + TH + 24), fill=(48, 82, 128))
for y in YS:
    for x in XS:
        _d.rounded_rectangle((x + 4, y + 4, x + TW - 4, y + TH - 4), 52, fill=(40, 70, 114), outline=(96, 134, 180), width=4)

def ease(t):
    t = max(0.0, min(1.0, t))
    return 1 - (1 - t) ** 3

def game_screen(i, p):
    """State i moving toward i+1 with progress p."""
    im = base.copy()
    a = STATES[i]
    b = STATES[min(i + 1, len(STATES) - 1)]
    for cell, v in enumerate(a):
        if not v: continue
        to = b.index(v)
        k = ease(p)
        x = XS[cell % 3] + (XS[to % 3] - XS[cell % 3]) * k
        y = YS[cell // 3] + (YS[to // 3] - YS[cell // 3]) * k
        t = TILES[(v, (to if p > 0.6 else cell) == v - 1)]
        im.paste(t, (round(x), round(y)), t)
    moves = i + (1 if p > 0.5 else 0)
    d = ImageDraw.Draw(im)
    d.rectangle((165, 385, 285, 456), fill=game.getpixel((140, 420)))
    d.text((224, 420), str(moves), font=font(74), fill=CREAM, anchor="mm")
    return im

# ---- layout ----
def gradient(top, bottom):
    g = Image.new("RGB", (1, H))
    for y in range(H):
        g.putpixel((0, y), tuple(round(a + (b - a) * y / H) for a, b in zip(top, bottom)))
    return g.resize((W, H)).convert("RGBA")

def card(shot, width=756):
    h = round(shot.height * width / shot.width)
    shot = shot.resize((width, h), Image.BILINEAR)
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, width - 1, h - 1), 46, fill=255)
    c = Image.new("RGBA", (width + 24, h + 24), (0, 0, 0, 0))
    ImageDraw.Draw(c).rounded_rectangle((0, 0, width + 23, h + 23), 58, fill=(255, 214, 140))
    c.paste(shot, (12, 12), mask)
    return c

def headline(im, text, sub, t):
    k = ease(t / 0.35)
    layer = Image.new("RGBA", (W, 300), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    size = 112
    while d.textlength(text, font=font(size)) > 980: size -= 4
    d.text((W / 2, 100), text, font=font(size), fill=CREAM, anchor="mm", stroke_width=8, stroke_fill=(46, 22, 80))
    d.text((W / 2, 212), sub, font=font(52, "nunito_black.ttf"), fill=(255, 226, 170), anchor="mm")
    layer.putalpha(layer.getchannel("A").point(lambda a: int(a * k)))
    im.alpha_composite(layer, (0, int(150 + (1 - k) * 40)))

random.seed(3)
CONFETTI = [(random.random() * W, random.random() * -900, random.uniform(500, 1100), random.choice(
    [(255, 94, 120), (255, 200, 70), (67, 230, 210), (170, 130, 255)]), random.uniform(0, 6)) for _ in range(130)]

def confetti(im, t):
    d = ImageDraw.Draw(im)
    for x, y, v, c, ph in CONFETTI:
        px, py = x + 30 * math.sin(t * 4 + ph), y + v * t * 1.6
        if -30 < py < H:
            w = 8 + 10 * abs(math.sin(t * 7 + ph))
            d.rectangle((px, py, px + w, py + 24), fill=c)

BG = {"game": gradient((52, 128, 206), (22, 28, 72)), "win": gradient((140, 82, 128), (30, 20, 58)),
      "tour": gradient((44, 104, 180), (22, 28, 70)), "home": gradient((84, 72, 168), (26, 22, 62)),
      "passport": gradient((168, 56, 88), (40, 18, 52)), "end": gradient((118, 70, 168), (24, 18, 56))}

crop = lambda k, bottom=2225: paths[k].crop((0, 112, 1080, bottom))
WIN, HOME, PASS = card(crop("win")), card(crop("home", 2068)), card(crop("passport", 2012))
TOUR = paths["tour"]

STEP = 0.25                 # seconds per move: an eighth note at 120 BPM
INTRO = 0.25
# lengths follow the music's beat (0.5 s)
SCENES = [("game", 5.5), ("win", 2.5), ("tour", 3.0), ("home", 2.5), ("passport", 2.5), ("end", 4.0)]
TOTAL = sum(d for _, d in SCENES)
ICON = Image.open(os.path.join(ROOT, "docs/store/icon-512.png")).convert("RGBA").resize((460, 460))
mask = Image.new("L", ICON.size, 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, 459, 459), 104, fill=255)
ICON.putalpha(mask)

def scene(name, t, dur):
    im = BG[name].copy()
    rise = int((1 - ease(t / 0.4)) * 140)
    if name == "game":
        x = t - INTRO
        i = max(0, min(len(STATES) - 1, int(x / STEP)))
        p = 0 if x < 0 or i >= len(STATES) - 1 else (x - i * STEP) / (STEP * 0.6)
        done = i >= len(STATES) - 1
        im.alpha_composite(card(game_screen(i, min(1, p)).crop((0, 112, 1080, 2225))), (150, 440))
        headline(im, "Solved!" if done else "Can you solve it?", f"{len(STATES) - 1} moves is the best" if done else "Slide the tiles into order", 1 if not done else x - STEP * (len(STATES) - 1))
    elif name == "win":
        im.alpha_composite(WIN, (150, 440 + rise))
        headline(im, "Earn three stars", "Every move counts", t)
        confetti(im, t)
    elif name == "tour":
        # whole city cards only (London, then Paris), sliding in from alternate sides
        for n, (top, y) in enumerate(((410, 500), (1124, 1160))):
            k = ease((t - 0.25 * n) / 0.5)
            city = TOUR.crop((48, top, 1032, top + 671)).resize((900, 614), Image.BILINEAR).convert("RGBA")
            m = Image.new("L", city.size, 0)
            ImageDraw.Draw(m).rounded_rectangle((0, 0, 899, 613), 58, fill=255)
            city.putalpha(m)
            im.alpha_composite(city, (90 + int((1 - k) * (-1000 if n == 0 else 1000)), y))
        headline(im, "Tour the world", "New cities, bigger boards", t)
    elif name == "home":
        im.alpha_composite(HOME, (150, 440 + rise))
        headline(im, "A new puzzle daily", "Build your streak", t)
    elif name == "passport":
        im.alpha_composite(PASS, (150, 440 + rise))
        headline(im, "Stamp your passport", "21 cities to collect", t)
    else:
        s = 0.7 + 0.3 * ease(t / 0.5)
        icon = ICON.resize((int(460 * s), int(460 * s)))
        im.alpha_composite(icon, ((W - icon.width) // 2, 640 - icon.height // 2))
        d = ImageDraw.Draw(im)
        d.text((W / 2, 1000), "Sliding Numbers", font=font(120), fill=CREAM, anchor="mm", stroke_width=8, stroke_fill=(46, 22, 80))
        d.text((W / 2, 1115), "City Puzzle", font=font(84), fill=GOLD, anchor="mm")
        d.text((W / 2, 1235), "A fresh new look", font=font(52, "nunito_black.ttf"), fill=CREAM, anchor="mm")
        w = d.textlength(end_line, font=font(52)) + 100
        d.rounded_rectangle(((W - w) / 2, 1315, (W + w) / 2, 1435), 60, fill=GOLD)
        d.text((W / 2, 1375), end_line, font=font(52), fill=(74, 36, 0), anchor="mm")
        d.text((W / 2, 1540), "PixelPocket  @pixelpocket.apps", font=font(42, "nunito_bold.ttf"), fill=(214, 200, 244), anchor="mm")
        if t < 2: confetti(im, t)
    fade = min(1.0, (dur - t) / 0.18) if name != "end" else 1.0
    if fade < 1:
        im = Image.blend(Image.new("RGBA", im.size, (20, 14, 44, 255)), im, max(0, fade))
    return im.convert("RGB")

def frame(T):
    acc = 0
    for name, dur in SCENES:
        if T < acc + dur: return scene(name, T - acc, dur)
        acc += dur
    return scene("end", SCENES[-1][1] - 0.01, SCENES[-1][1])

if stills:
    os.makedirs(stills, exist_ok=True)
    acc, shots = 0, []
    for name, dur in SCENES:
        shots.append(frame(acc + dur * (0.45 if name == "game" else 0.7)))
        acc += dur
    sheet = Image.new("RGB", (360 * 3 + 40, 640 * 2 + 30), "white")
    for i, s in enumerate(shots):
        sheet.paste(s.resize((360, 640)), (10 + (i % 3) * 370, 10 + (i // 3) * 650))
    sheet.save(os.path.join(stills, "sheet.jpg"))
    print("seconds", round(TOTAL, 1), "moves", len(STATES) - 1)
    sys.exit()

# ---- sound ----
assert music, "pass --music TRACK.wav (made by make_promo_music.py)"
tmp = out[:-4] + ".part.mp4"
cmd = ["ffmpeg", "-y", "-v", "error", "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", f"{W}x{H}", "-r", str(FPS), "-i", "-",
       "-i", music, "-map", "0:v", "-map", "1:a", "-shortest",
       "-c:v", "libx264", "-preset", "veryfast", "-crf", "19", "-pix_fmt", "yuv420p",
       "-c:a", "aac", "-b:a", "192k", "-ar", "44100", "-movflags", "+faststart", tmp]
p = subprocess.Popen(cmd, stdin=subprocess.PIPE)
for i in range(int(TOTAL * FPS)):
    p.stdin.write(frame(i / FPS).tobytes())
p.stdin.close()
assert p.wait() == 0
os.replace(tmp, out)
