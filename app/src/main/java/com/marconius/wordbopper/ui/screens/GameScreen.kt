package com.marconius.wordbopper.ui.screens

import android.content.Intent
import android.net.Uri
import java.util.UUID
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.booleanResource
import com.marconius.wordbopper.R
import com.marconius.wordbopper.ui.components.BubbleGrid
import com.marconius.wordbopper.ui.components.ChainMeter
import com.marconius.wordbopper.ui.components.StatsBar
import com.marconius.wordbopper.ui.components.WordTray
import com.marconius.wordbopper.ui.theme.WbAccent1
import com.marconius.wordbopper.ui.theme.WbAccent2
import com.marconius.wordbopper.ui.theme.WbAccent4
import com.marconius.wordbopper.ui.theme.WbAccent5
import com.marconius.wordbopper.ui.theme.WbBackground
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbPanel
import com.marconius.wordbopper.ui.theme.WbSurface
import com.marconius.wordbopper.ui.theme.WbText
import com.marconius.wordbopper.ui.theme.WbTimerGreen
import com.marconius.wordbopper.viewmodel.GameViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign

@Composable
fun GameScreen(vm: GameViewModel) {
    val selectedIds by remember { derivedStateOf {
        if (vm.bopAwayIsActive) emptySet<UUID>() else vm.selected.map { it.bubbleId }.toHashSet()
    } }

    BackHandler {
        // While paused, Back dismisses the pause cover (resume) rather than ending the
        // game; otherwise it ends the game as before.
        if (vm.gamePaused) vm.resumeGame() else vm.endGame()
    }

    val useLandscapeLayout = booleanResource(R.bool.use_landscape_game_layout)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WbBackground)
            .statusBarsPadding()
            .semantics { paneTitle = vm.gameplayHeading }
    ) {
        if (vm.gamePaused) {
            GamePauseCover(vm = vm)
        } else if (useLandscapeLayout) {
            LandscapeLayout(vm = vm, selectedIds = selectedIds)
        } else {
            PortraitLayout(vm = vm, selectedIds = selectedIds)
        }
    }
}

@Composable
private fun GamePauseCover(vm: GameViewModel) {
    val context = LocalContext.current
    val heading = vm.pauseHeading

    // Re-announce the paused screen whenever it is shown, including after returning
    // from the mail composer or backgrounding the app.
    LaunchedEffect(heading) {
        vm.announce(heading, includeInLowVerbosity = true)
    }

    // The cover sits above the board and swallows taps so sighted players can't peek
    // at the letters while paused. pointerInput consumes touches without adding any
    // semantics (a disabled clickable would surface a phantom focusable node to
    // TalkBack ahead of the heading).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WbBackground)
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .semantics { paneTitle = heading },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = heading,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black,
            color = WbText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .semantics {
                    heading()
                    traversalIndex = -1f
                }
        )

        PauseActionButton(
            title = "Resume Game",
            textColor = Color.Black,
            background = Brush.linearGradient(listOf(WbAccent5, WbAccent4)),
            onClick = { vm.resumeGame() }
        )

        PauseActionButton(
            title = "End Game",
            textColor = WbAccent2,
            background = Brush.linearGradient(listOf(WbAccent2.copy(alpha = 0.15f), WbAccent2.copy(alpha = 0.15f))),
            onClick = { vm.endGame() }
        )

        PauseActionButton(
            title = "Report Missing Word",
            textColor = WbText,
            background = Brush.linearGradient(listOf(WbPanel, WbPanel)),
            onClick = { reportMissingWord(context, vm) }
        )
    }
}

@Composable
private fun PauseActionButton(
    title: String,
    textColor: Color,
    background: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = title
                onClick { onClick(); true }
            }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(background)
                .padding(vertical = 20.dp, horizontal = 16.dp)
                .wrapContentHeight(Alignment.CenterVertically)
        )
    }
}

private fun reportMissingWord(context: android.content.Context, vm: GameViewModel) {
    val subject = Uri.encode("WordBopper Missing Word")
    val body = Uri.encode(
        """
        Missing word:

        Bubble Language: ${vm.dictionaryLanguage.label}
        Game Mode: ${vm.gameMode.label}

        Please include the missing word above. If you know the language or regional spelling details, feel free to add those too.
        """.trimIndent()
    )
    context.startActivity(
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:marco@marconius.com?subject=$subject&body=$body"))
    )
}

@Composable
private fun GameHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Black,
        color = WbText,
        modifier = modifier
            .background(WbSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics { heading() }
    )
}

// MARK: - Portrait

@Composable
private fun PortraitLayout(vm: GameViewModel, selectedIds: Set<UUID>) {
    Column(modifier = Modifier.fillMaxSize()) {
        GameHeading(text = vm.gameplayHeading, modifier = Modifier.fillMaxWidth())

        StatsBar(
            showsTimer = vm.showsTimer,
            formattedTime = vm.formattedTime,
            timerIsWarning = vm.timerIsWarning,
            score = vm.score,
            wordCount = vm.wordCount,
            headerAccessibilityLabel = vm.headerAccessibilityLabel,
            pauseButtonTitle = vm.pauseButtonTitle,
            pauseButtonAccessibilityLabel = vm.pauseButtonAccessibilityLabel,
            onPause = { vm.pauseGame() },
            leftHanded = vm.leftHandedMode
        )

        if (vm.gameMode != com.marconius.wordbopper.model.GameMode.BOPPLE) {
            ChainMeter(
                connectedWordStreak = vm.connectedWordStreak,
                chainPowerUpActive = vm.chainPowerUpActive,
                chainPowerUpSecondsLeft = vm.chainPowerUpSecondsLeft,
                chainMeterProgress = vm.chainMeterProgress,
                chainMeterValue = vm.chainMeterValue
            )
        }

        WordTray(
            selected = vm.selected,
            wordTrayLabel = vm.wordTrayLabel,
            letterStyle = vm.bubbleLetterStyle
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            val cellSize = minOf(maxWidth / vm.boardColumns, maxHeight / vm.boardRows).coerceAtLeast(44.dp)

            BubbleGrid(
                bubbles = vm.bubbles,
                selectedIds = selectedIds,
                cellSize = cellSize,
                columns = vm.boardColumns,
                rows = vm.boardRows,
                textColorOption = vm.bubbleTextColorOption,
                colorTheme = vm.bubbleColorTheme,
                letterStyle = vm.bubbleLetterStyle,
                dictionaryLanguage = vm.dictionaryLanguage,
                letterPositionMode = vm.letterPositionMode,
                speakLetterPhonetics = vm.speakLetterPhonetics,
                onTap = { vm.tapBubble(it) }
            )
        }

        PortraitActionBar(vm = vm)
    }
}

@Composable
private fun PortraitActionBar(vm: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WbSurface)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = Color.White.copy(alpha = 0.07f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
        ) {
            if (vm.leftHandedMode) {
                MakeWordButton(vm = vm, modifier = Modifier.weight(0.66f))
                ClearButton(vm = vm, modifier = Modifier.weight(0.34f))
            } else {
                ClearButton(vm = vm, modifier = Modifier.weight(0.34f))
                MakeWordButton(vm = vm, modifier = Modifier.weight(0.66f))
            }
        }
    }
}

@Composable
private fun ClearButton(vm: GameViewModel, modifier: Modifier = Modifier) {
    val clearLabel = vm.clearActionTitle
    Box(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clickable { vm.clearSelection() }
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = clearLabel
                onClick { vm.clearSelection(); true }
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clearLabel,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WbPanel)
                .padding(vertical = 18.dp, horizontal = 12.dp)
                .wrapContentHeight(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun MakeWordButton(vm: GameViewModel, modifier: Modifier = Modifier) {
    val makeWordEnabled = vm.makeWordEnabled
    Box(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clickable(enabled = makeWordEnabled) { vm.makeWord() }
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "Make Word"
                if (makeWordEnabled) onClick { vm.makeWord(); true } else disabled()
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Make Word",
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (makeWordEnabled) Color.Black else WbMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (makeWordEnabled)
                        Brush.linearGradient(listOf(WbAccent5, WbAccent4))
                    else
                        Brush.linearGradient(listOf(WbPanel, WbPanel))
                )
                .padding(vertical = 18.dp, horizontal = 12.dp)
                .wrapContentHeight(Alignment.CenterVertically)
        )
    }
}

// MARK: - Landscape

@Composable
private fun LandscapeLayout(vm: GameViewModel, selectedIds: Set<UUID>) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val controlsWidth = (maxWidth * 0.30f).coerceIn(220.dp, maxWidth * 0.36f)

        Row(modifier = Modifier.fillMaxSize()) {
            if (vm.leftHandedMode) {
                LandscapeControlPanel(
                    vm = vm,
                    modifier = Modifier
                        .width(controlsWidth)
                        .fillMaxHeight()
                )
                LandscapeGridPanel(
                    vm = vm,
                    selectedIds = selectedIds,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                LandscapeGridPanel(
                    vm = vm,
                    selectedIds = selectedIds,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                LandscapeControlPanel(
                    vm = vm,
                    modifier = Modifier
                        .width(controlsWidth)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun LandscapeGridPanel(
    vm: GameViewModel,
    selectedIds: Set<UUID>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(WbBackground)) {
        LandscapeHeading(text = vm.gameplayHeading, modifier = Modifier.fillMaxWidth())

        LandscapeWordTray(
            vm = vm,
            modifier = Modifier.fillMaxWidth()
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 3.dp, vertical = 3.dp)
        ) {
            val cellSize = minOf(maxWidth / vm.boardColumns, maxHeight / vm.boardRows).coerceAtLeast(44.dp)

            BubbleGrid(
                bubbles = vm.bubbles,
                selectedIds = selectedIds,
                cellSize = cellSize,
                columns = vm.boardColumns,
                rows = vm.boardRows,
                textColorOption = vm.bubbleTextColorOption,
                colorTheme = vm.bubbleColorTheme,
                letterStyle = vm.bubbleLetterStyle,
                dictionaryLanguage = vm.dictionaryLanguage,
                letterPositionMode = vm.letterPositionMode,
                speakLetterPhonetics = vm.speakLetterPhonetics,
                onTap = { vm.tapBubble(it) },
                modifier = Modifier.fillMaxSize(),
                rectangularCells = true
            )
        }
    }
}

@Composable
private fun LandscapeHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Black,
        color = WbText,
        modifier = modifier
            .background(WbSurface)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun LandscapeWordTray(vm: GameViewModel, modifier: Modifier = Modifier) {
    val displayWord = vm.currentWord.ifEmpty { "Word tray" }
    Text(
        text = displayWord,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (vm.currentWord.isEmpty()) WbMuted else WbAccent4,
        modifier = modifier
            .background(WbSurface)
            .heightIn(min = 34.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clearAndSetSemantics {
                contentDescription = vm.wordTrayLabel
            }
    )
    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun LandscapeControlPanel(vm: GameViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(WbSurface)
            .navigationBarsPadding()
    ) {
        LandscapePauseButton(
            title = vm.pauseButtonTitle,
            accessibilityLabel = vm.pauseButtonAccessibilityLabel,
            onPause = { vm.pauseGame() }
        )

        LandscapeStatsPanel(vm = vm)

        if (vm.gameMode != com.marconius.wordbopper.model.GameMode.BOPPLE) {
            ChainMeter(
                connectedWordStreak = vm.connectedWordStreak,
                chainPowerUpActive = vm.chainPowerUpActive,
                chainPowerUpSecondsLeft = vm.chainPowerUpSecondsLeft,
                chainMeterProgress = vm.chainMeterProgress,
                chainMeterValue = vm.chainMeterValue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ClearButton(vm = vm, modifier = Modifier.fillMaxWidth().weight(1f))
            MakeWordButton(vm = vm, modifier = Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun LandscapePauseButton(title: String, accessibilityLabel: String, onPause: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onPause)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = accessibilityLabel
            }
            .background(WbAccent2.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = WbAccent2,
            textAlign = TextAlign.Center
        )
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun LandscapeStatsPanel(vm: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clearAndSetSemantics {
                contentDescription = vm.headerAccessibilityLabel
            },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (vm.showsTimer) {
            LandscapeStatLine(
                label = "Time",
                value = vm.formattedTime,
                color = if (vm.timerIsWarning) WbAccent2 else WbTimerGreen
            )
        }
        LandscapeStatLine(label = "Score", value = "${vm.score}", color = WbAccent1)
        LandscapeStatLine(label = "Words", value = "${vm.wordCount}", color = WbAccent4)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun LandscapeStatLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 58.dp)
        )
    }
}
