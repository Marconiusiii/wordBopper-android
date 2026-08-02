#!/usr/bin/env python3
"""Generate the 1024 by 500 Google Play feature graphic for WordBopper."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


WIDTH = 1024
HEIGHT = 500
SCALE = 3

# Colors shared with the canonical icon and the Android game theme.
BACKGROUND = (15, 14, 23)
BACKGROUND_MID = (26, 24, 38)
BACKGROUND_LIGHT = (34, 31, 53)
SURFACE = (26, 24, 38)
PANEL = (34, 31, 53)
TEXT = (255, 255, 254)
MUTED = (167, 169, 190)
ORANGE = (255, 137, 6)
CORAL = (242, 95, 76)
PINK = (229, 49, 112)
BLUE = (61, 169, 252)
GREEN = (114, 209, 143)
SELECTED = (70, 70, 93)
LETTER_DARK = (20, 18, 30)

BUBBLE_FILLS = (
    (255, 137, 6),
    (255, 159, 31),
    (61, 169, 252),
    (114, 209, 143),
    (184, 192, 255),
    (255, 209, 102),
    (239, 133, 156),
    (143, 240, 199),
)

FONT_PATH = "/System/Library/Fonts/SFNSRounded.ttf"


def scaled(value: float) -> int:
    return round(value * SCALE)


def color_between(start: tuple[int, int, int], end: tuple[int, int, int], amount: float):
    return tuple(round(a + (b - a) * amount) for a, b in zip(start, end))


def load_font(size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(FONT_PATH, scaled(size))


def draw_radial_background(image: Image.Image) -> None:
    """Draw a soft radial background derived from the canonical app icon."""
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, scaled(WIDTH), scaled(HEIGHT)), fill=BACKGROUND)

    center_x = scaled(465)
    center_y = scaled(175)
    outer_radius_x = scaled(700)
    outer_radius_y = scaled(520)
    steps = 120

    for step in range(steps, 0, -1):
        ratio = step / steps
        light_amount = (1.0 - ratio) ** 1.45
        color = color_between(BACKGROUND, BACKGROUND_LIGHT, light_amount)
        radius_x = round(outer_radius_x * ratio)
        radius_y = round(outer_radius_y * ratio)
        draw.ellipse(
            (
                center_x - radius_x,
                center_y - radius_y,
                center_x + radius_x,
                center_y + radius_y,
            ),
            fill=color,
        )


def add_blurred_ellipse(
    image: Image.Image,
    bounds: tuple[int, int, int, int],
    color: tuple[int, int, int, int],
    blur_radius: int,
) -> None:
    layer = Image.new("RGBA", image.size, (0, 0, 0, 0))
    layer_draw = ImageDraw.Draw(layer)
    layer_draw.ellipse(tuple(scaled(value) for value in bounds), fill=color)
    layer = layer.filter(ImageFilter.GaussianBlur(scaled(blur_radius)))
    image.alpha_composite(layer)


def draw_flat_bubble(
    image: Image.Image,
    center_x: int,
    center_y: int,
    radius: int,
    fill: tuple[int, int, int],
    letter: str,
    font: ImageFont.FreeTypeFont,
    selected: bool = False,
) -> None:
    """Draw a game bubble using the app's normal or selected appearance."""
    draw = ImageDraw.Draw(image)
    cx = scaled(center_x)
    cy = scaled(center_y)
    r = scaled(radius)

    shadow_offset = scaled(5)
    draw.ellipse(
        (cx - r, cy - r + shadow_offset, cx + r, cy + r + shadow_offset),
        fill=(0, 0, 0, 90),
    )

    if selected:
        ring_width = scaled(5)
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=GREEN)
        inner_radius = r - ring_width
        draw.ellipse(
            (
                cx - inner_radius,
                cy - inner_radius,
                cx + inner_radius,
                cy + inner_radius,
            ),
            fill=SELECTED,
        )
        letter_color = TEXT
    else:
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=fill)
        highlight_radius = round(r * 0.68)
        highlight = Image.new("RGBA", image.size, (0, 0, 0, 0))
        highlight_draw = ImageDraw.Draw(highlight)
        highlight_draw.ellipse(
            (
                cx - highlight_radius,
                cy - highlight_radius,
                cx + highlight_radius,
                cy + highlight_radius,
            ),
            fill=(255, 255, 255, 12),
        )
        image.alpha_composite(highlight)
        letter_color = LETTER_DARK

    draw = ImageDraw.Draw(image)
    draw.text((cx, cy - scaled(1)), letter, font=font, fill=letter_color, anchor="mm")


def draw_connection_path(image: Image.Image, points: tuple[tuple[int, int], ...]) -> None:
    scaled_points = [(scaled(x), scaled(y)) for x, y in points]

    glow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_draw.line(
        scaled_points,
        fill=(*GREEN, 180),
        width=scaled(19),
        joint="curve",
    )
    glow = glow.filter(ImageFilter.GaussianBlur(scaled(12)))
    image.alpha_composite(glow)

    draw = ImageDraw.Draw(image)
    draw.line(
        scaled_points,
        fill=GREEN,
        width=scaled(7),
        joint="curve",
    )


def draw_wordmark(image: Image.Image) -> None:
    draw = ImageDraw.Draw(image)
    title_font = load_font(66)
    tagline_font = load_font(31)

    title_x = scaled(112)
    title_y = scaled(196)
    draw.text((title_x, title_y), "Word", font=title_font, fill=TEXT, anchor="la")
    word_width = draw.textlength("Word", font=title_font)
    draw.text(
        (round(title_x + word_width), title_y),
        "Bopper",
        font=title_font,
        fill=ORANGE,
        anchor="la",
    )

    draw.text(
        (scaled(114), scaled(294)),
        "Bop letters. Build words.",
        font=tagline_font,
        fill=GREEN,
        anchor="la",
    )


def draw_grid_and_word_tray(image: Image.Image) -> None:
    bubble_font = load_font(38)
    tray_word_font = load_font(31)
    tray_label_font = load_font(13)
    multiplier_font = load_font(25)

    columns = (618, 700, 782, 864)
    rows = (82, 164, 246, 328)
    letters = (
        ("W", "A", "T", "S"),
        ("E", "O", "R", "N"),
        ("L", "C", "M", "D"),
        ("I", "B", "P", "E"),
    )
    selected_positions = ((0, 0), (1, 1), (1, 2), (2, 3))
    selected_points = tuple(
        (columns[column], rows[row]) for row, column in selected_positions
    )

    add_blurred_ellipse(image, (548, 8, 946, 392), (61, 169, 252, 52), 54)
    draw_connection_path(image, selected_points)

    selected_set = set(selected_positions)
    color_index = 0
    for row_index, row_letters in enumerate(letters):
        for column_index, letter in enumerate(row_letters):
            is_selected = (row_index, column_index) in selected_set
            draw_flat_bubble(
                image=image,
                center_x=columns[column_index],
                center_y=rows[row_index],
                radius=34,
                fill=BUBBLE_FILLS[color_index % len(BUBBLE_FILLS)],
                letter=letter,
                font=bubble_font,
                selected=is_selected,
            )
            color_index += 1

    draw = ImageDraw.Draw(image)
    tray_bounds = (scaled(603), scaled(390), scaled(918), scaled(461))
    draw.rounded_rectangle(
        tray_bounds,
        radius=scaled(18),
        fill=SURFACE,
        outline=PANEL,
        width=scaled(2),
    )
    draw.text(
        (scaled(628), scaled(405)),
        "WORD TRAY",
        font=tray_label_font,
        fill=MUTED,
        anchor="la",
    )
    draw.text(
        (scaled(626), scaled(433)),
        "WORD",
        font=tray_word_font,
        fill=TEXT,
        anchor="lm",
    )

    multiplier_bounds = (scaled(821), scaled(404), scaled(894), scaled(447))
    draw.rounded_rectangle(
        multiplier_bounds,
        radius=scaled(21),
        fill=ORANGE,
    )
    draw.text(
        (scaled(858), scaled(425)),
        "3x",
        font=multiplier_font,
        fill=LETTER_DARK,
        anchor="mm",
    )


def draw_edge_accents(image: Image.Image) -> None:
    """Add low-priority color at the edges without competing with the focal point."""
    draw = ImageDraw.Draw(image)
    accents = (
        (34, 46, 17, GREEN),
        (72, 17, 10, BLUE),
        (36, 470, 18, PINK),
        (989, 46, 25, CORAL),
        (993, 466, 31, BLUE),
    )
    for center_x, center_y, radius, color in accents:
        cx = scaled(center_x)
        cy = scaled(center_y)
        r = scaled(radius)
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=color)


def main() -> None:
    canvas = Image.new("RGBA", (scaled(WIDTH), scaled(HEIGHT)), (*BACKGROUND, 255))
    draw_radial_background(canvas)
    draw_edge_accents(canvas)
    draw_wordmark(canvas)
    draw_grid_and_word_tray(canvas)

    final_image = canvas.resize((WIDTH, HEIGHT), Image.Resampling.LANCZOS).convert("RGB")
    output_path = Path(__file__).resolve().parent.parent / "feature_graphic.png"
    final_image.save(output_path, "PNG", optimize=True)
    print(f"Saved {output_path}")


if __name__ == "__main__":
    main()
