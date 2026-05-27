package com.marconius.wordbopper

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.marconius.wordbopper.monarch.MonarchDisplayController
import com.marconius.wordbopper.model.GameScreen
import com.marconius.wordbopper.ui.screens.GameScreen
import com.marconius.wordbopper.ui.screens.ResultsScreen
import com.marconius.wordbopper.ui.screens.StartScreen
import com.marconius.wordbopper.ui.theme.WordBopperTheme
import com.marconius.wordbopper.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private var monarchController: MonarchDisplayController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MonarchDisplayController.shouldUseMonarchMode()) {
            monarchController = MonarchDisplayController(
                activity = this,
                viewModel = viewModel,
                lifecycleScope = lifecycleScope
            ).also { it.create() }
            return
        }

        enableEdgeToEdge()
        setContent {
            WordBopperTheme {
                var announcementSerial by remember { mutableIntStateOf(0) }
                var currentAnnouncement by remember { mutableStateOf("") }
                LaunchedEffect(Unit) {
                    viewModel.announcementEvent.collect { message ->
                        announcementSerial += 1
                        currentAnnouncement = message
                    }
                }
                WordBopperApp(viewModel = viewModel)
                AccessibilityAnnouncementHost(
                    serial = announcementSerial,
                    message = currentAnnouncement
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        monarchController?.resume()
    }

    override fun onStop() {
        monarchController?.stop()
        super.onStop()
    }

    override fun onDestroy() {
        monarchController?.destroy()
        monarchController = null
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (
            event.action == KeyEvent.ACTION_DOWN &&
            monarchController?.handleKeyDown(event.keyCode) == true
        ) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (monarchController?.handleKeyDown(keyCode) == true) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun WordBopperApp(viewModel: GameViewModel) {
    when (viewModel.screen) {
        GameScreen.START -> StartScreen(viewModel)
        GameScreen.GAME -> GameScreen(viewModel)
        GameScreen.RESULTS -> ResultsScreen(viewModel)
    }
}

@Composable
private fun AccessibilityAnnouncementHost(serial: Int, message: String) {
    if (message.isBlank()) return
    key(serial) {
        Box(
            modifier = Modifier
                .size(1.dp)
                .semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = message
                }
        )
    }
}
