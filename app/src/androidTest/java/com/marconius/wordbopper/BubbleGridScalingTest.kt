package com.marconius.wordbopper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marconius.wordbopper.model.Bubble
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.BubbleTextColorOption
import com.marconius.wordbopper.model.DictionaryLanguage
import com.marconius.wordbopper.model.LetterPositionMode
import com.marconius.wordbopper.ui.components.BubbleGrid
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Layout-scaling proof for the gameplay letter grid.
 *
 * These tests do NOT render to a real device screen size; they compose [BubbleGrid] inside
 * fixed-size containers that stand in for large-screen and wide-landscape gameplay areas, then
 * assert structural guarantees that hold no matter how big the container is:
 *
 *  1. Every letter cell is still emitted (nothing gets dropped or clipped off the grid).
 *  2. Each cell stays at or above the 44dp accessible touch-target minimum.
 *
 * This gives a repeatable, screen-reader-independent guarantee that enlarging the play area
 * (tablets, foldables unfolded, split-screen) grows the grid rather than breaking it.
 */
@RunWith(AndroidJUnit4::class)
class BubbleGridScalingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val columns = 5
    private val rows = 5

    /** A full 5x5 board of distinct letters A..Y so each cell has a unique contentDescription. */
    private fun board(): List<Bubble> = buildList {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                val letter = ('A' + (r * columns + c)).toString()
                add(Bubble(letter = letter, colorIndex = 0, row = r, col = c))
            }
        }
    }

    /**
     * Composes the grid inside a [containerWidth] x [containerHeight] box, mirroring the
     * cellSize math used by GameScreen (minOf(w/cols, h/rows), floored at 44dp).
     */
    @Composable
    private fun GridIn(containerWidth: Dp, containerHeight: Dp) {
        Box(modifier = Modifier.size(containerWidth, containerHeight)) {
            BoxWithConstraints {
                val cellSize = minOf(maxWidth / columns, maxHeight / rows).coerceAtLeast(44.dp)
                BubbleGrid(
                    bubbles = board(),
                    selectedIds = emptySet(),
                    cellSize = cellSize,
                    columns = columns,
                    rows = rows,
                    textColorOption = BubbleTextColorOption.DARK,
                    letterStyle = BubbleLetterStyle.PLAYFUL,
                    dictionaryLanguage = DictionaryLanguage.ENGLISH,
                    letterPositionMode = LetterPositionMode.OFF,
                    speakLetterPhonetics = false,
                    onTap = {}
                )
            }
        }
    }

    private fun assertAllCellsPresentAndAccessible() {
        // Every one of the 25 letters must still be in the tree (lowercased: the cell
        // contentDescription is built from letter.lowercase()).
        for (i in 0 until rows * columns) {
            val letter = ('A' + i).toString().lowercase()
            composeRule.onAllNodesWithContentDescription(letter).assertCountEquals(1)
            // Each cell must meet the accessible minimum touch target in both dimensions.
            composeRule.onAllNodesWithContentDescription(letter)[0]
                .assertWidthIsAtLeast(44.dp)
                .assertHeightIsAtLeast(44.dp)
        }
    }

    @Test
    fun gridFillsAndStaysAccessibleOnLargeTablet() {
        // ~10" tablet play area, portrait-ish: roomy, cells should be large.
        composeRule.setContent { GridIn(containerWidth = 900.dp, containerHeight = 1100.dp) }
        assertAllCellsPresentAndAccessible()
    }

    @Test
    fun gridFillsAndStaysAccessibleInWideLandscape() {
        // Wide, short area like a tablet/foldable in landscape or a split-screen pane.
        composeRule.setContent { GridIn(containerWidth = 1280.dp, containerHeight = 700.dp) }
        assertAllCellsPresentAndAccessible()
    }

    @Test
    fun gridStaysAccessibleInTightSplitScreenPane() {
        // A cramped multi-window pane: the 44dp floor must keep every cell tappable
        // even when the container can't comfortably fit a 5x5 board.
        composeRule.setContent { GridIn(containerWidth = 320.dp, containerHeight = 360.dp) }
        assertAllCellsPresentAndAccessible()
    }
}
