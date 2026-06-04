package com.marconius.wordbopper

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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
                    message = currentAnnouncement
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
