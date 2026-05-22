package com.marconius.wordbopper.monarch

import com.marconius.wordbopper.model.Bubble
import com.marconius.wordbopper.model.GameScreen
import com.marconius.wordbopper.model.SelectedLetter
import com.marconius.wordbopper.viewmodel.GameViewModel

private const val PIN_DOWN: Byte = 0
private const val PIN_UP: Byte = 1

class MonarchGridRenderer(
    private val width: Int,
    private val height: Int
) {
    private val gameplayTop = 5
    private val gameplayHeight = height - gameplayTop

    fun render(viewModel: GameViewModel, statusText: String = ""): Array<ByteArray> {
        val dots = blank()
        when (viewModel.screen) {
            GameScreen.START -> renderStart(dots, viewModel)
            GameScreen.GAME -> renderGame(dots, viewModel, statusText)
            GameScreen.RESULTS -> renderResults(dots, viewModel)
        }
        return dots
    }

    fun tappedCell(pointX: Int, pointY: Int, columns: Int, rows: Int): Pair<Int, Int>? {
        if (pointX !in 0 until width || pointY < gameplayTop || pointY >= height) return null
        val cellWidth = width / columns
        val cellHeight = gameplayHeight / rows
        val col = (pointX / cellWidth).coerceIn(0, columns - 1)
        val row = ((pointY - gameplayTop) / cellHeight).coerceIn(0, rows - 1)
        return row to col
    }

    private fun renderStart(dots: Array<ByteArray>, viewModel: GameViewModel) {
        drawBrailleText(dots, "word", 42, 5)
        drawBrailleText(dots, "bopper", 39, 16)
        if (viewModel.bopAway) {
            drawBrailleText(dots, "bopaway on", 28, 26)
        }
        drawBrailleText(dots, "enter", 40, 32)
    }

    private fun renderGame(dots: Array<ByteArray>, viewModel: GameViewModel, statusText: String) {
        val trayText = when {
            viewModel.currentWord.isNotEmpty() -> viewModel.currentWord
            statusText.isNotBlank() -> statusText
            else -> "word tray"
        }
        drawBrailleText(dots, trayText.lowercase().filter { it.isLetterOrDigit() || it == ' ' }.take(TRAY_CHARACTER_LIMIT), 0, 0)

        val selectedIds = viewModel.selected.map { it.bubbleId }.toSet()
        viewModel.bubbles.forEach { bubble ->
            drawCell(
                dots = dots,
                bubble = bubble,
                isSelected = selectedIds.any { it == bubble.id },
                selected = viewModel.selected,
                columns = viewModel.boardColumns,
                rows = viewModel.boardRows
            )
        }
    }

    private fun renderResults(dots: Array<ByteArray>, viewModel: GameViewModel) {
        drawBrailleText(dots, "score", 4, 3)
        drawBrailleNumber(dots, viewModel.score, 32, 3)
        drawBrailleText(dots, "words", 4, 16)
        drawBrailleNumber(dots, viewModel.wordCount, 32, 16)
        drawBrailleText(dots, "enter", 40, 30)
    }

    private fun drawCell(
        dots: Array<ByteArray>,
        bubble: Bubble,
        isSelected: Boolean,
        selected: List<SelectedLetter>,
        columns: Int,
        rows: Int
    ) {
        val cellWidth = width / columns
        val cellHeight = gameplayHeight / rows
        val left = bubble.col * cellWidth
        val top = gameplayTop + bubble.row * cellHeight
        val right = if (bubble.col == columns - 1) width - 1 else left + cellWidth - 1
        val bottom = if (bubble.row == rows - 1) height - 1 else top + cellHeight - 1

        if (isSelected) {
            drawHorizontalLine(dots, left + 4, right - 4, top + 1)
            drawHorizontalLine(dots, left + 4, right - 4, bottom - 1)
        }

        val letterX = left + ((right - left - BRAILLE_CELL_WIDTH) / 2)
        val letterY = top + ((bottom - top - BRAILLE_CELL_HEIGHT) / 2)
        drawBrailleCell(dots, bubble.letter.firstOrNull() ?: ' ', letterX, letterY)

        val selectionIndex = selected.indexOfFirst { it.bubbleId == bubble.id }
        if (selectionIndex >= 0) {
            drawBrailleNumber(dots, selectionIndex + 1, right - 7, bottom - 3)
        }
    }

    private fun drawBrailleText(dots: Array<ByteArray>, text: String, x: Int, y: Int) {
        var cursor = x
        text.forEach { char ->
            drawBrailleCell(dots, char, cursor, y)
            cursor += BRAILLE_CELL_STRIDE
        }
    }

    private fun drawBrailleNumber(dots: Array<ByteArray>, number: Int, x: Int, y: Int) {
        var cursor = x
        drawBraillePattern(dots, numberSignDots, cursor, y)
        cursor += BRAILLE_CELL_STRIDE
        number.toString().forEach { digit ->
            val pattern = numberDots[digit] ?: return@forEach
            drawBraillePattern(dots, pattern, cursor, y)
            cursor += BRAILLE_CELL_STRIDE
        }
    }

    private fun drawBrailleCell(dots: Array<ByteArray>, char: Char, x: Int, y: Int) {
        val unicodeBraille = unicodeBrailleCells[char.lowercaseChar()] ?: return
        drawBrailleCharacter(dots, unicodeBraille, x, y)
    }

    private fun drawBraillePattern(dots: Array<ByteArray>, pattern: Set<Int>, x: Int, y: Int) {
        pattern.forEach { dot ->
            when (dot) {
                1 -> setPin(dots, x, y)
                2 -> setPin(dots, x, y + BRAILLE_DOT_STEP)
                3 -> setPin(dots, x, y + BRAILLE_DOT_STEP * 2)
                4 -> setPin(dots, x + BRAILLE_DOT_STEP, y)
                5 -> setPin(dots, x + BRAILLE_DOT_STEP, y + BRAILLE_DOT_STEP)
                6 -> setPin(dots, x + BRAILLE_DOT_STEP, y + BRAILLE_DOT_STEP * 2)
            }
        }
    }

    private fun drawBrailleCharacter(dots: Array<ByteArray>, char: Char, x: Int, y: Int) {
        val code = char.code
        if (code !in 0x2800..0x28ff) return
        if ((code and 0x01) != 0) setPin(dots, x, y)
        if ((code and 0x02) != 0) setPin(dots, x, y + 1)
        if ((code and 0x04) != 0) setPin(dots, x, y + 2)
        if ((code and 0x08) != 0) setPin(dots, x + 1, y)
        if ((code and 0x10) != 0) setPin(dots, x + 1, y + 1)
        if ((code and 0x20) != 0) setPin(dots, x + 1, y + 2)
        if ((code and 0x40) != 0) setPin(dots, x, y + 3)
        if ((code and 0x80) != 0) setPin(dots, x + 1, y + 3)
    }

    private fun drawHorizontalLine(dots: Array<ByteArray>, startX: Int, endX: Int, y: Int) {
        for (x in startX..endX) setPin(dots, x, y)
    }

    private fun setPin(dots: Array<ByteArray>, x: Int, y: Int) {
        if (y in dots.indices && x in dots[y].indices) {
            dots[y][x] = PIN_UP
        }
    }

    private fun blank(): Array<ByteArray> = Array(height) { ByteArray(width) { PIN_DOWN } }

    companion object {
        private const val BRAILLE_DOT_STEP = 1
        private const val BRAILLE_CELL_WIDTH = 2
        private const val BRAILLE_CELL_HEIGHT = 4
        private const val BRAILLE_CELL_STRIDE = 4
        private const val TRAY_CHARACTER_LIMIT = 24
        private val numberSignDots = setOf(3, 4, 5, 6)

        private val unicodeBrailleCells = mapOf(
            'a' to '\u2801',
            'b' to '\u2803',
            'c' to '\u2809',
            'd' to '\u2819',
            'e' to '\u2811',
            'f' to '\u280b',
            'g' to '\u281b',
            'h' to '\u2813',
            'i' to '\u280a',
            'j' to '\u281a',
            'k' to '\u2805',
            'l' to '\u2807',
            'm' to '\u280d',
            'n' to '\u281d',
            'o' to '\u2815',
            'p' to '\u280f',
            'q' to '\u281f',
            'r' to '\u2817',
            's' to '\u280e',
            't' to '\u281e',
            'u' to '\u2825',
            'v' to '\u2827',
            'w' to '\u283a',
            'x' to '\u282d',
            'y' to '\u283d',
            'z' to '\u2835'
        )

        private val numberDots = mapOf(
            '1' to setOf(1),
            '2' to setOf(1, 2),
            '3' to setOf(1, 4),
            '4' to setOf(1, 4, 5),
            '5' to setOf(1, 5),
            '6' to setOf(1, 2, 4),
            '7' to setOf(1, 2, 4, 5),
            '8' to setOf(1, 2, 5),
            '9' to setOf(2, 4),
            '0' to setOf(2, 4, 5)
        )
    }
}
