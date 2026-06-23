package com.marconius.wordbopper

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.marconius.wordbopper.monarch.MonarchDisplayController
import com.marconius.wordbopper.model.GameScreen
import com.marconius.wordbopper.ui.screens.GameScreen
import com.marconius.wordbopper.ui.screens.ResultsScreen
import com.marconius.wordbopper.ui.LocalReduceMotion
import com.marconius.wordbopper.ui.rememberReduceMotion
import com.marconius.wordbopper.ui.screens.StartScreen
import com.marconius.wordbopper.ui.theme.WbBackground
import com.marconius.wordbopper.ui.theme.WbText
import com.marconius.wordbopper.ui.theme.WordBopperTheme
import com.marconius.wordbopper.viewmodel.GameViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private var monarchController: MonarchDisplayController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MonarchDisplayController.shouldUseMonarchMode()) {
            // The Monarch path has no Loading screen to trigger warm-up, so kick it off
            // here; it advances the screen from LOADING to START when ready, which the
            // controller already handles.
            viewModel.warmUp()
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
              CompositionLocalProvider(LocalReduceMotion provides rememberReduceMotion()) {
                var announcementSerial by remember { mutableIntStateOf(0) }
                var currentAnnouncement by remember { mutableStateOf("") }
                var ageSignalsMessage by remember { mutableStateOf<AgeSignalsMessage?>(null) }
                LaunchedEffect(Unit) {
                    viewModel.announcementEvent.collect { message ->
                        announcementSerial += 1
                        currentAnnouncement = message
                    }
                }
                LaunchedEffect(Unit) {
                    AgeSignalsChecker(this@MainActivity).check { result ->
                        if (isFinishing || isDestroyed) {
                            return@check
                        }
                        ageSignalsMessage = result.toMessage()
                    }
                }
                WordBopperApp(viewModel = viewModel)
                AccessibilityAnnouncementHost(
                    serial = announcementSerial,
                    message = currentAnnouncement,
                    onConsumed = { consumedMessage ->
                        if (currentAnnouncement == consumedMessage) {
                            currentAnnouncement = ""
                        }
                    }
                )
                ageSignalsMessage?.let { message ->
                    AgeSignalsDialog(
                        message = message,
                        onDismiss = { ageSignalsMessage = null }
                    )
                }
              }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        monarchController?.resume()
    }

    override fun onStop() {
        // Auto-pause when the app actually goes to the background or the screen locks.
        // This lives in onStop (not onPause) on purpose: onPause also fires on transient
        // window-focus loss — which TalkBack and external keyboards trigger constantly —
        // and pausing there made a freshly started game pop straight into the Pause cover.
        // Silent pause; the flourish is reserved for the explicit Pause button.
        viewModel.pauseGame(playSound = false)
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
        if (event.action == KeyEvent.ACTION_DOWN && handleGameKeyShortcut(event.keyCode)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    // External-keyboard accelerators for the game screen. We deliberately only claim
    // keys that focus traversal and text entry never use (Escape, Backspace/Delete),
    // so Tab/Shift+Tab navigation and Enter/Space activation of the focused control
    // keep working untouched. Returns true only when a shortcut was actually consumed.
    private fun handleGameKeyShortcut(keyCode: Int): Boolean {
        if (viewModel.screen != GameScreen.GAME || !viewModel.gameActive) return false
        return when (keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> {
                if (viewModel.gamePaused) viewModel.resumeGame() else viewModel.pauseGame()
                true
            }
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_FORWARD_DEL -> {
                if (viewModel.gamePaused) return false
                viewModel.clearSelection()
                true
            }
            else -> false
        }
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
        GameScreen.LOADING -> LoadingScreen(onReady = { viewModel.warmUpForPhone() })
        GameScreen.START -> StartScreen(viewModel)
        GameScreen.GAME -> GameScreen(viewModel)
        GameScreen.RESULTS -> ResultsScreen(viewModel)
    }
}

@Composable
private fun LoadingScreen(onReady: () -> Unit) {
    LaunchedEffect(Unit) { onReady() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WbBackground)
            .semantics { paneTitle = "Loading WordBopper" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clearAndSetSemantics {}
        )

        Spacer(modifier = Modifier.size(24.dp))

        Text(
            text = "Loading WordBopper…",
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Black,
            color = WbText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Loading WordBopper"
                }
        )
    }
}

@Composable
private fun AccessibilityAnnouncementHost(
    serial: Int,
    message: String,
    onConsumed: (String) -> Unit
) {
    if (message.isBlank()) return
    key(serial) {
        LaunchedEffect(serial, message) {
            delay(1_000)
            onConsumed(message)
        }
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

@Composable
private fun AgeSignalsDialog(message: AgeSignalsMessage, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(message.title) },
        text = { Text(message.body) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private data class AgeSignalsMessage(
    val title: String,
    val body: String
)

private fun AgeSignalsCheckResult.toMessage(): AgeSignalsMessage? {
    return when (this) {
        AgeSignalsCheckResult.Ready -> null
        AgeSignalsCheckResult.ResolveInPlayStore -> AgeSignalsMessage(
            title = "Google Play Age Status",
            body = "Google Play needs your age status to be resolved. You can keep using WordBopper, and you may need to visit Google Play to finish age assurance steps."
        )
        AgeSignalsCheckResult.ParentApprovalPending -> AgeSignalsMessage(
            title = "Parent Approval Pending",
            body = "Google Play reports that parent approval is pending. You can keep using WordBopper, and this status may need to be resolved in Google Play."
        )
        AgeSignalsCheckResult.ParentApprovalDenied -> AgeSignalsMessage(
            title = "Parent Approval Not Approved",
            body = "Google Play reports that parent approval was not approved. You can keep using WordBopper, and this status may need to be resolved in Google Play."
        )
    }
}
