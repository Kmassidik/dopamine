# DESIGN

Source of truth is the code: `ui/theme/Palette.kt` (raw tokens), `Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Motion.kt`. This file explains the intent. The owner chose **white background with green, yellow and pink accents, chunky and playful** (replacing the original Japanese washi/ink brief, see `DECISIONS.md`). Reference look: bento tiles, big greeting, curved text ribbon, pill list rows, black primary pill.

## Color

| Token | Light | Use |
|---|---|---|
| bg / surface | `#FFFFFF` | backgrounds |
| ink | `#14201A` | text, dark tile, primary pill |
| inkSoft | `#4F5D55` | secondary text (6.9:1 on white) |
| line | `#E4EAE6` | thin outlines |
| green / greenText / greenTint / greenLip | `#35C46F` / `#17803F` / `#E4F8EC` / `#22994F` | success, primary tile, Math |
| yellow / yellowText / yellowTint / yellowLip | `#FFC93C` / `#8A5F00` / `#FFF3CC` / `#D9A21B` | rewards, Review tile |
| pink / pinkText / pinkTint / pinkLip | `#FF7FAE` / `#B8305F` / `#FFE4EE` / `#DB5A8A` | highlights, errors, avatar |

Rules: **text on any accent fill is `onAccent` (ink)**; accent fills are never used for body text (use the `*Text` tones). Correct/wrong must never rely on color alone (add icon and text). Dark tokens exist (`N_*`, deep green-black `#0E1411`) but the app is currently forced light. Every foreground/background pair used for text is asserted at 4.5:1 in `ContrastTest`; add new pairs there.

Category tints rotate by `category.order % 3`: 1 green, 2 yellow, 0 pink.

## Type

Zen Maru Gothic (OFL), Latin subset, Regular / Medium / Bold only (about 22 KB each). Scale (sp): display 32/40 bold, title 22/30 bold, subtitle 18/26 medium, small title 16/24 medium, body 16/24, body-small 14/22, label 16/22 bold, label-medium 14/20, caption 12/16. All sizes use `sp` so they follow the system font scale; never fix the height of a text container.

## Shape, spacing

Radii: 8 / 12 / 20 (buttons) / 28 (cards) / pill. Outline 2 dp in `line`. 8 dp grid: 4, 8, 16, 24, 32; screen gutter 16; minimum touch target 48 dp (primary buttons 56 dp).

## Motion

Subtle and cheap. Tokens in `Motion.kt` (120 / 240 / 400 ms, soft ease). Implemented: icons bob (about 1.1 s, 3 dp, 1.5 degree tilt, staggered), cards squash to 0.95 on press with a spring. All of it checks `LocalReducedMotion` (system animator scale 0) and falls back to nothing. No full-screen blur, no particle systems. Planned: gentle spring on correct, soft shake on wrong, confetti only on streak milestones.

## Components (`ui/components`)

`IconImage` (illustrated icon with optional bob), `bounceClick` (press squash), `ChunkyButton` (green action with a darker lip, supports `enabled`), `InkButton` (flat black pill), `StatChip`, `ProgressBar`, `ProgressRing`, `RibbonCorner` (curved band with text on a path, decorative), `NameField`, `Message` (success/problem banner), `AppIcon` (tinted vector glyph for functional icons only: back, chevron, check, warning, download).

## Icons

Colorful "Bots" mini illustrations: thick black outline, dot-eye faces, ground shadow, spark ticks, green/yellow/pink fills. 7 sheets x 16 (Core UI, Math, English, SWE, Electronics, Path states, Bot mascot). Prompts in `ASSETS.md`, pipeline in `tools/slice_sheet.py` (192 px WebP, mascot 256 px). The Bot is also the launcher icon on a yellow tile. Functional UI glyphs stay vectors.

## Do / Don't

Do: large rounded shapes, one idea per tile, short calm copy, dark text on bright fills, scroll anything that can overflow, bottom-weighted primary actions.
Don't: default Material purple, blue-to-purple gradients, glassmorphism, gradient hero cards, emoji as icons, stock illustration packs, lorem ipsum, white text on green/yellow/pink fills.
