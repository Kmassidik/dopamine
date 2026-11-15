#!/usr/bin/env python3
"""Single source for icon sheets. Writes ASSETS.md and tools/sheets/sheetNN.txt (names, row-major)."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
from sheet_data_toy import TOY_MASCOT, TOY_SHEETS, TOY_STYLE  # noqa: E402

BOTS_CORE = (
    "Mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with "
    "rounded joins and a slightly hand-drawn wobble. Simple cel shading: a few white highlight dashes and one or "
    "two flat shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living "
    "character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), "
    "no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). "
    "Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. "
    "No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers."
)
MONO = " Only pure black, white and two flat mid-greys: NO color at all inside the icons."
COLOR = (
    " Fill colors: use only fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE and white, with a "
    "deeper tone of the same color for the shade plane. Each icon is mostly one main color; spread green, yellow "
    "and pink evenly across the sheet. The black outline stays."
)
ACCENT = (
    " Only pure black, white and two flat mid-greys, plus exactly ONE accent color, bubblegum pink #FF7FAE, used "
    "sparingly on a single part of each icon (a cheek, a tip, a button)."
)


def layout(bg, forbid):
    return (
        "Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, "
        f"about 12% padding, all icons the same visual size, each a single object. Flat solid {bg} background "
        "across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing "
        f"cells. Never use {forbid} inside an icon."
    )


YELLOW = ("warm yellow #F9D56B", "yellow")
PALE = ("pale blue-grey #DCE6F2", "that blue-grey")

STYLES = {
    "A-mono": dict(
        title="A. Bots mono (matches your sample)",
        blurb="Black, white and grey only, faces on every object. Color comes from the tiles behind the icons.",
        style=BOTS_CORE + MONO, bg=YELLOW, sheets="bots", mascot="bot"),
    "B-color": dict(
        title="B. Bots color",
        blurb="Same Bots line style and faces, filled with the app's green, yellow and pink.",
        style=BOTS_CORE + COLOR, bg=PALE, sheets="bots", mascot="bot"),
    "C-toy": dict(
        title="C. Toy 3D (no faces)",
        blurb="Soft colorful toy-like icons with a green ink outline. No faces. The very first style.",
        style=TOY_STYLE, bg=("pale blue-grey #DCE6F2", "magenta or purple"), sheets="toy", mascot="toy"),
    "D-accent": dict(
        title="D. Bots mono + pink accent",
        blurb="Like A, with one pink accent on each icon. A middle path.",
        style=BOTS_CORE + ACCENT, bg=YELLOW, sheets="bots", mascot="bot"),
}

BOT_NOTE = (
    "The Bot: a small friendly robot with a rounded body, a screen face showing two dot eyes, a short antenna, "
    "round ear discs, stubby arms and legs, same style. First generate ONE character reference, then reuse that "
    "exact robot in every cell. Consistency across 16 cells is hard; if it drifts, generate 4 cells at a time and "
    "merge them, or skip this sheet."
)
OUTPUT = "Highest square resolution available, ideally 3072x3072. PNG."

BOTS_SHEETS = [
    ("01", "Core UI", [
        ("ic_streak", "a small flame with two dot eyes and a tiny happy mouth, the tip wobbling"),
        ("ic_xp", "a chunky five-point star with two dot eyes and a spark"),
        ("ic_heart", "a plump heart with two dot eyes and a white highlight"),
        ("ic_goal", "an archery target with an arrow in the bullseye, the bullseye has two dot eyes"),
        ("ic_complete", "a round badge with a bold check mark and a small ribbon tail, two dot eyes above the check"),
        ("ic_locked", "a closed padlock with two dot eyes on its body"),
        ("ic_review", "a fan of three flash cards, the front card has two dot eyes"),
        ("ic_trophy", "a small cup with two handles and two dot eyes"),
        ("ic_update", "a cloud with a thick down arrow, two dot eyes on the cloud"),
        ("ic_offline", "a cloud with a diagonal slash and small x eyes"),
        ("ic_correct", "a circle with a thick check mark and two happy dot eyes"),
        ("ic_wrong", "a circle with a thick cross and two droopy dot eyes"),
        ("ic_settings", "a gear with two dot eyes in its center"),
        ("ic_stats", "three rounded bars of rising height, the tallest has two dot eyes and a tiny star"),
        ("ic_home", "a little house with a round door and two dot-eye windows"),
        ("ic_path", "a winding road with three round stops and a tiny pin at the end"),
    ]),
    ("02", "Math", [
        ("ic_math", "a chunky plus-minus sign with two dot eyes"),
        ("ic_fraction", "a fraction bar with a big dot above and a big dot below that act as eyes"),
        ("ic_sqrt", "a bold square root symbol with two dot eyes under the roof"),
        ("ic_pi", "a bold pi symbol with two dot eyes on the top bar"),
        ("ic_algebra", "a balance scale, a star on one pan and two cubes on the other, two dot eyes on the pivot"),
        ("ic_geometry", "a set-square triangle ruler with a small angle arc and two dot eyes"),
        ("ic_compass", "a drawing compass drawing a circle arc, two dot eyes on the hinge"),
        ("ic_protractor", "a half-circle protractor with a pointer and two dot eyes"),
        ("ic_graph", "a coordinate grid with a smooth curve and a dot on it that has two tiny eyes"),
        ("ic_integral", "a bold integral sign with a shaded area under a curve and two dot eyes"),
        ("ic_sigma", "a bold sigma summation symbol with two dot eyes"),
        ("ic_infinity", "a chunky infinity symbol with two dot eyes in the loops"),
        ("ic_matrix", "a 2x2 grid of dots inside tall rounded brackets, two dots are eyes"),
        ("ic_probability", "two rounded dice showing dots, one with x eyes in its pips"),
        ("ic_statistics", "a pie chart with one slice pulled out and two dot eyes"),
        ("ic_abacus", "a small soroban abacus with a few beads moved and two dot eyes on a bead"),
    ]),
    ("03", "English", [
        ("ic_english", "an open book with a small speech bubble above it, the book has two dot eyes"),
        ("ic_grammar", "a pair of chunky quotation marks with two dot eyes"),
        ("ic_vocabulary", "a stack of three word cards with blank lines, top card has two dot eyes"),
        ("ic_pen", "a fat pencil drawing a wavy line, two dot eyes on the barrel"),
        ("ic_dictionary", "a thick closed book with a magnifying glass, the book has two dot eyes"),
        ("ic_listening", "over-ear headphones with two dot eyes on the headband"),
        ("ic_speaking", "a rounded microphone with small sound arcs and two dot eyes"),
        ("ic_conversation", "two overlapping speech bubbles with three dots, each bubble has two dot eyes"),
        ("ic_reading", "a pair of round glasses resting on an open book, two dot eyes in the lenses"),
        ("ic_writing", "a sheet of paper with wavy lines and a feather quill, two dot eyes on the paper"),
        ("ic_spelling", "five blank square tiles in a row with a check mark above, the middle tile has two dot eyes"),
        ("ic_translate", "two speech bubbles joined by curved arrows, two dot eyes on one bubble"),
        ("ic_news", "a folded newspaper with blank headline bars and a picture block, two dot eyes"),
        ("ic_letter", "a sealed envelope with a heart stamp and two dot eyes"),
        ("ic_bookmark", "a thick bookmark ribbon with a star and two dot eyes"),
        ("ic_globe", "a globe on a small stand with a pin, two dot eyes on the globe"),
    ]),
    ("04", "Software engineering", [
        ("ic_swe", "a laptop with angle brackets and a slash on the screen, two dot eyes under the brackets"),
        ("ic_terminal", "a terminal window with a greater-than prompt and a cursor block, the cursor has two dot eyes"),
        ("ic_git", "a git branch: three round nodes joined by a forked line, each node has two dot eyes"),
        ("ic_bug", "a cute round beetle bug with antennae and two big dot eyes"),
        ("ic_database", "a stacked cylinder database with two dot eyes on the top disc"),
        ("ic_server", "a stack of server boxes with tiny lights, two dot eyes on the top box"),
        ("ic_api", "a plug meeting a socket with a small spark, two dot eyes on the plug"),
        ("ic_algorithm", "a small flowchart: a diamond splitting into two boxes, the diamond has two dot eyes"),
        ("ic_function", "a pair of chunky curly braces around two dot eyes"),
        ("ic_test", "a lab flask with a check mark bubble and two dot eyes"),
        ("ic_deploy", "a plump rocket launching with a small flame, two dot eyes in its window"),
        ("ic_security", "a shield with a keyhole that has two dot eyes"),
        ("ic_container", "a stack of three shipping boxes, the top box has two dot eyes"),
        ("ic_cpu", "a square chip with pins on all sides, two dot eyes on the core"),
        ("ic_keyboard", "a keyboard with chunky keys, two keys are dot eyes"),
        ("ic_network", "five round nodes connected in a star pattern, the hub has two dot eyes"),
    ]),
    ("05", "Electronics", [
        ("ic_electronics", "a lightning bolt with two dot eyes and a small spark"),
        ("ic_resistor", "a resistor with colored-band stripes drawn as grey and black bands, two wire legs, two dot eyes"),
        ("ic_capacitor", "an electrolytic capacitor can with a stripe and two legs, two dot eyes"),
        ("ic_inductor", "a coil of wire with five loops and two dot eyes"),
        ("ic_diode", "a diode symbol: triangle pointing into a bar with wires, two dot eyes in the triangle"),
        ("ic_led", "a round-top LED glowing with rays and two dot eyes"),
        ("ic_transistor", "a half-round transistor with three legs and two dot eyes"),
        ("ic_battery", "a chunky battery with a plus end and a lightning bolt, two dot eyes"),
        ("ic_switch", "a toggle switch in the on position with two dot eyes"),
        ("ic_breadboard", "a breadboard with a grid of holes and two jumper wires, two holes are eyes"),
        ("ic_microcontroller", "a small circuit board with a chip and pins, two dot eyes on the chip"),
        ("ic_oscilloscope", "an oscilloscope screen showing a sine wave, two dot eyes under the wave"),
        ("ic_multimeter", "a multimeter with a dial and a tiny screen, two dot eyes on the screen"),
        ("ic_solder", "a soldering iron with a curl of smoke and a drop of solder, two dot eyes"),
        ("ic_transformer", "two coils around an iron core with two dot eyes"),
        ("ic_bulb", "a light bulb with a bright filament and small rays, two dot eyes"),
    ]),
    ("06", "Path nodes and states", [
        ("ic_inprogress", "a round node with a half-filled progress ring and a play triangle, two dot eyes"),
        ("ic_new", "a round badge with a sparkle star and tiny burst lines, two dot eyes"),
        ("ic_perfect", "a star badge with a check mark and sparkles, two happy dot eyes"),
        ("ic_freeze", "an ice crystal snowflake with two dot eyes, a few shiver ticks"),
        ("ic_reminder", "a plump bell with a small ring arc and two dot eyes"),
        ("ic_chest", "a small treasure chest with a heart lock, two dot eyes above the lock"),
        ("ic_unit", "a crown with three points and two dot eyes"),
        ("ic_checkpoint", "a little flag on a hill, the flag has two dot eyes"),
        ("ic_skip", "a double chevron arrow with two dot eyes"),
        ("ic_hint", "a light bulb with a question-mark spark and two dot eyes"),
        ("ic_retry", "a thick circular arrow with two dot eyes in the middle"),
        ("ic_practice", "a dumbbell with a sweat drop and two dot eyes"),
        ("ic_start", "a big round button with a thick play triangle and two dot eyes"),
        ("ic_shield", "a shield with a star and two dot eyes"),
        ("ic_gem", "a faceted gem with white highlight dashes and two dot eyes"),
        ("ic_calendar", "a calendar page with a binding and a check mark on one day, two dot eyes in the header"),
    ]),
    ("07", "Mascot (optional): the Bot", [
        ("mascot_happy", "the Bot standing, smiling with a curved mouth on its screen face"),
        ("mascot_cheer", "the Bot with both arms up cheering"),
        ("mascot_think", "the Bot with one hand on its chin, eyes looking up"),
        ("mascot_sleep", "the Bot asleep with a small sleep bubble and flat eyes"),
        ("mascot_wave", "the Bot waving hello"),
        ("mascot_sad", "the Bot looking down with a tiny tear"),
        ("mascot_wow", "the Bot with wide eyes and an open round mouth"),
        ("mascot_study", "the Bot holding an open book"),
        ("mascot_flame", "the Bot sitting on a small flame"),
        ("mascot_trophy", "the Bot holding a small cup"),
        ("mascot_heart", "the Bot hugging a heart"),
        ("mascot_sweat", "the Bot looking nervous with a sweat drop"),
        ("mascot_idea", "the Bot with a light bulb glowing above its antenna"),
        ("mascot_coffee", "the Bot sipping from a tiny cup"),
        ("mascot_peek", "the Bot peeking from the bottom edge, only head and hands visible"),
        ("mascot_dance", "the Bot mid-dance with small music-note shapes"),
    ]),
]



def prompt(key, idx, title, icons):
    st = STYLES[key]
    lines = [f"{i + 1}. {d}" for i, (_, d) in enumerate(icons)]
    extra = ""
    if idx == "07":
        extra = "\n\n" + (BOT_NOTE if st["mascot"] == "bot" else TOY_MASCOT)
    return (f"STYLE\n{st['style']}\n\nLAYOUT\n{layout(*st['bg'])}\n\nOUTPUT\n{OUTPUT}\n\n"
            f"SHEET {idx}: {title}. Draw exactly these 16 icons in row-major order "
            f"(left to right, top to bottom):\n" + "\n".join(lines) + extra)


def main():
    out = ["# ASSETS", "",
           "Illustrated icons for Dopamine. You generate the sheets; the app ships WebP icons sliced from them.", "",
           "Until a sheet exists, the app uses the monochrome vector placeholders in `res/drawable/` "
           "(marked PLACEHOLDER).", "",
           "## 1. Choose a style (render sheet 01 in two or three, then pick)", ""]
    for k, st in STYLES.items():
        out += [f"- **{st['title']}**: {st['blurb']}",
                f"  - copy: `pbcopy < tools/sheets/sheet01.{k}.prompt.txt`"]
    out += ["",
            "Tell me the letter you pick. Then render sheets 02 to 07 with the same style: "
            "`pbcopy < tools/sheets/sheet0N.<style>.prompt.txt`.", "",
            "## 2. Workflow", "",
            "1. Paste a sheet prompt into the image generator and save as `sheets-src/sheetNN.png` (not committed).",
            "2. Check: 4x4 grid, flat solid background, same size, no text.",
            "3. Slice (any flat background color is detected automatically): "
            "`python3 tools/slice_sheet.py sheets-src/sheet01.png tools/sheets/sheet01.txt app/src/main/res/drawable-nodpi`",
            "4. Names come from `tools/sheets/sheetNN.txt` (row-major). Output: `<name>.webp`, 256 px.",
            "5. Budget: all icons together under 1 MB. The script prints the total.",
            "6. Optional: `rembg` can replace the flood-fill key if an edge is rough.", "",
            "Pipeline: `tools/slice_sheet.py` (Pillow) splits 4x4, removes the flat background by flood fill with "
            "edge cleanup, trims, re-pads to a square, resizes, exports WebP.", "",
            "## 3. Prompts", ""]
    for key, st in STYLES.items():
        sheets = BOTS_SHEETS if st["sheets"] == "bots" else TOY_SHEETS
        out += [f"### Style {st['title']}", ""]
        for idx, title, icons in sheets:
            txt = prompt(key, idx, title, icons)
            (ROOT / "tools" / "sheets" / f"sheet{idx}.{key}.prompt.txt").write_text(txt + "\n")
            (ROOT / "tools" / "sheets" / f"sheet{idx}.txt").write_text("\n".join(n for n, _ in icons) + "\n")
            out += [f"<details><summary>Sheet {idx}: {title}</summary>", "",
                    "Files: " + ", ".join(f"`{n}.webp`" for n, _ in icons), "",
                    "```text", txt, "```", "", "</details>", ""]
    (ROOT / "ASSETS.md").write_text("\n".join(out))
    print("wrote ASSETS.md;", len(STYLES), "styles x 7 sheets")


if __name__ == "__main__":
    main()
