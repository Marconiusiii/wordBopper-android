package com.marconius.wordbopper.ui.components

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import com.marconius.wordbopper.ui.LocalReduceMotion
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.model.Bubble
import com.marconius.wordbopper.model.BubbleColorTheme
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.BubbleTextColorOption
import com.marconius.wordbopper.model.DictionaryLanguage
import com.marconius.wordbopper.model.LetterPositionMode
import com.marconius.wordbopper.ui.theme.bubbleFills
import com.marconius.wordbopper.ui.theme.bubbleTextColor
import com.marconius.wordbopper.ui.theme.selectedBubbleFill
import com.marconius.wordbopper.ui.theme.selectedBubbleRingColor
import com.marconius.wordbopper.ui.theme.selectedBubbleTextColor

@Composable
fun BubbleGrid(
    bubbles: List<Bubble>,
    selectedIds: Set<java.util.UUID>,
    cellSize: Dp,
    columns: Int,
    rows: Int,
    textColorOption: BubbleTextColorOption,
    colorTheme: BubbleColorTheme,
    letterStyle: BubbleLetterStyle,
    dictionaryLanguage: DictionaryLanguage,
    letterPositionMode: LetterPositionMode,
    speakLetterPhonetics: Boolean,
    onTap: (Bubble) -> Unit,
    modifier: Modifier = Modifier,
    rectangularCells: Boolean = false
) {
    Column(modifier = modifier.fillMaxSize()) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until columns) {
                    val bubble = bubbles.getOrNull(row * columns + col) ?: continue
                    val isSelected = selectedIds.contains(bubble.id)
                    BubbleCell(
                        bubble = bubble,
                        isSelected = isSelected,
                        visualSize = cellSize,
                        textColorOption = textColorOption,
                        colorTheme = colorTheme,
                        letterStyle = letterStyle,
                        dictionaryLanguage = dictionaryLanguage,
                        letterPositionMode = letterPositionMode,
                        speakLetterPhonetics = speakLetterPhonetics,
                        rectangularCell = rectangularCells,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onTap = { onTap(bubble) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BubbleCell(
    bubble: Bubble,
    isSelected: Boolean,
    visualSize: Dp,
    textColorOption: BubbleTextColorOption,
    colorTheme: BubbleColorTheme,
    letterStyle: BubbleLetterStyle,
    dictionaryLanguage: DictionaryLanguage,
    letterPositionMode: LetterPositionMode,
    speakLetterPhonetics: Boolean,
    rectangularCell: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val fills = bubbleFills(textColorOption, colorTheme)
    val fillColor = if (isSelected) selectedBubbleFill(textColorOption)
        else fills.getOrElse(bubble.colorIndex) { fills[0] }
    val textColor = if (isSelected) selectedBubbleTextColor(textColorOption)
        else bubbleTextColor(textColorOption)
    val ringColor = if (isSelected) selectedBubbleRingColor(textColorOption) else Color.Transparent

    val reduceMotion = LocalReduceMotion.current
    val touchExplorationEnabled = rememberTouchExplorationEnabled()
    val reduceVisualMotion = reduceMotion || touchExplorationEnabled
    val targetScale = if (isSelected) 0.88f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (reduceVisualMotion) snap() else spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "bubble_scale"
    )

    val label = remember(
        bubble.letter,
        bubble.col,
        bubble.row,
        dictionaryLanguage,
        speakLetterPhonetics,
        letterPositionMode
    ) {
        buildBubbleLabel(
            letter = bubble.letter,
            dictionaryLanguage = dictionaryLanguage,
            speakPhonetics = speakLetterPhonetics,
            letterPositionMode = letterPositionMode,
            col = bubble.col,
            row = bubble.row
        )
    }
    val bubbleShape = if (rectangularCell) RoundedCornerShape(18.dp) else CircleShape

    // clearAndSetSemantics completely removes the inner Text node from the accessibility tree,
    // preventing the uppercase visual letter from leaking into the announcement.
    // - selected is only set when true; false would cause TalkBack to say "not selected" on every bubble
    // - role is omitted; the onClick action is sufficient to mark it as activatable without adding "button"
    // - onClick label is only set when selected so users hear "Double-tap to deselect" on selected bubbles;
    //   unselected bubbles just say "Double-tap to activate" (shorter, faster for gameplay)
    Box(
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = label
                if (isSelected) selected = true
                onClick(label = if (isSelected) "deselect" else null) {
                    onTap()
                    true
                }
            }
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = if (rectangularCell) {
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            } else {
                Modifier.size(visualSize * 0.92f)
            }
                .scale(scale)
                .then(
                    if (!isSelected && !touchExplorationEnabled) {
                        Modifier.shadow(4.dp, bubbleShape, ambientColor = Color.Black.copy(alpha = 0.3f))
                    }
                    else Modifier
                )
                .clip(bubbleShape)
                .background(fillColor)
                .then(
                    if (isSelected) Modifier.border(4.dp, ringColor, bubbleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayLetter(bubble.letter),
                fontSize = (visualSize.value * if (rectangularCell) 0.50f else 0.58f).coerceAtMost(44f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = letterStyle.fontFamily,
                color = textColor
            )
        }
    }
}

@Composable
private fun rememberTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = remember(context) {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    }
    var enabled by remember(accessibilityManager) {
        mutableStateOf(accessibilityManager?.isTouchExplorationEnabled == true)
    }

    DisposableEffect(accessibilityManager) {
        if (accessibilityManager == null) return@DisposableEffect onDispose {}
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { isEnabled ->
            enabled = isEnabled
        }
        accessibilityManager.addTouchExplorationStateChangeListener(listener)
        onDispose {
            accessibilityManager.removeTouchExplorationStateChangeListener(listener)
        }
    }

    return enabled
}

// Produces concise labels like "d", "d, 3 4", "d, B3", or "d, 3A" depending on settings.
private fun buildBubbleLabel(
    letter: String,
    dictionaryLanguage: DictionaryLanguage,
    speakPhonetics: Boolean,
    letterPositionMode: LetterPositionMode,
    col: Int,
    row: Int
): String {
    val lower = letter.lowercase()
    val sb = StringBuilder(lower)
    if (speakPhonetics) {
        dictionaryLanguage.phoneticName(lower)?.let { sb.append(", $it") }
    }
    positionValue(letterPositionMode, col, row)?.let { sb.append(", $it") }
    return sb.toString()
}

private fun positionValue(mode: LetterPositionMode, col: Int, row: Int): String? {
    return when (mode) {
        LetterPositionMode.OFF -> null
        LetterPositionMode.COLUMN_NUMBER_ROW_NUMBER -> "${col + 1} ${row + 1}"
        LetterPositionMode.COLUMN_LETTER_ROW_NUMBER -> "${gridLetter(col)}${row + 1}"
        LetterPositionMode.COLUMN_NUMBER_ROW_LETTER -> "${col + 1}${gridLetter(row)}"
    }
}

private fun gridLetter(index: Int): String {
    return ('A'.code + index).toChar().toString()
}

private fun displayLetter(letter: String): String {
    return if (letter == "ß") "ß" else letter.uppercase()
}
