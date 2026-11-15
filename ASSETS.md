# ASSETS

Illustrated icons for Dopamine. You generate the sheets; the app ships WebP icons sliced from them.

Until a sheet exists, the app uses the monochrome vector placeholders in `res/drawable/` (marked PLACEHOLDER). Each sheet below has a complete prompt to paste into the image generator.

## Workflow

1. Paste a sheet prompt, generate, save as `sheets-src/sheetNN.png` (not committed).
2. Check: 4x4 grid, flat solid yellow background, same size, no text. Render sheet 01 first as a style test.
3. Slice (the flat background color is detected automatically, any flat color works): `python3 tools/slice_sheet.py sheets-src/sheet01.png tools/sheets/sheet01.txt app/src/main/res/drawable-nodpi`
4. Names come from `tools/sheets/sheetNN.txt` (row-major, one per line). Output: `<name>.webp`, 256 px.
5. Budget: all icons together under 1 MB. The script prints the total.
6. Optional: `rembg` can replace the flood-fill key if an edge is rough: `pip install rembg`.

Pipeline: `tools/slice_sheet.py` (Pillow) splits 4x4, removes the flat background by flood fill with edge cleanup, trims, re-pads to a square, resizes, exports WebP.

## Shared blocks (already included in each prompt below)

**STYLE** Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

**LAYOUT** Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

**OUTPUT** Highest square resolution available, ideally 3072x3072. PNG.

## Sheets

### Sheet 01: Core UI

Files: `ic_streak.webp`, `ic_xp.webp`, `ic_heart.webp`, `ic_goal.webp`, `ic_complete.webp`, `ic_locked.webp`, `ic_review.webp`, `ic_trophy.webp`, `ic_update.webp`, `ic_offline.webp`, `ic_correct.webp`, `ic_wrong.webp`, `ic_settings.webp`, `ic_stats.webp`, `ic_home.webp`, `ic_path.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 01: Core UI. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a small flame with two dot eyes and a tiny happy mouth, the tip wobbling
2. a chunky five-point star with two dot eyes and a spark
3. a plump heart with two dot eyes and a white highlight
4. an archery target with an arrow in the bullseye, the bullseye has two dot eyes
5. a round badge with a bold check mark and a small ribbon tail, two dot eyes above the check
6. a closed padlock with two dot eyes on its body
7. a fan of three flash cards, the front card has two dot eyes
8. a small cup with two handles and two dot eyes
9. a cloud with a thick down arrow, two dot eyes on the cloud
10. a cloud with a diagonal slash and small x eyes
11. a circle with a thick check mark and two happy dot eyes
12. a circle with a thick cross and two droopy dot eyes
13. a gear with two dot eyes in its center
14. three rounded bars of rising height, the tallest has two dot eyes and a tiny star
15. a little house with a round door and two dot-eye windows
16. a winding road with three round stops and a tiny pin at the end
```

### Sheet 02: Math

Files: `ic_math.webp`, `ic_fraction.webp`, `ic_sqrt.webp`, `ic_pi.webp`, `ic_algebra.webp`, `ic_geometry.webp`, `ic_compass.webp`, `ic_protractor.webp`, `ic_graph.webp`, `ic_integral.webp`, `ic_sigma.webp`, `ic_infinity.webp`, `ic_matrix.webp`, `ic_probability.webp`, `ic_statistics.webp`, `ic_abacus.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 02: Math. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a chunky plus-minus sign with two dot eyes
2. a fraction bar with a big dot above and a big dot below that act as eyes
3. a bold square root symbol with two dot eyes under the roof
4. a bold pi symbol with two dot eyes on the top bar
5. a balance scale, a star on one pan and two cubes on the other, two dot eyes on the pivot
6. a set-square triangle ruler with a small angle arc and two dot eyes
7. a drawing compass drawing a circle arc, two dot eyes on the hinge
8. a half-circle protractor with a pointer and two dot eyes
9. a coordinate grid with a smooth curve and a dot on it that has two tiny eyes
10. a bold integral sign with a shaded area under a curve and two dot eyes
11. a bold sigma summation symbol with two dot eyes
12. a chunky infinity symbol with two dot eyes in the loops
13. a 2x2 grid of dots inside tall rounded brackets, two dots are eyes
14. two rounded dice showing dots, one with x eyes in its pips
15. a pie chart with one slice pulled out and two dot eyes
16. a small soroban abacus with a few beads moved and two dot eyes on a bead
```

### Sheet 03: English

Files: `ic_english.webp`, `ic_grammar.webp`, `ic_vocabulary.webp`, `ic_pen.webp`, `ic_dictionary.webp`, `ic_listening.webp`, `ic_speaking.webp`, `ic_conversation.webp`, `ic_reading.webp`, `ic_writing.webp`, `ic_spelling.webp`, `ic_translate.webp`, `ic_news.webp`, `ic_letter.webp`, `ic_bookmark.webp`, `ic_globe.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 03: English. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. an open book with a small speech bubble above it, the book has two dot eyes
2. a pair of chunky quotation marks with two dot eyes
3. a stack of three word cards with blank lines, top card has two dot eyes
4. a fat pencil drawing a wavy line, two dot eyes on the barrel
5. a thick closed book with a magnifying glass, the book has two dot eyes
6. over-ear headphones with two dot eyes on the headband
7. a rounded microphone with small sound arcs and two dot eyes
8. two overlapping speech bubbles with three dots, each bubble has two dot eyes
9. a pair of round glasses resting on an open book, two dot eyes in the lenses
10. a sheet of paper with wavy lines and a feather quill, two dot eyes on the paper
11. five blank square tiles in a row with a check mark above, the middle tile has two dot eyes
12. two speech bubbles joined by curved arrows, two dot eyes on one bubble
13. a folded newspaper with blank headline bars and a picture block, two dot eyes
14. a sealed envelope with a heart stamp and two dot eyes
15. a thick bookmark ribbon with a star and two dot eyes
16. a globe on a small stand with a pin, two dot eyes on the globe
```

### Sheet 04: Software engineering

Files: `ic_swe.webp`, `ic_terminal.webp`, `ic_git.webp`, `ic_bug.webp`, `ic_database.webp`, `ic_server.webp`, `ic_api.webp`, `ic_algorithm.webp`, `ic_function.webp`, `ic_test.webp`, `ic_deploy.webp`, `ic_security.webp`, `ic_container.webp`, `ic_cpu.webp`, `ic_keyboard.webp`, `ic_network.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 04: Software engineering. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a laptop with angle brackets and a slash on the screen, two dot eyes under the brackets
2. a terminal window with a greater-than prompt and a cursor block, the cursor has two dot eyes
3. a git branch: three round nodes joined by a forked line, each node has two dot eyes
4. a cute round beetle bug with antennae and two big dot eyes
5. a stacked cylinder database with two dot eyes on the top disc
6. a stack of server boxes with tiny lights, two dot eyes on the top box
7. a plug meeting a socket with a small spark, two dot eyes on the plug
8. a small flowchart: a diamond splitting into two boxes, the diamond has two dot eyes
9. a pair of chunky curly braces around two dot eyes
10. a lab flask with a check mark bubble and two dot eyes
11. a plump rocket launching with a small flame, two dot eyes in its window
12. a shield with a keyhole that has two dot eyes
13. a stack of three shipping boxes, the top box has two dot eyes
14. a square chip with pins on all sides, two dot eyes on the core
15. a keyboard with chunky keys, two keys are dot eyes
16. five round nodes connected in a star pattern, the hub has two dot eyes
```

### Sheet 05: Electronics

Files: `ic_electronics.webp`, `ic_resistor.webp`, `ic_capacitor.webp`, `ic_inductor.webp`, `ic_diode.webp`, `ic_led.webp`, `ic_transistor.webp`, `ic_battery.webp`, `ic_switch.webp`, `ic_breadboard.webp`, `ic_microcontroller.webp`, `ic_oscilloscope.webp`, `ic_multimeter.webp`, `ic_solder.webp`, `ic_transformer.webp`, `ic_bulb.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 05: Electronics. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a lightning bolt with two dot eyes and a small spark
2. a resistor with colored-band stripes drawn as grey and black bands, two wire legs, two dot eyes
3. an electrolytic capacitor can with a stripe and two legs, two dot eyes
4. a coil of wire with five loops and two dot eyes
5. a diode symbol: triangle pointing into a bar with wires, two dot eyes in the triangle
6. a round-top LED glowing with rays and two dot eyes
7. a half-round transistor with three legs and two dot eyes
8. a chunky battery with a plus end and a lightning bolt, two dot eyes
9. a toggle switch in the on position with two dot eyes
10. a breadboard with a grid of holes and two jumper wires, two holes are eyes
11. a small circuit board with a chip and pins, two dot eyes on the chip
12. an oscilloscope screen showing a sine wave, two dot eyes under the wave
13. a multimeter with a dial and a tiny screen, two dot eyes on the screen
14. a soldering iron with a curl of smoke and a drop of solder, two dot eyes
15. two coils around an iron core with two dot eyes
16. a light bulb with a bright filament and small rays, two dot eyes
```

### Sheet 06: Path nodes and states

Files: `ic_inprogress.webp`, `ic_new.webp`, `ic_perfect.webp`, `ic_freeze.webp`, `ic_reminder.webp`, `ic_chest.webp`, `ic_unit.webp`, `ic_checkpoint.webp`, `ic_skip.webp`, `ic_hint.webp`, `ic_retry.webp`, `ic_practice.webp`, `ic_start.webp`, `ic_shield.webp`, `ic_gem.webp`, `ic_calendar.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 06: Path nodes and states. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. a round node with a half-filled progress ring and a play triangle, two dot eyes
2. a round badge with a sparkle star and tiny burst lines, two dot eyes
3. a star badge with a check mark and sparkles, two happy dot eyes
4. an ice crystal snowflake with two dot eyes, a few shiver ticks
5. a plump bell with a small ring arc and two dot eyes
6. a small treasure chest with a heart lock, two dot eyes above the lock
7. a crown with three points and two dot eyes
8. a little flag on a hill, the flag has two dot eyes
9. a double chevron arrow with two dot eyes
10. a light bulb with a question-mark spark and two dot eyes
11. a thick circular arrow with two dot eyes in the middle
12. a dumbbell with a sweat drop and two dot eyes
13. a big round button with a thick play triangle and two dot eyes
14. a shield with a star and two dot eyes
15. a faceted gem with white highlight dashes and two dot eyes
16. a calendar page with a binding and a check mark on one day, two dot eyes in the header
```

### Sheet 07: Mascot (optional): the Bot

Files: `mascot_happy.webp`, `mascot_cheer.webp`, `mascot_think.webp`, `mascot_sleep.webp`, `mascot_wave.webp`, `mascot_sad.webp`, `mascot_wow.webp`, `mascot_study.webp`, `mascot_flame.webp`, `mascot_trophy.webp`, `mascot_heart.webp`, `mascot_sweat.webp`, `mascot_idea.webp`, `mascot_coffee.webp`, `mascot_peek.webp`, `mascot_dance.webp`

```text
STYLE
Monochrome mini illustration icon set in the style of a cute 'Bots' sticker pack. Thick uniform black outline with rounded joins and a slightly hand-drawn wobble. Only pure black, white and two flat mid-greys: NO color at all inside the icons. Simple cel shading: a few white highlight dashes, one or two flat grey shade planes, solid black accents (bases, buttons, small parts). Every object is a tiny living character: it has a face made of two small black dot eyes (some use tiny x eyes or a small curved mouth), no nose. Under each object a solid black oval ground shadow (omit only if the object clearly floats). Two to four short spark or motion tick lines near it. Chunky, friendly, playful, slightly imperfect. No gradients, no 3D render, no photorealism, no glow. No text, no letters, no numbers.

LAYOUT
Square image with exactly 4 columns and 4 rows of equal cells (16 cells). One icon per cell, centered, about 12% padding, all icons the same visual size, each a single object. Flat solid warm yellow #F9D56B background across the whole image. No grid lines, no labels, no captions, no cell borders, no shadows crossing cells. Never use yellow inside an icon.

OUTPUT
Highest square resolution available, ideally 3072x3072. PNG.

SHEET 07: Mascot (optional): the Bot. Draw exactly these 16 icons in row-major order (left to right, top to bottom):
1. the Bot standing, smiling with a curved mouth on its screen face
2. the Bot with both arms up cheering
3. the Bot with one hand on its chin, eyes looking up
4. the Bot asleep with a small sleep bubble and flat eyes
5. the Bot waving hello
6. the Bot looking down with a tiny tear
7. the Bot with wide eyes and an open round mouth
8. the Bot holding an open book
9. the Bot sitting on a small flame
10. the Bot holding a small cup
11. the Bot hugging a heart
12. the Bot looking nervous with a sweat drop
13. the Bot with a light bulb glowing above its antenna
14. the Bot sipping from a tiny cup
15. the Bot peeking from the bottom edge, only head and hands visible
16. the Bot mid-dance with small music-note shapes

The Bot: a small friendly robot with a rounded body, a screen face showing two dot eyes, a short antenna, round ear discs, stubby arms and legs, same monochrome style. First generate ONE character reference, then reuse that exact robot in every cell. Consistency across 16 cells is hard; if it drifts, generate 4 cells at a time and merge them, or skip this sheet.
```
