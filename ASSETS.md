# ASSETS

Illustrated icons for Dopamine. You generate the sheets; the app ships WebP icons sliced from them.

Until a sheet exists, the app uses the monochrome vector placeholders in `res/drawable/` (marked PLACEHOLDER). Each sheet below has a complete prompt to paste into the image generator.

## Workflow

1. Paste a sheet prompt, generate, save as `sheets-src/sheetNN.png` (not committed).
2. Check: 4x4 grid, solid #FF00FF background, same size, no text. Regenerate bad cells' sheet if needed.
3. Slice: `python3 tools/slice_sheet.py sheets-src/sheet01.png tools/sheets/sheet01.txt app/src/main/res/drawable-nodpi`
4. Names come from `tools/sheets/sheetNN.txt` (row-major, one per line). Output: `<name>.webp`, 256 px.
5. Budget: all icons together under 1 MB. The script prints the total.
6. Optional: `rembg` can replace the magenta key if the edge is rough: `pip install rembg`.

Pipeline: `tools/slice_sheet.py` (Pillow) splits 4x4, keys out magenta with edge cleanup, trims, re-pads to a square, resizes, exports WebP.

## Shared blocks (already included in each prompt below)

**STYLE** Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

**LAYOUT** Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

**OUTPUT** Highest square resolution available, ideally 3072x3072. PNG.

## Sheets

### Sheet 01: Core UI

Files: `ic_streak.webp`, `ic_xp.webp`, `ic_heart.webp`, `ic_goal.webp`, `ic_complete.webp`, `ic_locked.webp`, `ic_review.webp`, `ic_trophy.webp`, `ic_update.webp`, `ic_offline.webp`, `ic_correct.webp`, `ic_wrong.webp`, `ic_settings.webp`, `ic_stats.webp`, `ic_home.webp`, `ic_path.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 01: Core UI. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a happy flame spirit, pink and yellow flame with a tiny smile
2. a chunky five-point yellow star with a small sparkle
3. a plump pink heart
4. an archery target with an arrow in the bullseye, green and yellow rings
5. a round green badge with a bold white check mark and a small ribbon
6. a rounded padlock, closed, soft green body and yellow shackle
7. a fan of three rounded flash cards, pink, yellow and green
8. a small golden cup with two handles and a ribbon
9. a paper-plane-like cloud with a thick down arrow, green
10. a soft pale cloud with a diagonal slash
11. a green circle with a thick rounded check mark
12. a pink circle with a thick rounded cross
13. a rounded gear shaped like a flower, yellow
14. three rounded bars of rising height with a small star on the tallest, green and yellow
15. a cozy little house with a round door, green roof
16. a winding road with three round stops, pink and green
```

### Sheet 02: Math

Files: `ic_math.webp`, `ic_fraction.webp`, `ic_sqrt.webp`, `ic_pi.webp`, `ic_algebra.webp`, `ic_geometry.webp`, `ic_compass.webp`, `ic_protractor.webp`, `ic_graph.webp`, `ic_integral.webp`, `ic_sigma.webp`, `ic_infinity.webp`, `ic_matrix.webp`, `ic_probability.webp`, `ic_statistics.webp`, `ic_abacus.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 02: Math. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a rounded plus-minus sign inside a soft green square
2. a chunky fraction bar with a dot above and a dot below, yellow
3. a bold square root symbol, green
4. a bold pi symbol on a pink round tile
5. a balance scale with a tiny star on one side and two cubes on the other
6. a set square triangle ruler with a small angle arc, yellow
7. a drawing compass making a circle arc, pink and green
8. a half-circle protractor with a pink pointer
9. a coordinate grid with a smooth green curve and a yellow dot on it
10. a bold integral sign, yellow, with a soft green area under a curve
11. a bold sigma summation symbol, pink
12. a chunky infinity symbol, green with a yellow highlight
13. a 2x2 grid of dots inside tall rounded brackets, yellow
14. two rounded dice showing dots, one pink one green
15. a pie chart with one slice pulled out, green yellow pink
16. a small soroban abacus with a few beads moved, yellow frame
```

### Sheet 03: English

Files: `ic_english.webp`, `ic_grammar.webp`, `ic_vocabulary.webp`, `ic_pen.webp`, `ic_dictionary.webp`, `ic_listening.webp`, `ic_speaking.webp`, `ic_conversation.webp`, `ic_reading.webp`, `ic_writing.webp`, `ic_spelling.webp`, `ic_translate.webp`, `ic_news.webp`, `ic_letter.webp`, `ic_bookmark.webp`, `ic_globe.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 03: English. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. an open book with a small speech bubble above it, green
2. a pair of chunky quotation marks on a yellow round tile
3. a stack of three rounded word cards, pink yellow green, blank lines instead of text
4. a fat pencil writing a wavy line, pink and yellow
5. a closed thick book with a magnifying glass, green
6. round over-ear headphones, pink with yellow cushions
7. a rounded microphone with small sound arcs, green
8. two overlapping speech bubbles, one pink one green, with three dots
9. a pair of round glasses resting on an open book, yellow
10. a sheet of paper with wavy lines and a feather quill, pink
11. five small square tiles in a row with a green check mark above, tiles blank
12. two speech bubbles joined by curved arrows, green and yellow
13. a folded newspaper with blank headline bars and a small picture block, pale yellow
14. a sealed envelope with a pink heart stamp
15. a thick rounded bookmark ribbon, pink with a yellow star
16. a friendly globe on a small stand, green continents on a pale mint ocean, pink pin
```

### Sheet 04: Software engineering

Files: `ic_swe.webp`, `ic_terminal.webp`, `ic_git.webp`, `ic_bug.webp`, `ic_database.webp`, `ic_server.webp`, `ic_api.webp`, `ic_algorithm.webp`, `ic_function.webp`, `ic_test.webp`, `ic_deploy.webp`, `ic_security.webp`, `ic_container.webp`, `ic_cpu.webp`, `ic_keyboard.webp`, `ic_network.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 04: Software engineering. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a rounded laptop with angle brackets and a slash on the screen, green
2. a rounded terminal window with a greater-than prompt and a cursor block, ink and green
3. a git branch: three round nodes joined by a forked line, pink and green
4. a cute rounded beetle bug with antennae, pink
5. a stacked cylinder database, yellow with green bands
6. a rounded cloud with a small server rack inside, green
7. a plug meeting a socket with a small spark, yellow and pink
8. a small flowchart: a diamond splitting into two rounded boxes, green and yellow
9. a pair of chunky curly braces around a dot, pink
10. a rounded lab flask with a green check mark bubble
11. a plump rocket launching with a small flame, pink and yellow
12. a rounded shield with a keyhole, green
13. a stack of three rounded shipping boxes, yellow, pink and green
14. a square chip with pins on all sides and a small star core, green
15. a rounded keyboard with chunky keys, one key pink, one yellow
16. five round nodes connected by lines in a star pattern, green with a pink hub
```

### Sheet 05: Electronics

Files: `ic_electronics.webp`, `ic_resistor.webp`, `ic_capacitor.webp`, `ic_inductor.webp`, `ic_diode.webp`, `ic_led.webp`, `ic_transistor.webp`, `ic_battery.webp`, `ic_switch.webp`, `ic_breadboard.webp`, `ic_microcontroller.webp`, `ic_oscilloscope.webp`, `ic_multimeter.webp`, `ic_solder.webp`, `ic_transformer.webp`, `ic_bulb.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 05: Electronics. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a rounded lightning bolt inside a yellow circle, with a small spark
2. a rounded resistor with colored bands and two wire legs, pink body
3. a rounded electrolytic capacitor can with a stripe, green, two legs
4. a coil of copper wire with five loops, yellow
5. a diode symbol: triangle pointing into a bar, wires on both sides, green
6. a round-top LED glowing with soft rays, pink
7. a small black-half-round transistor with three legs, yellow label dot
8. a chunky battery with a plus end and a lightning bolt, green
9. a rounded toggle switch in the on position, pink and white
10. a rounded breadboard with a grid of holes and two jumper wires, green
11. a small circuit board with a black chip and gold pins, green board
12. a rounded oscilloscope screen showing a sine wave, yellow frame
13. a rounded multimeter with a dial and a tiny screen, pink
14. a soldering iron with a small curl of smoke and a drop of solder, yellow
15. two coils around a rounded iron core, green
16. a round light bulb with a bright filament and small rays, yellow
```

### Sheet 06: Path nodes and states

Files: `ic_inprogress.webp`, `ic_new.webp`, `ic_perfect.webp`, `ic_freeze.webp`, `ic_reminder.webp`, `ic_chest.webp`, `ic_unit.webp`, `ic_checkpoint.webp`, `ic_skip.webp`, `ic_hint.webp`, `ic_retry.webp`, `ic_practice.webp`, `ic_start.webp`, `ic_shield.webp`, `ic_gem.webp`, `ic_calendar.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 06: Path nodes and states. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a round node with a half-filled progress ring and a play triangle, green
2. a round pink badge with a sparkle star and tiny burst lines
3. a golden star badge with a green check and small sparkles
4. a chunky ice crystal snowflake in a rounded square, pale green tint
5. a plump bell with a small ring arc, yellow
6. a small treasure chest with a heart lock, pink and yellow
7. a rounded crown with three star points, yellow
8. a little flag on a hill, pink flag
9. a double chevron arrow in a rounded square, green
10. a small light bulb with a question mark spark, yellow
11. a thick circular arrow, pink
12. a rounded dumbbell with a small sweat drop, green
13. a big round start button with a thick play triangle, green
14. a rounded shield with a star, pink
15. a faceted rounded gem, green with a yellow highlight
16. a rounded calendar page with a binding and a green check on one day, blank grid
```

### Sheet 07: Mascot (optional)

Files: `mascot_happy.webp`, `mascot_cheer.webp`, `mascot_think.webp`, `mascot_sleep.webp`, `mascot_wave.webp`, `mascot_sad.webp`, `mascot_wow.webp`, `mascot_study.webp`, `mascot_flame.webp`, `mascot_trophy.webp`, `mascot_heart.webp`, `mascot_sweat.webp`, `mascot_idea.webp`, `mascot_coffee.webp`, `mascot_peek.webp`, `mascot_dance.webp`

```text
STYLE
Modern 2026 mobile-app icon illustration. Chunky rounded shapes, thick soft dark-green ink outline (#14201A), flat fills with one subtle top-left highlight and a soft bottom shade for a gentle toy-like volume. NOT glossy, NOT photorealistic, NO neon, NO glassmorphism, NO full-icon gradients. Palette: fresh green #35C46F, sunny yellow #FFC93C, bubblegum pink #FF7FAE, white, ink #14201A, plus pale tints of those three (#E4F8EC, #FFF3CC, #FFE4EE). One light source from the top left. Friendly, playful, clean. No text, no letters, no numbers. Math and code symbols are allowed only where the icon list names them.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid #FF00FF background across the whole image. No grid lines, no labels, no captions, no shadows crossing cells. Never use magenta or purple inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 07: Mascot (optional). Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. the mascot smiling with eyes closed
2. the mascot with both arms up cheering
3. the mascot with one hand on chin, eyes looking up
4. the mascot asleep with a small sleep bubble
5. the mascot waving hello
6. the mascot looking down, a small tear
7. the mascot with wide eyes and open mouth
8. the mascot holding an open book
9. the mascot sitting on a small pink flame
10. the mascot holding a small golden cup
11. the mascot hugging a pink heart
12. the mascot looking nervous with a sweat drop
13. the mascot with a glowing lightbulb above
14. the mascot sipping from a tiny cup
15. the mascot peeking from the bottom edge, only head and hands
16. the mascot mid-dance with small music notes as shapes

Mascot sheet: first generate ONE character reference (a round, soft green blob-like study buddy with two big eyes, tiny pink cheeks, a small yellow leaf tuft on top, no text), then reuse that exact character in every cell. Consistency across 16 cells is hard; if it drifts, generate 4 cells at a time and merge them, or skip this sheet.
```
