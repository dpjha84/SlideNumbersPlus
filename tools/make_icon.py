"""Draws the launcher icon, splash art and Play Store icon from one set of shapes.

Needs fonttools, Pillow and rsvg-convert. Run from the repo root: python3 tools/make_icon.py
"""
import os
import subprocess

from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont
from PIL import Image, ImageDraw

RES = "app/src/main/res"
FONT = TTFont(f"{RES}/font/lilita_one.ttf")


def rr(x, y, w, h, r):
    """Rounded rectangle path."""
    return (f"M{x + r},{y}h{w - 2 * r}a{r},{r} 0 0 1 {r},{r}v{h - 2 * r}a{r},{r} 0 0 1 {-r},{r}"
            f"h{-(w - 2 * r)}a{r},{r} 0 0 1 {-r},{-r}v{-(h - 2 * r)}a{r},{r} 0 0 1 {r},{-r}z")


def circle(cx, cy, r):
    return f"M{cx - r},{cy}a{r},{r} 0 1 0 {2 * r},0a{r},{r} 0 1 0 {-2 * r},0z"


def digit(ch, cx, cy, height):
    glyphs = FONT.getGlyphSet()
    name = FONT.getBestCmap()[ord(ch)]
    bounds = BoundsPen(glyphs)
    glyphs[name].draw(bounds)
    x0, y0, x1, y1 = bounds.bounds
    s = height / (y1 - y0)
    pen = SVGPathPen(glyphs, ntos=lambda v: f"{v:.2f}".rstrip("0").rstrip("."))
    glyphs[name].draw(TransformPen(pen, (s, 0, 0, -s, cx - s * (x0 + x1) / 2, cy + s * (y0 + y1) / 2)))
    return pen.getCommands()


# a shape is (path, fill, alpha); fill is a colour or (y1, y2, [(offset, colour)...])
def background():
    far, near = "#6D3F8E", "#2A1A55"
    shapes = [
        ("M0,0h108v108h-108z", (0, 100, [(0, "#2A1F73"), (0.38, "#6A3A9E"), (0.66, "#E8628A"), (0.86, "#FFB25E"), (1, "#FFD27A")]), 1),
        (circle(54, 74, 27), "#FFE3A0", 0.45),
        (circle(54, 74, 19), "#FFF0C2", 0.55),
    ]
    for x, y, r in [(26, 25, .8), (41, 19, .6), (72, 22, .9), (86, 36, .6), (20, 43, .6), (60, 15, .5)]:
        shapes.append((circle(x, y, r), "#FFF6DD", 0.85))
    # far roofs, one unbroken band
    shapes.append(("M0,108V81h8v-2h5v3h5v-4h4v2h4v-3q3,-4 6,0v4h5v-3h4v2h3v-4h2v-3h1v3h2v4h5v-2h4v3h4v-4q3,-4 6,0v3h5v-2h4v-2h4v4h6v-3h5v2h8v26z", far, 0.9))
    # Eiffel tower on the left
    shapes.append(("M15,90q8,-14 9.4,-40h1.2q1.4,26 9.4,40h-5q-5,-9 -10,0z", near, 1))
    shapes += [(rr(21.4, 66, 7.2, 1.8, 0.6), near, 1), (rr(24.5, 44, 1, 8, 0.5), near, 1)]
    # clock tower on the right
    shapes.append(("M77,90v-35h-1.2v-3h2l4.7,-15l4.7,15h2v3h-1.2v35z", near, 1))
    shapes.append((circle(82.5, 59.5, 3.1), "#FFE9A8", 1))
    # river bank
    shapes.append(("M0,86q27,-4 54,0t54,0v22H0z", near, 1))
    for x, y, w in [(40, 90.5, 12), (56, 93.5, 17), (33, 96, 9)]:
        shapes.append((rr(x, y, w, 1, 0.5), "#FFC978", 0.7))
    return shapes


# the board sits a little high and small, so the skyline shows below it
FIT = "matrix(0.9 0 0 0.9 5.4 2.4)"


def tile(x, y, ch, light, dark, edge, size=23):
    cx = x + size / 2
    return [
        (rr(x, y + 1.9, size, size, 6), "#14082E", 0.35),
        (rr(x, y + 1.2, size, size - 0.4, 6), edge, 1),
        (rr(x, y - 0.4, size, size - 1.1, 6), (y, y + size, [(0, light), (1, dark)]), 1),
        (rr(x + 2.2, y + 0.8, size - 4.4, 7.2, 3.6), "#FFFFFF", 0.24),
        (digit(ch, cx, y + size / 2 + 0.9, 12.6), edge, 0.55),
        (digit(ch, cx, y + size / 2 - 0.1, 12.6), "#FFFFFF", 1),
    ]


def foreground():
    shapes = [
        (rr(27, 28.6, 54, 54, 13), "#160A3A", 0.30),
        (rr(27, 27, 54, 54, 13), "#FFF4E2", 0.92),
        (rr(28.6, 28.6, 50.8, 50.8, 11.5), (28, 80, [(0, "#4A2E86"), (1, "#2C1A5C")]), 1),
        (rr(55, 55, 23, 23, 6), "#160A3A", 0.45),
    ]
    shapes += tile(30, 30, "1", "#FFE27A", "#FF9A1F", "#C25E0A")
    shapes += tile(55, 30, "2", "#62EEDC", "#0FA596", "#06665F")
    # the third tile is on its way into the gap
    for y, x, w in [(60.5, 31.5, 7), (66.5, 30.5, 8.5), (72.5, 32.5, 6)]:
        shapes.append((rr(x, y, w, 1.8, 0.9), "#FFD9F0", 0.75))
    shapes += tile(43, 55, "3", "#FF86CC", "#CC2A8E", "#85125C")
    return shapes


def monochrome():
    def cut(x, y, ch):
        return (rr(x, y, 23, 23, 6) + digit(ch, x + 11.5, y + 11.5, 12.6), "#FFFFFF", 1)
    return [cut(30, 30, "1"), cut(55, 30, "2"), cut(43, 55, "3"),
            (rr(30.5, 65.4, 8.5, 2.2, 1.1), "#FFFFFF", 1)]


def vector(shapes, evenodd=False, fit=False):
    out = ['<?xml version="1.0" encoding="utf-8"?>', '<!-- Made by tools/make_icon.py -->',
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
           '    xmlns:aapt="http://schemas.android.com/aapt"',
           '    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">']
    rule = ' android:fillType="evenOdd"' if evenodd else ""

    def argb(colour, alpha):
        return f"#{round(alpha * 255):02X}{colour[1:]}"

    if fit:
        out.append('    <group android:scaleX="0.9" android:scaleY="0.9" android:translateX="5.4" android:translateY="2.4">')
    for d, fill, alpha in shapes:
        if isinstance(fill, str):
            out.append(f'    <path{rule} android:fillColor="{argb(fill, alpha)}" android:pathData="{d}" />')
        else:
            y1, y2, stops = fill
            out += [f'    <path{rule} android:pathData="{d}">', '        <aapt:attr name="android:fillColor">',
                    f'            <gradient android:type="linear" android:startX="0" android:startY="{y1}" android:endX="0" android:endY="{y2}">']
            out += [f'                <item android:offset="{o}" android:color="{argb(c, alpha)}" />' for o, c in stops]
            out += ['            </gradient>', '        </aapt:attr>', '    </path>']
    return "\n".join(out + (["    </group>"] if fit else []) + ["</vector>", ""])


def svg(layers, box, evenodd=False):
    """layers: (shapes, fitted) pairs, back to front."""
    defs, body = [], []
    for shapes, fit in layers:
        body.append(f'<g transform="{FIT}">' if fit else "<g>")
        for d, fill, alpha in shapes:
            if not isinstance(fill, str):
                y1, y2, stops = fill
                marks = "".join(f'<stop offset="{o}" stop-color="{c}"/>' for o, c in stops)
                key = f"g{len(defs)}"
                defs.append(f'<linearGradient id="{key}" gradientUnits="userSpaceOnUse" x1="0" y1="{y1}" x2="0" y2="{y2}">{marks}</linearGradient>')
                fill = f"url(#{key})"
            rule = ' fill-rule="evenodd"' if evenodd else ""
            body.append(f'<path d="{d}" fill="{fill}" fill-opacity="{alpha}"{rule}/>')
        body.append("</g>")
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{box}"><defs>{"".join(defs)}</defs>{"".join(body)}</svg>'


def png(layers, box, size, path, evenodd=False):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    subprocess.run(["rsvg-convert", "-w", str(size), "-h", str(size), "-o", path],
                   input=svg(layers, box, evenodd).encode(), check=True)


def masked(path, radius):
    image = Image.open(path).convert("RGBA")
    big = image.width * 4
    mask = Image.new("L", (big, big), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, big - 1, big - 1), int(big * radius), fill=255)
    image.putalpha(mask.resize(image.size, Image.LANCZOS))
    image.save(path)


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(text)


def main():
    back, front = background(), foreground()
    write(f"{RES}/drawable-v26/ic_launcher_background.xml", vector(back))
    write(f"{RES}/drawable-v26/ic_launcher_foreground.xml", vector(front, fit=True))
    write(f"{RES}/drawable-v26/ic_launcher_monochrome.xml", vector(monochrome(), evenodd=True, fit=True))
    adaptive = ('<?xml version="1.0" encoding="utf-8"?>\n'
                '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
                '    <background android:drawable="@drawable/ic_launcher_background" />\n'
                '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
                '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
                '</adaptive-icon>\n')
    for name in ("ic_launcher", "ic_launcher_round"):
        write(f"{RES}/mipmap-anydpi-v26/{name}.xml", adaptive)
    # phones older than Android 8 get flat pictures
    for density, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
        for name, radius in [("ic_launcher", 0.22), ("ic_launcher_round", 0.5)]:
            path = f"{RES}/mipmap-{density}/{name}.png"
            png([(back, False), (front, True)], "18 18 72 72", size, path)
            masked(path, radius)
    # unscaled by the launcher, so sized per screen density (128dp) for the window before Android 12
    for density, size in [("mdpi", 128), ("hdpi", 192), ("xhdpi", 256), ("xxhdpi", 384), ("xxxhdpi", 512)]:
        png([(front, False)], "22 22 64 64", size, f"{RES}/drawable-{density}/splash_icon.png")
    png([(back, False), (front, True)], "16 16 76 76", 512, "docs/store/icon-512.png")


if __name__ == "__main__":
    main()
