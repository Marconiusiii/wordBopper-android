package com.marconius.wordbopper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import com.marconius.wordbopper.model.GameScreen
import com.marconius.wordbopper.ui.screens.GameScreen
import com.marconius.wordbopper.ui.screens.ResultsScreen
import com.marconius.wordbopper.ui.screens.StartScreen
import com.marconius.wordbopper.ui.theme.WordBopperTheme
import com.marconius.wordbopper.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

@Composable
fun WordBopperApp(viewModel: GameViewModel) {
    when (viewModel.screen) {
        GameScreen.START -> StartScreen(viewModel)
        GameScreen.GAME -> GameScreen(viewModel)
        GameScreen.RESULTS -> ResultsScreen(viewModel)
    }
}
