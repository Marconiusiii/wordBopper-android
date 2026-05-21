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
    private val cellWidth = width / 5
    private val cellHeight = height / 5

    fun render(viewModel: GameViewModel): Array<ByteArray> {
        val dots = blank()
        when (viewModel.screen) {
            GameScreen.START -> renderStart(dots)
            GameScreen.GAME -> renderGame(dots, viewModel)
            GameScreen.RESULTS -> renderResults(dots, viewModel)
        }
        return dots
    }

    fun tappedCell(pointX: Int, pointY: Int): Pair<Int, Int>? {
        if (pointX !in 0 until width || pointY !in 0 until height) return null
        val col = ((pointX * 5) / width).coerceIn(0, 4)
        val row = ((pointY * 5) / height).coerceIn(0, 4)
        return row to col
    }

    private fun renderStart(dots: Array<ByteArray>) {
        drawText(dots, "word", 10, 5)
        drawText(dots, "bopper", 10, 16)
        drawText(dots, "enter", 25, 30)
    }

    private fun renderGame(dots: Array<ByteArray>, viewModel: GameViewModel) {
        val selectedIds = viewModel.selected.map { it.bubbleId }.toSet()
        viewModel.bubbles.forEach { bubble ->
            drawCell(dots, bubble, selectedIds.any { it == bubble.id }, viewModel.selected)
        }
    }

    private fun renderResults(dots: Array<ByteArray>, viewModel: GameViewModel) {
        drawText(dots, "score", 4, 3)
        drawNumber(dots, viewModel.score, 42, 3)
        drawText(dots, "words", 4, 16)
        drawNumber(dots, viewModel.wordCount, 42, 16)
        drawText(dots, "enter", 20, 30)
    }

    private fun drawCell(
        dots: Array<ByteArray>,
        bubble: Bubble,
        isSelected: Boolean,
        selected: List<SelectedLetter>
    ) {
        val left = bubble.col * cellWidth
        val top = bubble.row * cellHeight
        val right = if (bubble.col == 4) width - 1 else left + cellWidth - 1
        val bottom = if (bubble.row == 4) height - 1 else top + cellHeight - 1

        drawHorizontalLine(dots, left, right, top)
        drawVerticalLine(dots, left, top, bottom)
        if (bubble.col == 4) drawVerticalLine(dots, right, top, bottom)
        if (bubble.row == 4) drawHorizontalLine(dots, left, right, bottom)

        if (isSelected) {
            drawHorizontalLine(dots, left + 2, right - 2, top + 1)
            drawHorizontalLine(dots, left + 2, right - 2, bottom - 1)
        }

        val letterX = left + ((right - left - 5) / 2)
        val letterY = top + 1
        drawGlyph(dots, bubble.letter.firstOrNull() ?: ' ', letterX, letterY)

        val selectionIndex = selected.indexOfFirst { it.bubbleId == bubble.id }
        if (selectionIndex >= 0) {
            drawNumber(dots, selectionIndex + 1, right - 5, bottom - 5)
        }
    }

    private fun drawText(dots: Array<ByteArray>, text: String, x: Int, y: Int) {
        var cursor = x
        text.forEach { char ->
            drawGlyph(dots, char, cursor, y)
            cursor += 6
        }
    }

    private fun drawNumber(dots: Array<ByteArray>, number: Int, x: Int, y: Int) {
        drawText(dots, number.toString(), x, y)
    }

    private fun drawGlyph(dots: Array<ByteArray>, char: Char, x: Int, y: Int) {
        val rows = glyphs[char.lowercaseChar()] ?: glyphs[' '] ?: return
        rows.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, value ->
                if (value == '1') setPin(dots, x + colIndex, y + rowIndex)
            }
        }
    }

    private fun drawHorizontalLine(dots: Array<ByteArray>, startX: Int, endX: Int, y: Int) {
        for (x in startX..endX) setPin(dots, x, y)
    }

    private fun drawVerticalLine(dots: Array<ByteArray>, x: Int, startY: Int, endY: Int) {
        for (y in startY..endY) setPin(dots, x, y)
    }

    private fun setPin(dots: Array<ByteArray>, x: Int, y: Int) {
        if (y in dots.indices && x in dots[y].indices) {
            dots[y][x] = PIN_UP
        }
    }

    private fun blank(): Array<ByteArray> = Array(height) { ByteArray(width) { PIN_DOWN } }

    companion object {
        private val glyphs = mapOf(
            ' ' to listOf("00000", "00000", "00000", "00000", "00000"),
            '0' to listOf("11100", "10100", "10100", "10100", "11100"),
            '1' to listOf("01000", "11000", "01000", "01000", "11100"),
            '2' to listOf("11100", "00100", "11100", "10000", "11100"),
            '3' to listOf("11100", "00100", "11100", "00100", "11100"),
            '4' to listOf("10100", "10100", "11100", "00100", "00100"),
            '5' to listOf("11100", "10000", "11100", "00100", "11100"),
            '6' to listOf("11100", "10000", "11100", "10100", "11100"),
            '7' to listOf("11100", "00100", "01000", "01000", "01000"),
            '8' to listOf("11100", "10100", "11100", "10100", "11100"),
            '9' to listOf("11100", "10100", "11100", "00100", "11100"),
            'a' to listOf("01110", "10001", "11111", "10001", "10001"),
            'b' to listOf("11110", "10001", "11110", "10001", "11110"),
            'c' to listOf("01111", "10000", "10000", "10000", "01111"),
            'd' to listOf("11110", "10001", "10001", "10001", "11110"),
            'e' to listOf("11111", "10000", "11110", "10000", "11111"),
            'f' to listOf("11111", "10000", "11110", "10000", "10000"),
            'g' to listOf("01111", "10000", "10111", "10001", "01111"),
            'h' to listOf("10001", "10001", "11111", "10001", "10001"),
            'i' to listOf("11100", "01000", "01000", "01000", "11100"),
            'j' to listOf("00111", "00010", "00010", "10010", "01100"),
            'k' to listOf("10001", "10010", "11100", "10010", "10001"),
            'l' to listOf("10000", "10000", "10000", "10000", "11111"),
            'm' to listOf("10001", "11011", "10101", "10001", "10001"),
            'n' to listOf("10001", "11001", "10101", "10011", "10001"),
            'o' to listOf("01110", "10001", "10001", "10001", "01110"),
            'p' to listOf("11110", "10001", "11110", "10000", "10000"),
            'q' to listOf("01110", "10001", "10101", "10010", "01101"),
            'r' to listOf("11110", "10001", "11110", "10010", "10001"),
            's' to listOf("01111", "10000", "01110", "00001", "11110"),
            't' to listOf("11111", "00100", "00100", "00100", "00100"),
            'u' to listOf("10001", "10001", "10001", "10001", "01110"),
            'v' to listOf("10001", "10001", "10001", "01010", "00100"),
            'w' to listOf("10001", "10001", "10101", "11011", "10001"),
            'x' to listOf("10001", "01010", "00100", "01010", "10001"),
            'y' to listOf("10001", "01010", "00100", "00100", "00100"),
            'z' to listOf("11111", "00010", "00100", "01000", "11111")
        )
    }
}
