package com.marconius.wordbopper

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
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
                val view = LocalView.current
                LaunchedEffect(Unit) {
                    viewModel.announcementEvent.collect { message ->
                        view.announceForAccessibility(message)
                    }
                }
                WordBopperApp(viewModel = viewModel)
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
