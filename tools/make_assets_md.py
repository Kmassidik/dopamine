#!/usr/bin/env python3
"""Single source for icon sheets. Writes ASSETS.md and tools/sheets/sheetNN.txt (names, row-major)."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

STYLE = (
    "Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline "
    "(#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like "
    "volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. "
    "Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus "
    "pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, "
    "playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon "
    "list names them."
)
LAYOUT = (
    "Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, "
    "about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background "
    "across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use "
    "magenta or purple inside an icon."
)
OUTPUT = "Highest square resolution available, ideally 3072x3072. PNG."

SHEETS = [
    ("01", "Core UI", [
        ("ic_streak", "a happy flame spirit, pink and yellow flame with a tiny smile"),
        ("ic_xp", "a chunky five-point yellow star with a small sparkle"),
        ("ic_heart", "a plump pink heart"),
        ("ic_goal", "an archery target with an arrow in the bullseye, green and yellow rings"),
        ("ic_complete", "a round green badge with a bold white check mark and a small ribbon"),
        ("ic_locked", "a rounded padlock, closed, soft green body and yellow shackle"),
        ("ic_review", "a fan of three rounded flash cards, pink, yellow and green"),
        ("ic_trophy", "a small golden cup with two handles and a ribbon"),
        ("ic_update", "a paper-plane-like cloud with a thick down arrow, green"),
        ("ic_offline", "a soft pale cloud with a diagonal slash"),
        ("ic_correct", "a green circle with a thick rounded check mark"),
        ("ic_wrong", "a pink circle with a thick rounded cross"),
        ("ic_settings", "a rounded gear shaped like a flower, yellow"),
        ("ic_stats", "three rounded bars of rising height with a small star on the tallest, green and yellow"),
        ("ic_home", "a cozy little house with a round door, green roof"),
        ("ic_path", "a winding road with three round stops, pink and green"),
    ]),
    ("02", "Math", [
        ("ic_math", "a rounded plus-minus sign inside a soft green square"),
        ("ic_fraction", "a chunky fraction bar with a dot above and a dot below, yellow"),
        ("ic_sqrt", "a bold square root symbol, green"),
        ("ic_pi", "a bold pi symbol on a pink round tile"),
        ("ic_algebra", "a balance scale with a tiny star on one side and two cubes on the other"),
        ("ic_geometry", "a set square triangle ruler with a small angle arc, yellow"),
        ("ic_compass", "a drawing compass making a circle arc, pink and green"),
        ("ic_protractor", "a half-circle protractor with a pink pointer"),
        ("ic_graph", "a coordinate grid with a smooth green curve and a yellow dot on it"),
        ("ic_integral", "a bold integral sign, yellow, with a soft green area under a curve"),
        ("ic_sigma", "a bold sigma summation symbol, pink"),
        ("ic_infinity", "a chunky infinity symbol, green with a yellow highlight"),
        ("ic_matrix", "a 2x2 grid of dots inside tall rounded brackets, yellow"),
        ("ic_probability", "two rounded dice showing dots, one pink one green"),
        ("ic_statistics", "a pie chart with one slice pulled out, green yellow pink"),
        ("ic_abacus", "a small soroban abacus with a few beads moved, yellow frame"),
    ]),
    ("03", "English", [
        ("ic_english", "an open book with a small speech bubble above it, green"),
        ("ic_grammar", "a pair of chunky quotation marks on a yellow round tile"),
        ("ic_vocabulary", "a stack of three rounded word cards, pink yellow green, blank lines instead of text"),
        ("ic_pen", "a fat pencil writing a wavy line, pink and yellow"),
        ("ic_dictionary", "a closed thick book with a magnifying glass, green"),
        ("ic_listening", "round over-ear headphones, pink with yellow cushions"),
        ("ic_speaking", "a rounded microphone with small sound arcs, green"),
        ("ic_conversation", "two overlapping speech bubbles, one pink one green, with three dots"),
        ("ic_reading", "a pair of round glasses resting on an open book, yellow"),
        ("ic_writing", "a sheet of paper with wavy lines and a feather quill, pink"),
        ("ic_spelling", "five small square tiles in a row with a green check mark above, tiles blank"),
        ("ic_translate", "two speech bubbles joined by curved arrows, green and yellow"),
        ("ic_news", "a folded newspaper with blank headline bars and a small picture block, pale yellow"),
        ("ic_letter", "a sealed envelope with a pink heart stamp"),
        ("ic_bookmark", "a thick rounded bookmark ribbon, pink with a yellow star"),
        ("ic_globe", "a friendly globe on a small stand, green continents on a pale mint ocean, pink pin"),
    ]),
    ("04", "Software engineering", [
        ("ic_swe", "a rounded laptop with angle brackets and a slash on the screen, green"),
        ("ic_terminal", "a rounded terminal window with a greater-than prompt and a cursor block, ink and green"),
        ("ic_git", "a git branch: three round nodes joined by a forked line, pink and green"),
        ("ic_bug", "a cute rounded beetle bug with antennae, pink"),
        ("ic_database", "a stacked cylinder database, yellow with green bands"),
        ("ic_server", "a rounded cloud with a small server rack inside, green"),
        ("ic_api", "a plug meeting a socket with a small spark, yellow and pink"),
        ("ic_algorithm", "a small flowchart: a diamond splitting into two rounded boxes, green and yellow"),
        ("ic_function", "a pair of chunky curly braces around a dot, pink"),
        ("ic_test", "a rounded lab flask with a green check mark bubble"),
        ("ic_deploy", "a plump rocket launching with a small flame, pink and yellow"),
        ("ic_security", "a rounded shield with a keyhole, green"),
        ("ic_container", "a stack of three rounded shipping boxes, yellow, pink and green"),
        ("ic_cpu", "a square chip with pins on all sides and a small star core, green"),
        ("ic_keyboard", "a rounded keyboard with chunky keys, one key pink, one yellow"),
        ("ic_network", "five round nodes connected by lines in a star pattern, green with a pink hub"),
    ]),
    ("05", "Electronics", [
        ("ic_electronics", "a rounded lightning bolt inside a yellow circle, with a small spark"),
        ("ic_resistor", "a rounded resistor with colored bands and two wire legs, pink body"),
        ("ic_capacitor", "a rounded electrolytic capacitor can with a stripe, green, two legs"),
        ("ic_inductor", "a coil of copper wire with five loops, yellow"),
        ("ic_diode", "a diode symbol: triangle pointing into a bar, wires on both sides, green"),
        ("ic_led", "a round-top LED glowing with soft rays, pink"),
        ("ic_transistor", "a small black-half-round transistor with three legs, yellow label dot"),
        ("ic_battery", "a chunky battery with a plus end and a lightning bolt, green"),
        ("ic_switch", "a rounded toggle switch in the on position, pink and white"),
        ("ic_breadboard", "a rounded breadboard with a grid of holes and two jumper wires, green"),
        ("ic_microcontroller", "a small circuit board with a black chip and gold pins, green board"),
        ("ic_oscilloscope", "a rounded oscilloscope screen showing a sine wave, yellow frame"),
        ("ic_multimeter", "a rounded multimeter with a dial and a tiny screen, pink"),
        ("ic_solder", "a soldering iron with a small curl of smoke and a drop of solder, yellow"),
        ("ic_transformer", "two coils around a rounded iron core, green"),
        ("ic_bulb", "a round light bulb with a bright filament and small rays, yellow"),
    ]),
    ("06", "Path nodes and states", [
        ("ic_inprogress", "a round node with a half-filled progress ring and a play triangle, green"),
        ("ic_new", "a round pink badge with a sparkle star and tiny burst lines"),
        ("ic_perfect", "a golden star badge with a green check and small sparkles"),
        ("ic_freeze", "a chunky ice crystal snowflake in a rounded square, pale green tint"),
        ("ic_reminder", "a plump bell with a small ring arc, yellow"),
        ("ic_chest", "a small treasure chest with a heart lock, pink and yellow"),
        ("ic_unit", "a rounded crown with three star points, yellow"),
        ("ic_checkpoint", "a little flag on a hill, pink flag"),
        ("ic_skip", "a double chevron arrow in a rounded square, green"),
        ("ic_hint", "a small light bulb with a question mark spark, yellow"),
        ("ic_retry", "a thick circular arrow, pink"),
        ("ic_practice", "a rounded dumbbell with a small sweat drop, green"),
        ("ic_start", "a big round start button with a thick play triangle, green"),
        ("ic_shield", "a rounded shield with a star, pink"),
        ("ic_gem", "a faceted rounded gem, green with a yellow highlight"),
        ("ic_calendar", "a rounded calendar page with a binding and a green check on one day, blank grid"),
    ]),
    ("07", "Mascot (optional)", [
        ("mascot_happy", "the mascot smiling with eyes closed"),
        ("mascot_cheer", "the mascot with both arms up cheering"),
        ("mascot_think", "the mascot with one hand on chin, eyes looking up"),
        ("mascot_sleep", "the mascot asleep with a small sleep bubble"),
        ("mascot_wave", "the mascot waving hello"),
        ("mascot_sad", "the mascot looking down, a small tear"),
        ("mascot_wow", "the mascot with wide eyes and open mouth"),
        ("mascot_study", "the mascot holding an open book"),
        ("mascot_flame", "the mascot sitting on a small pink flame"),
        ("mascot_trophy", "the mascot holding a small golden cup"),
        ("mascot_heart", "the mascot hugging a pink heart"),
        ("mascot_sweat", "the mascot looking nervous with a sweat drop"),
        ("mascot_idea", "the mascot with a glowing lightbulb above"),
        ("mascot_coffee", "the mascot sipping from a tiny cup"),
        ("mascot_peek", "the mascot peeking from the bottom edge, only head and hands"),
        ("mascot_dance", "the mascot mid-dance with small music notes as shapes"),
    ]),
]

MASCOT_NOTE = (
    "Mascot sheet: first generate ONE character reference (a round, soft green blob-like study buddy with "
    "two big eyes, tiny pink cheeks, a small yellow leaf tuft on top, no text), then reuse that exact "
    "character in every cell. Consistency across 16 cells is hard; if it drifts, generate 4 cells at a "
    "time and merge them, or skip this sheet."
)


def prompt(idx, title, icons):
    lines = [f"{i + 1}. {d}" for i, (_, d) in enumerate(icons)]
    extra = f"\n\n{MASCOT_NOTE}" if idx == "07" else ""
    return (f"STYLE\n{STYLE}\n\nLAYOUT\n{LAYOUT}\n\nOUTPUT\n{OUTPUT}\n\n"
            f"SHEET {idx}: {title}. Draw exactly these 16 icons in row-major order "
            f"(left to right, top to bottom):\n" + "\n".join(lines) + extra)


def main():
    out = ["# ASSETS", "",
           "Illustrated icons for Dopamine. You generate the sheets; the app ships WebP icons sliced from them.",
           "",
           "Until a sheet exists, the app uses the monochrome vector placeholders in `res/drawable/` "
           "(marked PLACEHOLDER). Each sheet below has a complete prompt to paste into the image generator.",
           "",
           "## Workflow", "",
           "1. Paste a sheet prompt, generate, save as `sheets-src/sheetNN.png` (not committed).",
           "2. Check: 4x4 grid, solid #FF00FF background, same size, no text. Regenerate bad cells' sheet if needed.",
           "3. Slice: `python3 tools/slice_sheet.py sheets-src/sheet01.png tools/sheets/sheet01.txt app/src/main/res/drawable-nodpi`",
           "4. Names come from `tools/sheets/sheetNN.txt` (row-major, one per line). Output: `<name>.webp`, 256 px.",
           "5. Budget: all icons together under 1 MB. The script prints the total.",
           "6. Optional: `rembg` can replace the magenta key if the edge is rough: `pip install rembg`.",
           "",
           "Pipeline: `tools/slice_sheet.py` (Pillow) splits 4x4, keys out magenta with edge cleanup, trims, "
           "re-pads to a square, resizes, exports WebP.",
           "",
           "## Shared blocks (already included in each prompt below)", "",
           "**STYLE** " + STYLE, "", "**LAYOUT** " + LAYOUT, "", "**OUTPUT** " + OUTPUT, "",
           "## Sheets", ""]
    for idx, title, icons in SHEETS:
        out += [f"### Sheet {idx}: {title}", "", "Files: " + ", ".join(f"`{n}.webp`" for n, _ in icons), "",
                "```text", prompt(idx, title, icons), "```", ""]
        (ROOT / "tools" / "sheets" / f"sheet{idx}.txt").write_text("\n".join(n for n, _ in icons) + "\n")
    (ROOT / "ASSETS.md").write_text("\n".join(out))
    print("wrote ASSETS.md and", len(SHEETS), "name lists;", sum(len(s[2]) for s in SHEETS), "icons")


if __name__ == "__main__":
    main()
