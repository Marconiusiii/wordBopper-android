package com.marconius.wordbopper.ui.theme

import androidx.compose.ui.graphics.Color
import com.marconius.wordbopper.model.BubbleTextColorOption

val WbBackground = Color(red = 0.059f, green = 0.055f, blue = 0.090f)
val WbSurface    = Color(red = 0.102f, green = 0.094f, blue = 0.149f)
val WbPanel      = Color(red = 0.133f, green = 0.122f, blue = 0.208f)
val WbText       = Color(red = 1.000f, green = 1.000f, blue = 0.996f)
val WbMuted      = Color(red = 0.655f, green = 0.663f, blue = 0.745f)
val WbAccent1    = Color(red = 1.000f, green = 0.537f, blue = 0.024f)
val WbAccent2    = Color(red = 0.949f, green = 0.373f, blue = 0.298f)
val WbAccent3    = Color(red = 0.898f, green = 0.192f, blue = 0.439f)
val WbAccent4    = Color(red = 0.239f, green = 0.663f, blue = 0.988f)
val WbAccent5    = Color(red = 0.447f, green = 0.820f, blue = 0.561f)
val WbTimerGreen = WbAccent5
val WbSelectedBubble = Color(red = 0.275f, green = 0.275f, blue = 0.365f)

val DarkTextBubbleFills = listOf(
    Color(red = 1.000f, green = 0.537f, blue = 0.024f),
    Color(red = 1.000f, green = 0.624f, blue = 0.122f),
    Color(red = 0.239f, green = 0.663f, blue = 0.988f),
    Color(red = 0.447f, green = 0.820f, blue = 0.561f),
    Color(red = 0.722f, green = 0.753f, blue = 1.000f),
    Color(red = 1.000f, green = 0.820f, blue = 0.400f),
    Color(red = 0.937f, green = 0.522f, blue = 0.659f),
    Color(red = 0.561f, green = 0.941f, blue = 0.780f),
)

val LightTextBubbleFills = listOf(
    Color(red = 0.451f, green = 0.141f, blue = 0.027f),
    Color(red = 0.514f, green = 0.128f, blue = 0.235f),
    Color(red = 0.345f, green = 0.176f, blue = 0.651f),
    Color(red = 0.075f, green = 0.298f, blue = 0.565f),
    Color(red = 0.000f, green = 0.373f, blue = 0.290f),
    Color(red = 0.333f, green = 0.263f, blue = 0.675f),
    Color(red = 0.478f, green = 0.267f, blue = 0.024f),
    Color(red = 0.282f, green = 0.251f, blue = 0.376f),
)

fun bubbleFills(option: BubbleTextColorOption) = when (option) {
    BubbleTextColorOption.DARK -> DarkTextBubbleFills
    BubbleTextColorOption.LIGHT -> LightTextBubbleFills
}

fun bubbleTextColor(option: BubbleTextColorOption) = when (option) {
    BubbleTextColorOption.DARK -> Color.Black
    BubbleTextColorOption.LIGHT -> Color.White
}

fun selectedBubbleFill(option: BubbleTextColorOption) = when (option) {
    BubbleTextColorOption.DARK -> WbSelectedBubble
    BubbleTextColorOption.LIGHT -> Color(red = 1.0f, green = 0.878f, blue = 0.322f)
}

fun selectedBubbleTextColor(option: BubbleTextColorOption) = when (option) {
    BubbleTextColorOption.DARK -> Color.White
    BubbleTextColorOption.LIGHT -> Color.Black
}

fun selectedBubbleRingColor(option: BubbleTextColorOption) = when (option) {
    BubbleTextColorOption.DARK -> WbAccent5
    BubbleTextColorOption.LIGHT -> Color(red = 0.075f, green = 0.298f, blue = 0.565f)
}
