#!/usr/bin/env python3
"""Slice an icon sheet into transparent WebP icons.

  python3 tools/slice_sheet.py SHEET.png NAMES.txt OUT_DIR [--grid 4] [--size 256] [--also 384]

NAMES.txt has one icon name per line, row-major. Needs: pip install pillow
"""
import argparse
import sys
from pathlib import Path

from PIL import Image


def key_color(img):
    """The background key: median of the four corner pixels (should be #FF00FF)."""
    w, h = img.size
    px = [img.getpixel(p)[:3] for p in ((2, 2), (w - 3, 2), (2, h - 3), (w - 3, h - 3))]
    return tuple(sorted(c[i] for c in px)[1] for i in range(3))


def remove_key(cell, key, lo=60, hi=150):
    """Soft alpha by distance to the key color, then despill magenta from the edge pixels."""
    cell = cell.convert("RGBA")
    px = cell.load()
    kr, kg, kb = key
    for y in range(cell.height):
        for x in range(cell.width):
            r, g, b, _ = px[x, y]
            d = ((r - kr) ** 2 + (g - kg) ** 2 + (b - kb) ** 2) ** 0.5
            if d <= lo:
                px[x, y] = (r, g, b, 0)
            elif d < hi:
                a = int(255 * (d - lo) / (hi - lo))
                # despill: pull the magenta tint out of semi-transparent edge pixels
                m = min(r, b)
                if m > g:
                    k = (m - g) * (1 - a / 255) * 0.9
                    r, b = int(r - k), int(b - k)
                px[x, y] = (r, g, b, a)
    return cell


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
    print(f"sheet {sheet.size}, key color {key}")
    if sum(abs(c - t) for c, t in zip(key, (255, 0, 255))) > 90:
        print("warning: corner color is not close to #FF00FF; check the sheet background")

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
