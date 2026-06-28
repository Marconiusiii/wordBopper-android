package com.marconius.wordbopper.ui.theme

import androidx.compose.ui.graphics.Color
import com.marconius.wordbopper.model.BubbleColorTheme
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

fun bubbleFills(option: BubbleTextColorOption, theme: BubbleColorTheme): List<Color> {
    if (!theme.supports(option)) {
        return bubbleFills(option, BubbleColorTheme.defaultFor(option))
    }

    return when (theme) {
        BubbleColorTheme.CLASSIC_BRIGHT -> DarkTextBubbleFills
        BubbleColorTheme.PASTEL -> listOf(
            Color(red = 1.000f, green = 0.733f, blue = 0.820f),
            Color(red = 0.741f, green = 0.902f, blue = 1.000f),
            Color(red = 0.792f, green = 0.941f, blue = 0.753f),
            Color(red = 1.000f, green = 0.878f, blue = 0.545f),
            Color(red = 0.859f, green = 0.776f, blue = 1.000f),
            Color(red = 1.000f, green = 0.788f, blue = 0.698f),
            Color(red = 0.733f, green = 0.929f, blue = 0.902f),
            Color(red = 0.949f, green = 0.827f, blue = 0.925f),
        )
        BubbleColorTheme.SPRING -> listOf(
            Color(red = 0.741f, green = 0.929f, blue = 0.522f),
            Color(red = 0.992f, green = 0.737f, blue = 0.816f),
            Color(red = 1.000f, green = 0.902f, blue = 0.455f),
            Color(red = 0.549f, green = 0.878f, blue = 0.749f),
            Color(red = 0.816f, green = 0.741f, blue = 1.000f),
            Color(red = 0.996f, green = 0.792f, blue = 0.529f),
            Color(red = 0.690f, green = 0.886f, blue = 1.000f),
            Color(red = 0.929f, green = 0.851f, blue = 0.502f),
        )
        BubbleColorTheme.SUMMER -> listOf(
            Color(red = 1.000f, green = 0.780f, blue = 0.149f),
            Color(red = 0.137f, green = 0.820f, blue = 0.957f),
            Color(red = 1.000f, green = 0.490f, blue = 0.302f),
            Color(red = 0.553f, green = 0.902f, blue = 0.349f),
            Color(red = 1.000f, green = 0.914f, blue = 0.357f),
            Color(red = 0.310f, green = 0.780f, blue = 1.000f),
            Color(red = 1.000f, green = 0.627f, blue = 0.365f),
            Color(red = 0.678f, green = 0.855f, blue = 0.278f),
        )
        BubbleColorTheme.CANDY -> listOf(
            Color(red = 1.000f, green = 0.576f, blue = 0.741f),
            Color(red = 0.561f, green = 0.843f, blue = 1.000f),
            Color(red = 1.000f, green = 0.753f, blue = 0.227f),
            Color(red = 0.722f, green = 0.624f, blue = 1.000f),
            Color(red = 0.518f, green = 0.922f, blue = 0.706f),
            Color(red = 1.000f, green = 0.624f, blue = 0.475f),
            Color(red = 0.937f, green = 0.788f, blue = 1.000f),
            Color(red = 0.996f, green = 0.886f, blue = 0.345f),
        )
        BubbleColorTheme.GARDEN -> listOf(
            Color(red = 0.557f, green = 0.859f, blue = 0.514f),
            Color(red = 0.792f, green = 0.749f, blue = 0.373f),
            Color(red = 0.933f, green = 0.733f, blue = 0.463f),
            Color(red = 0.667f, green = 0.890f, blue = 0.694f),
            Color(red = 0.765f, green = 0.847f, blue = 0.502f),
            Color(red = 0.988f, green = 0.808f, blue = 0.584f),
            Color(red = 0.478f, green = 0.824f, blue = 0.675f),
            Color(red = 0.882f, green = 0.780f, blue = 0.420f),
        )
        BubbleColorTheme.SUNRISE -> listOf(
            Color(red = 1.000f, green = 0.655f, blue = 0.271f),
            Color(red = 1.000f, green = 0.784f, blue = 0.337f),
            Color(red = 0.984f, green = 0.549f, blue = 0.463f),
            Color(red = 1.000f, green = 0.890f, blue = 0.502f),
            Color(red = 0.957f, green = 0.686f, blue = 0.671f),
            Color(red = 1.000f, green = 0.718f, blue = 0.424f),
            Color(red = 0.925f, green = 0.765f, blue = 0.925f),
            Color(red = 1.000f, green = 0.835f, blue = 0.482f),
        )
        BubbleColorTheme.SKY -> listOf(
            Color(red = 0.475f, green = 0.812f, blue = 1.000f),
            Color(red = 0.631f, green = 0.886f, blue = 1.000f),
            Color(red = 0.788f, green = 0.831f, blue = 1.000f),
            Color(red = 0.557f, green = 0.890f, blue = 0.937f),
            Color(red = 0.722f, green = 0.906f, blue = 1.000f),
            Color(red = 0.612f, green = 0.741f, blue = 1.000f),
            Color(red = 0.518f, green = 0.855f, blue = 0.890f),
            Color(red = 0.831f, green = 0.890f, blue = 1.000f),
        )
        BubbleColorTheme.SOFT_WHITE -> List(8) {
            Color(red = 0.925f, green = 0.933f, blue = 0.949f)
        }
        BubbleColorTheme.CLASSIC_DEEP -> LightTextBubbleFills
        BubbleColorTheme.NEON -> listOf(
            Color(red = 0.000f, green = 0.376f, blue = 0.455f),
            Color(red = 0.408f, green = 0.086f, blue = 0.592f),
            Color(red = 0.655f, green = 0.000f, blue = 0.290f),
            Color(red = 0.000f, green = 0.435f, blue = 0.302f),
            Color(red = 0.110f, green = 0.231f, blue = 0.702f),
            Color(red = 0.635f, green = 0.255f, blue = 0.000f),
            Color(red = 0.486f, green = 0.000f, blue = 0.529f),
            Color(red = 0.000f, green = 0.353f, blue = 0.706f),
        )
        BubbleColorTheme.FALL -> listOf(
            Color(red = 0.514f, green = 0.141f, blue = 0.024f),
            Color(red = 0.576f, green = 0.247f, blue = 0.031f),
            Color(red = 0.459f, green = 0.286f, blue = 0.075f),
            Color(red = 0.533f, green = 0.075f, blue = 0.114f),
            Color(red = 0.365f, green = 0.286f, blue = 0.125f),
            Color(red = 0.600f, green = 0.325f, blue = 0.039f),
            Color(red = 0.420f, green = 0.176f, blue = 0.039f),
            Color(red = 0.435f, green = 0.110f, blue = 0.165f),
        )
        BubbleColorTheme.WINTER -> listOf(
            Color(red = 0.122f, green = 0.247f, blue = 0.439f),
            Color(red = 0.075f, green = 0.345f, blue = 0.451f),
            Color(red = 0.271f, green = 0.251f, blue = 0.514f),
            Color(red = 0.118f, green = 0.322f, blue = 0.365f),
            Color(red = 0.188f, green = 0.251f, blue = 0.396f),
            Color(red = 0.051f, green = 0.392f, blue = 0.478f),
            Color(red = 0.302f, green = 0.314f, blue = 0.510f),
            Color(red = 0.141f, green = 0.298f, blue = 0.490f),
        )
        BubbleColorTheme.FOREST -> listOf(
            Color(red = 0.047f, green = 0.286f, blue = 0.188f),
            Color(red = 0.141f, green = 0.333f, blue = 0.118f),
            Color(red = 0.235f, green = 0.302f, blue = 0.078f),
            Color(red = 0.075f, green = 0.357f, blue = 0.267f),
            Color(red = 0.220f, green = 0.271f, blue = 0.149f),
            Color(red = 0.000f, green = 0.333f, blue = 0.318f),
            Color(red = 0.314f, green = 0.294f, blue = 0.098f),
            Color(red = 0.102f, green = 0.251f, blue = 0.157f),
        )
        BubbleColorTheme.OCEAN -> listOf(
            Color(red = 0.000f, green = 0.298f, blue = 0.451f),
            Color(red = 0.000f, green = 0.373f, blue = 0.416f),
            Color(red = 0.055f, green = 0.220f, blue = 0.514f),
            Color(red = 0.000f, green = 0.435f, blue = 0.565f),
            Color(red = 0.075f, green = 0.251f, blue = 0.392f),
            Color(red = 0.000f, green = 0.337f, blue = 0.624f),
            Color(red = 0.000f, green = 0.282f, blue = 0.333f),
            Color(red = 0.141f, green = 0.314f, blue = 0.573f),
        )
        BubbleColorTheme.SUNSET -> listOf(
            Color(red = 0.596f, green = 0.165f, blue = 0.059f),
            Color(red = 0.518f, green = 0.102f, blue = 0.259f),
            Color(red = 0.443f, green = 0.110f, blue = 0.506f),
            Color(red = 0.706f, green = 0.267f, blue = 0.000f),
            Color(red = 0.369f, green = 0.141f, blue = 0.514f),
            Color(red = 0.612f, green = 0.204f, blue = 0.275f),
            Color(red = 0.471f, green = 0.204f, blue = 0.110f),
            Color(red = 0.302f, green = 0.149f, blue = 0.490f),
        )
        BubbleColorTheme.GALAXY -> listOf(
            Color(red = 0.149f, green = 0.137f, blue = 0.365f),
            Color(red = 0.290f, green = 0.102f, blue = 0.439f),
            Color(red = 0.086f, green = 0.188f, blue = 0.424f),
            Color(red = 0.333f, green = 0.078f, blue = 0.333f),
            Color(red = 0.133f, green = 0.235f, blue = 0.329f),
            Color(red = 0.247f, green = 0.153f, blue = 0.514f),
            Color(red = 0.078f, green = 0.153f, blue = 0.345f),
            Color(red = 0.376f, green = 0.122f, blue = 0.431f),
        )
        BubbleColorTheme.SOFT_CHARCOAL -> List(8) {
            Color(red = 0.173f, green = 0.184f, blue = 0.212f)
        }
    }
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
