package com.marconius.wordbopper.ui.screens

import java.util.UUID
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.marconius.wordbopper.ui.theme.WbAccent4
import com.marconius.wordbopper.ui.theme.WbAccent5
import com.marconius.wordbopper.ui.theme.WbBackground
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbPanel
import com.marconius.wordbopper.ui.theme.WbSurface
import com.marconius.wordbopper.ui.theme.WbText
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
        vm.endGame()
    }

    val useLandscapeLayout = booleanResource(R.bool.use_landscape_game_layout)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WbBackground)
            .semantics { paneTitle = vm.gameplayHeading }
    ) {
        if (useLandscapeLayout) {
            LandscapeLayout(vm = vm, selectedIds = selectedIds)
        } else {
            PortraitLayout(vm = vm, selectedIds = selectedIds)
        }
    }
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
            .semantics {
                heading()
                traversalIndex = -1f
            }
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
            onEndGame = { vm.endGame() },
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
                letterStyle = vm.bubbleLetterStyle,
                dictionaryLanguage = vm.dictionaryLanguage,
                speakLetterPositions = vm.speakLetterPositions,
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
    Column(modifier = Modifier.fillMaxSize()) {
        GameHeading(text = vm.gameplayHeading, modifier = Modifier.fillMaxWidth())

        StatsBar(
            showsTimer = vm.showsTimer,
            formattedTime = vm.formattedTime,
            timerIsWarning = vm.timerIsWarning,
            score = vm.score,
            wordCount = vm.wordCount,
            headerAccessibilityLabel = vm.headerAccessibilityLabel,
            onEndGame = { vm.endGame() },
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
                letterStyle = vm.bubbleLetterStyle,
                dictionaryLanguage = vm.dictionaryLanguage,
                speakLetterPositions = vm.speakLetterPositions,
                speakLetterPhonetics = vm.speakLetterPhonetics,
                onTap = { vm.tapBubble(it) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WbSurface)
                .navigationBarsPadding()
                .heightIn(min = 72.dp)
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
