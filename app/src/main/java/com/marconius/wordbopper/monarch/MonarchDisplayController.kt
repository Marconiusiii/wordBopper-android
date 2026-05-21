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
    private var lastFrameKey = ""

    fun create() {
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
        refresh(force = true)
        startRenderLoop()
    }

    fun resume() {
        if (!servicesBound) {
            manager.bindService()
            servicesBound = true
            refresh(force = true)
        }
        startRenderLoop()
    }

    fun stop() {
        renderJob?.cancel()
        renderJob = null
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
                viewModel.clearSelection()
                refresh(force = true)
                true
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
            GameScreen.START -> viewModel.startGame()
            GameScreen.GAME -> viewModel.makeWord()
            GameScreen.RESULTS -> viewModel.goHome()
        }
        refresh(force = true)
    }

    private fun handleBrailleDoubleTap(pointX: Int, pointY: Int) {
        if (viewModel.screen != GameScreen.GAME) {
            handlePrimaryAction()
            return
        }

        val adjustedX = if (pointX >= TOUCH_X_OFFSET) pointX - TOUCH_X_OFFSET else pointX
        val (row, col) = renderer.tappedCell(adjustedX, pointY) ?: return
        val bubble = viewModel.bubbles.firstOrNull { it.row == row && it.col == col } ?: return
        viewModel.tapBubble(bubble)
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

    private fun refresh(force: Boolean = false) {
        val frameKey = buildFrameKey()
        if (!force && frameKey == lastFrameKey) return
        lastFrameKey = frameKey
        val dots = renderer.render(viewModel)
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
        }
    }

    companion object {
        private const val TOUCH_X_OFFSET = 3
        private const val RENDER_INTERVAL_MS = 150L

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
