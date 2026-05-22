package com.marconius.wordbopper.monarch

import android.os.Build
import android.util.Size
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.humanware.keysoftsdk.selfbrailling.SelfBraillingManager
import com.humanware.keysoftsdk.selfbrailling.aidl.DotsMatrix
import com.humanware.keysoftsdk.selfbrailling.widget.SelfBraillingWidget
import com.marconius.wordbopper.model.GameScreen
import com.marconius.wordbopper.viewmodel.GameViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MonarchDisplayController(
    private val activity: ComponentActivity,
    private val viewModel: GameViewModel,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    private lateinit var manager: SelfBraillingManager
    private lateinit var widget: SelfBraillingWidget
    private lateinit var screenDimensions: Size
    private lateinit var renderer: MonarchGridRenderer

    private val liveDots = MutableLiveData<Array<ByteArray>>()
    private val viewedImage = MutableLiveData<Array<ByteArray>>()
    private var servicesBound = false
    private var renderJob: Job? = null
    private var announcementJob: Job? = null
    private var delayedStatusJob: Job? = null
    private var lastFrameKey = ""
    private var latestStatusText = ""
    private var submittedWordWaitingForStatus = false

    fun create() {
        activity.title = ""
        viewModel.setBoardSize(MONARCH_COLUMNS, MONARCH_ROWS)
        manager = SelfBraillingManager(activity).apply { bindService() }
        servicesBound = true
        widget = SelfBraillingWidget(activity).also { activity.setContentView(it) }

        viewedImage.observe(activity) { widget.refresh(it) }
        liveDots.observe(activity) { manager.displayDots(it) }

        screenDimensions = Size(manager.brailleDisplayDotsSizeX, manager.brailleDisplayDotsSizeY)
        renderer = MonarchGridRenderer(screenDimensions.width, screenDimensions.height)

        widget.onSelfBraillingWidgetListener = object :
            SelfBraillingWidget.OnSelfBraillingWidgetListener {
            override fun onFocused() {
                refresh()
            }

            override fun onDoubleTapAtBraillePosition(pointX: Int, pointY: Int) {
                handleBrailleDoubleTap(pointX, pointY)
            }
        }

        manager.announceText("WordBopper")
        viewModel.audio.playRoundStartSound()
        refresh(force = true)
        lifecycleScope.launch {
            delay(INITIAL_REFRESH_DELAY_MS)
            refresh(force = true)
        }
        startRenderLoop()
        startAnnouncementRelay()
    }

    fun resume() {
        if (!servicesBound) {
            manager.bindService()
            servicesBound = true
            refresh(force = true)
        }
        startRenderLoop()
        startAnnouncementRelay()
    }

    fun stop() {
        renderJob?.cancel()
        renderJob = null
        announcementJob?.cancel()
        announcementJob = null
        delayedStatusJob?.cancel()
        delayedStatusJob = null
        if (servicesBound) {
            servicesBound = false
            manager.unbindService()
        }
    }

    fun destroy() {
        stop()
    }

    fun handleKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                handlePrimaryAction()
                true
            }
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_FORWARD_DEL,
            KeyEvent.KEYCODE_ESCAPE -> {
                clearStatusText()
                viewModel.clearSelection()
                refresh(force = true)
                true
            }
            KeyEvent.KEYCODE_B -> {
                if (viewModel.screen == GameScreen.START) {
                    toggleBopAway()
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                if (viewModel.screen == GameScreen.GAME) {
                    viewModel.endGame()
                    refresh(force = true)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun handlePrimaryAction() {
        when (viewModel.screen) {
            GameScreen.START -> {
                clearStatusText()
                manager.announceText("Starting game")
                viewModel.startGame()
            }
            GameScreen.GAME -> {
                val submittedWord = viewModel.currentWord.lowercase()
                if (submittedWord.isNotBlank()) {
                    delayedStatusJob?.cancel()
                    submittedWordWaitingForStatus = true
                    latestStatusText = submittedWord
                }
                manager.announceText("Make word $submittedWord")
                viewModel.makeWord()
            }
            GameScreen.RESULTS -> {
                clearStatusText()
                manager.announceText("Home")
                viewModel.goHome()
            }
        }
        refresh(force = true)
    }

    private fun handleBrailleDoubleTap(pointX: Int, pointY: Int) {
        if (viewModel.screen != GameScreen.GAME) {
            handlePrimaryAction()
            return
        }

        val adjustedX = if (pointX >= TOUCH_X_OFFSET) pointX - TOUCH_X_OFFSET else pointX
        val (row, col) = renderer.tappedCell(
            adjustedX,
            pointY,
            viewModel.boardColumns,
            viewModel.boardRows
        ) ?: return
        val bubble = viewModel.bubbles.firstOrNull { it.row == row && it.col == col } ?: return
        val wasSelected = viewModel.isSelected(bubble)
        viewModel.tapBubble(bubble)
        latestStatusText = viewModel.currentWord
        if (viewModel.bopAwayIsActive) {
            manager.announceText(
                "Added ${bubble.letter.lowercase()}, ${col + 1} ${row + 1}. Word ${viewModel.currentWord.lowercase()}"
            )
        } else {
            val action = if (wasSelected) "Deselected" else "Selected"
            manager.announceText(
                "$action ${bubble.letter.lowercase()}, ${col + 1} ${row + 1}. Word ${viewModel.currentWord.lowercase()}"
            )
        }
        refresh(force = true)
    }

    private fun startRenderLoop() {
        if (renderJob?.isActive == true) return
        renderJob = lifecycleScope.launch {
            while (true) {
                refresh()
                delay(RENDER_INTERVAL_MS)
            }
        }
    }

    private fun startAnnouncementRelay() {
        if (announcementJob?.isActive == true) return
        announcementJob = lifecycleScope.launch {
            viewModel.announcementEvent.collect { message ->
                val trayText = toMonarchTrayText(message)
                if (submittedWordWaitingForStatus) {
                    delayedStatusJob?.cancel()
                    delayedStatusJob = launch {
                        delay(SUBMITTED_WORD_HOLD_MS)
                        submittedWordWaitingForStatus = false
                        latestStatusText = trayText
                        refresh(force = true)
                    }
                } else {
                    latestStatusText = trayText
                    refresh(force = true)
                }
                manager.announceText(message)
            }
        }
    }

    private fun toggleBopAway() {
        val enabled = !viewModel.bopAway
        viewModel.setBopAway(enabled)
        clearStatusText()
        manager.announceText(if (enabled) "BopAway on" else "BopAway off")
        refresh(force = true)
    }

    private fun clearStatusText() {
        delayedStatusJob?.cancel()
        submittedWordWaitingForStatus = false
        latestStatusText = ""
    }

    private fun toMonarchTrayText(message: String): String {
        return message
            .trim()
            .removeSuffix(".")
            .replace(Regex("\\b(\\d+) points\\b"), "$1pts")
            .replace(Regex("\\b1 point\\b"), "1pt")
            .replace("chain bonus", "cb")
            .replace(",", "")
    }

    private fun refresh(force: Boolean = false) {
        val frameKey = buildFrameKey()
        if (!force && frameKey == lastFrameKey) return
        lastFrameKey = frameKey
        val dots = renderer.render(viewModel, latestStatusText)
        viewedImage.value = dots
        liveDots.value = DotsMatrix(dots).matrix
    }

    private fun buildFrameKey(): String {
        return buildString {
            append(viewModel.screen.name)
            append('|')
            append(viewModel.score)
            append('|')
            append(viewModel.wordCount)
            append('|')
            append(viewModel.bubbles.joinToString("") { it.letter + it.id.toString().take(4) })
            append('|')
            append(viewModel.selected.joinToString("") { it.bubbleId.toString().take(4) })
            append('|')
            append(latestStatusText)
        }
    }

    companion object {
        private const val TOUCH_X_OFFSET = 1
        private const val MONARCH_COLUMNS = 6
        private const val MONARCH_ROWS = 5
        private const val RENDER_INTERVAL_MS = 150L
        private const val INITIAL_REFRESH_DELAY_MS = 1000L
        private const val SUBMITTED_WORD_HOLD_MS = 5000L

        fun shouldUseMonarchMode(): Boolean {
            val deviceInfo = listOf(
                Build.MANUFACTURER,
                Build.BRAND,
                Build.MODEL,
                Build.DEVICE,
                Build.PRODUCT
            ).joinToString(" ").lowercase()

            return listOf("monarch", "humanware", "keysoft", "aph").any { marker ->
                deviceInfo.contains(marker)
            }
        }
    }
}
