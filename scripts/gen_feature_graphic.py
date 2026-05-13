#!/usr/bin/env python3
"""Generate the Play Store feature graphic (1024x500) for Word Bopper."""

from PIL import Image, ImageDraw, ImageFont
import os

W, H = 1024, 500

# ── Palette (from Color.kt) ───────────────────────────────────────────────────
BG          = (15,  14,  23)
LOGO_CYAN   = (90, 220, 240)
HOT_PINK    = (229, 47, 112)
MINT        = (114, 209, 143)
ORANGE      = (255, 137,   6)
WHITE       = (255, 255, 254)
DARK_TEXT   = ( 20,  18,  30)

BUBBLE_FILLS = [
    (255, 137,   6),   # 0 orange
    (255, 159,  31),   # 1 amber
    ( 61, 169, 252),   # 2 sky blue
    (114, 209, 143),   # 3 mint
    (184, 192, 255),   # 4 lavender
    (255, 209, 102),   # 5 gold
    (239, 133, 156),   # 6 pink
    (143, 240, 199),   # 7 seafoam
]

# ── Fonts ─────────────────────────────────────────────────────────────────────
SF = "/System/Library/Fonts/SFNSRounded.ttf"
f_title  = ImageFont.truetype(SF, 76)
f_tag    = ImageFont.truetype(SF, 34)
f_bubble = ImageFont.truetype(SF, 44)
f_logo   = ImageFont.truetype(SF, 96)

# ── Canvas ─────────────────────────────────────────────────────────────────────
img  = Image.new("RGBA", (W, H), (*BG, 255))
draw = ImageDraw.Draw(img)

# ── Background glow (right area only — behind the bubble grid) ────────────────
glow_cx, glow_cy = 760, 255
for i in range(22, 0, -1):
    r = 280 * i / 22
    a = int(30 * (1 - i / 22))
    draw.ellipse([glow_cx-r, glow_cy-r, glow_cx+r, glow_cy+r],
                 fill=(55, 42, 100, a))

# ── Helper: draw a bubble ─────────────────────────────────────────────────────
def bubble(cx, cy, r, fill, letter=None, font=None, tc=DARK_TEXT):
    draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=(*fill, 255))
    if letter and font:
        bb = font.getbbox(letter)
        tw, th = bb[2]-bb[0], bb[3]-bb[1]
        draw.text((cx - tw//2 - bb[0], cy - th//2 - bb[1]),
                  letter, fill=tc, font=font)

# ── App logo (left strip, vertically centered) ────────────────────────────────
LX, LY, LR = 92, 234, 90
bubble(LX, LY, LR, LOGO_CYAN, "W", f_logo, DARK_TEXT)
bubble(LX - 52, LY - 72, 28, HOT_PINK)   # satellite — hot pink top-left
bubble(LX + 62, LY + 62, 22, MINT)        # satellite — mint bottom-right

# ── Title + tagline (right of logo, left of grid) ────────────────────────────
TX = 207   # left edge of text block

# measure line heights
bb_w = f_title.getbbox("Word")
bb_b = f_title.getbbox("Bopper")
bb_t = f_tag.getbbox("Bop to the Top!")

line_h_title = bb_w[3] - bb_w[1]   # ~72
gap_lines    = 8

y_word   = 142
y_bopper = y_word + line_h_title + gap_lines
y_tag    = y_bopper + (bb_b[3] - bb_b[1]) + 22

draw.text((TX, y_word),   "Word",            fill=WHITE,  font=f_title)
draw.text((TX, y_bopper), "Bopper",          fill=ORANGE, font=f_title)
draw.text((TX, y_tag),    "Bop to the Top!", fill=MINT,   font=f_tag)

# ── Bubble grid (right ~55%) ──────────────────────────────────────────────────
# 5 cols × 4 rows; row 0 spells B-O-P prominently
GRID = [
    ("B", 0), ("O", 2), ("P", 5), ("W", 3), ("Z", 4),
    ("O", 1), ("R", 7), ("D", 6), ("A", 3), ("Q", 5),
    ("S", 4), ("T", 0), ("E", 2), ("N", 1), ("X", 6),
    ("I", 7), ("L", 3), ("C", 5), ("M", 0), ("F", 4),
]

GX0, GY0 = 558, 72   # GY0-BR=34 > 25px safe zone
GAP, BR  = 94, 38

for idx, (letter, ci) in enumerate(GRID):
    col = idx % 5
    row = idx // 5
    bubble(GX0 + col * GAP, GY0 + row * GAP, BR,
           BUBBLE_FILLS[ci], letter, f_bubble, DARK_TEXT)

# ── Decorative accent bubbles (fill dead space in left corners) ───────────────
# top-left corner
bubble(38,  38, 16, BUBBLE_FILLS[5])   # gold, small
bubble(72,  20, 10, BUBBLE_FILLS[2])   # sky blue, tiny
# bottom-left corner
bubble(30, 465, 18, BUBBLE_FILLS[3])   # mint
bubble(70, 480, 10, BUBBLE_FILLS[6])   # pink, tiny
# between logo and grid (vertical midpoint, subtle)
bubble(430, 440, 14, BUBBLE_FILLS[1])  # amber
bubble(455, 60,  12, BUBBLE_FILLS[4])  # lavender

# ── Output ─────────────────────────────────────────────────────────────────────
out_path = os.path.abspath(
    os.path.join(os.path.dirname(__file__), "..", "feature_graphic.png"))
img.convert("RGB").save(out_path, "PNG", optimize=True)
print(f"Saved → {out_path}")
