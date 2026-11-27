"""Draws the 1024x500 Play Store feature graphic from the icon art. Run from the repo root."""
import subprocess

import make_icon as m
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen


def text(words, x, baseline, size, colour):
    glyphs, cmap = m.FONT.getGlyphSet(), m.FONT.getBestCmap()
    s = size / m.FONT["head"].unitsPerEm
    out = []
    for ch in words:
        name = cmap[ord(ch)]
        pen = SVGPathPen(glyphs)
        glyphs[name].draw(TransformPen(pen, (s, 0, 0, -s, x, baseline)))
        out.append(pen.getCommands())
        x += glyphs[name].width * s
    d = "".join(out)
    return f'<path d="{d}" fill="#2A1450" fill-opacity="0.45" transform="translate(0 5)"/><path d="{d}" fill="{colour}"/>'


def inner(layers, key):
    """The drawing without its outer tag; gradient names get a prefix so parts do not share them."""
    full = m.svg(layers, "0 0 108 108")
    body = full[full.index(">") + 1:-len("</svg>")]
    return body.replace('id="g', f'id="{key}').replace("url(#g", f"url(#{key}")


# the sky and skyline stretched across the banner, the board on the right, the name on the left
art = (f'<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">'
       f'<svg width="1024" height="500" viewBox="0 12 108 90" preserveAspectRatio="none">{inner([(m.background()[:3], False)], "sky")}</svg>'
       f'<svg x="0" y="130" width="520" height="500" viewBox="2 22 92 88" preserveAspectRatio="xMinYMax slice">{inner([(m.background()[9:], False)], "left")}</svg>'
       f'<svg x="505" y="130" width="520" height="500" viewBox="6 22 92 88" preserveAspectRatio="xMaxYMax slice">{inner([(m.background()[-4:], False)], "right")}</svg>'
       f'<svg x="600" y="40" width="400" height="400" viewBox="24 24 60 60">{inner([(m.foreground(), False)], "board")}</svg>'
       + text("Sliding", 64, 150, 118, "#FFF4E2") + text("Numbers", 64, 262, 118, "#FFD34D")
       + text("City Puzzle", 68, 332, 52, "#FFF4E2") + "</svg>")
subprocess.run(["rsvg-convert", "-o", "docs/store/feature-graphic.png"], input=art.encode(), check=True)
