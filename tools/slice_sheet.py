#!/usr/bin/env python3
"""Slice an icon sheet into transparent WebP icons. The flat background color is detected from the corners.

  python3 tools/slice_sheet.py SHEET.png NAMES.txt OUT_DIR [--grid 4] [--size 256] [--also 384]

NAMES.txt has one icon name per line, row-major. Needs: pip install pillow
"""
import argparse
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter


def key_color(img):
    """The background key: median of the four corner pixels (should be #FF00FF)."""
    w, h = img.size
    px = [img.getpixel(p)[:3] for p in ((2, 2), (w - 3, 2), (2, h - 3), (w - 3, h - 3))]
    return tuple(sorted(c[i] for c in px)[1] for i in range(3))


def remove_key(cell, key, tol=48):
    """Remove the flat background by flood-filling inward from the cell border.

    Works for any flat color (magenta, pink, white...) and never punches holes in icon
    interiors that happen to share the color, because only border-connected pixels go.
    """
    rgb = cell.convert("RGB")
    work = rgb.copy()
    sentinel = (1, 2, 3)
    w, h = work.size
    for pt in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1), (w // 2, 0), (w // 2, h - 1), (0, h // 2), (w - 1, h // 2)]:
        if work.getpixel(pt) != sentinel and sum(abs(a - b) for a, b in zip(work.getpixel(pt), key)) <= tol * 3:
            ImageDraw.floodfill(work, pt, sentinel, thresh=tol)
    mask = Image.new("L", work.size, 255)
    mpx, wpx = mask.load(), work.load()
    for y in range(h):
        for x in range(w):
            if wpx[x, y] == sentinel:
                mpx[x, y] = 0
    # Feather the edge a touch so icons do not look cut with scissors.
    mask = mask.filter(ImageFilter.GaussianBlur(0.8)).point(lambda v: 0 if v < 40 else min(255, int(v * 1.15)))
    out = rgb.convert("RGBA")
    out.putalpha(mask)
    # Despill: pixels at the edge that lean toward the key color get pulled back.
    px = out.load()
    kr, kg, kb = key
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if 0 < a < 255:
                px[x, y] = (min(255, r + (r - kr) // 6), min(255, g + (g - kg) // 6), min(255, b + (b - kb) // 6), a)
    return out


def square_trim(cell, size, margin=0.06):
    bbox = cell.getchannel("A").point(lambda v: 255 if v > 8 else 0).getbbox()
    if bbox is None:
        return None
    cell = cell.crop(bbox)
    side = int(max(cell.size) * (1 + 2 * margin))
    out = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    out.paste(cell, ((side - cell.width) // 2, (side - cell.height) // 2))
    return out.resize((size, size), Image.LANCZOS)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("sheet")
    ap.add_argument("names")
    ap.add_argument("out")
    ap.add_argument("--grid", type=int, default=4)
    ap.add_argument("--size", type=int, default=256)
    ap.add_argument("--also", type=int, help="also export this size into OUT/<size>/")
    a = ap.parse_args()

    names = [n.strip() for n in Path(a.names).read_text().splitlines() if n.strip()]
    if len(names) != a.grid * a.grid:
        sys.exit(f"error: expected {a.grid * a.grid} names, got {len(names)}")
    sheet = Image.open(a.sheet).convert("RGB")
    key = key_color(sheet)
    print(f"sheet {sheet.size}, background color {key}")

    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    cw, ch = sheet.width // a.grid, sheet.height // a.grid
    total, bad = 0, []
    for i, name in enumerate(names):
        r, c = divmod(i, a.grid)
        cell = sheet.crop((c * cw, r * ch, (c + 1) * cw, (r + 1) * ch))
        cell = remove_key(cell, key)
        for size, dest in [(a.size, out)] + ([(a.also, out / str(a.also))] if a.also else []):
            icon = square_trim(cell, size)
            if icon is None:
                bad.append(name)
                break
            dest.mkdir(parents=True, exist_ok=True)
            f = dest / f"{name}.webp"
            icon.save(f, "WEBP", quality=90, method=6)
            if size == a.size:
                total += f.stat().st_size
    print(f"wrote {len(names) - len(bad)} icons, {total / 1024:.0f} KB total")
    if bad:
        print("empty cells (nothing drawn):", ", ".join(bad))
        sys.exit(1)


if __name__ == "__main__":
    main()
