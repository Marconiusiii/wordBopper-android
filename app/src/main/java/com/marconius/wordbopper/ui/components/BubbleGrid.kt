package com.marconius.wordbopper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.marconius.wordbopper.model.BubbleTextColorOption
import com.marconius.wordbopper.ui.theme.bubbleFills
import com.marconius.wordbopper.ui.theme.bubbleTextColor
import com.marconius.wordbopper.ui.theme.selectedBubbleFill
import com.marconius.wordbopper.ui.theme.selectedBubbleRingColor
import com.marconius.wordbopper.ui.theme.selectedBubbleTextColor

private val phonetics = listOf(
    "Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf",
    "Hotel", "India", "Juliet", "Kilo", "Lima", "Mike", "November", "Oscar",
    "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor",
    "Whiskey", "X-Ray", "Yankee", "Zulu"
)

@Composable
fun BubbleGrid(
    bubbles: List<Bubble>,
    selectedIds: Set<java.util.UUID>,
    cellSize: Dp,
    columns: Int,
    rows: Int,
    textColorOption: BubbleTextColorOption,
    speakLetterPositions: Boolean,
    speakLetterPhonetics: Boolean,
    onTap: (Bubble) -> Unit,
    modifier: Modifier = Modifier
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
                        speakLetterPositions = speakLetterPositions,
                        speakLetterPhonetics = speakLetterPhonetics,
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
    speakLetterPositions: Boolean,
    speakLetterPhonetics: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val fills = bubbleFills(textColorOption)
    val fillColor = if (isSelected) selectedBubbleFill(textColorOption)
        else fills.getOrElse(bubble.colorIndex) { fills[0] }
    val textColor = if (isSelected) selectedBubbleTextColor(textColorOption)
        else bubbleTextColor(textColorOption)
    val ringColor = if (isSelected) selectedBubbleRingColor(textColorOption) else Color.Transparent

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "bubble_scale"
    )

    val label = buildBubbleLabel(
        letter = bubble.letter,
        speakPhonetics = speakLetterPhonetics,
        speakPositions = speakLetterPositions,
        col = bubble.col,
        row = bubble.row
    )

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
            modifier = Modifier
                .size(visualSize * 0.92f)
                .scale(scale)
                .then(
                    if (!isSelected) Modifier.shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f))
                    else Modifier
                )
                .clip(CircleShape)
                .background(fillColor)
                .then(
                    if (isSelected) Modifier.border(4.dp, ringColor, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bubble.letter.uppercase(),
                fontSize = (visualSize.value * 0.58f).coerceAtMost(40f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
        }
    }
}

// Produces "d", "d, 3 4", "d, Delta", or "d, Delta, 3 4" depending on settings.
// Numbers only for position — no "column"/"row" words — so TalkBack stays concise.
private fun buildBubbleLabel(
    letter: String,
    speakPhonetics: Boolean,
    speakPositions: Boolean,
    col: Int,
    row: Int
): String {
    val lower = letter.lowercase()
    val sb = StringBuilder(lower)
    if (speakPhonetics) {
        val idx = lower.firstOrNull()?.code?.minus('a'.code) ?: -1
        if (idx in phonetics.indices) sb.append(", ${phonetics[idx]}")
    }
    if (speakPositions) sb.append(", ${col + 1} ${row + 1}")
    return sb.toString()
}
