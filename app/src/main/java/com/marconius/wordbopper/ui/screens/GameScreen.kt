package com.marconius.wordbopper.ui.screens

import java.util.UUID
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign

@Composable
fun GameScreen(vm: GameViewModel) {
    val selectedIds by remember { derivedStateOf {
        if (vm.bopAwayIsActive) emptySet<UUID>() else vm.selected.map { it.bubbleId }.toHashSet()
    } }

    BackHandler {
        vm.endGame()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(WbBackground)
            .semantics { paneTitle = vm.gameplayHeading }
    ) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Controls panel takes ~30% of the width with a sensible minimum so its
        // touch targets and enlarged text stay comfortable; the grid takes the rest.
        val controlsWidth = (maxWidth * 0.30f).coerceIn(236.dp, maxWidth * 0.42f)

        Row(modifier = Modifier.fillMaxSize()) {
            if (vm.leftHandedMode) {
                LandscapeControlsPanel(vm = vm, modifier = Modifier.width(controlsWidth).fillMaxHeight())
                LandscapeGridPanel(
                    vm = vm,
                    selectedIds = selectedIds,
                    endGameOnLeft = false,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            } else {
                LandscapeGridPanel(
                    vm = vm,
                    selectedIds = selectedIds,
                    endGameOnLeft = true,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                LandscapeControlsPanel(vm = vm, modifier = Modifier.width(controlsWidth).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun LandscapeGridPanel(
    vm: GameViewModel,
    selectedIds: Set<UUID>,
    endGameOnLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(WbBackground)) {
        // Title row: heading plus the End Game button, with End Game hugging the
        // screen edge (the side away from the controls panel).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WbSurface)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heading: @Composable RowScope.() -> Unit = {
                GameHeading(
                    text = vm.gameplayHeading,
                    modifier = Modifier.weight(1f)
                )
            }
            val endGame: @Composable RowScope.() -> Unit = {
                LandscapeEndGameButton(onEndGame = { vm.endGame() })
            }
            if (endGameOnLeft) {
                endGame()
                heading()
            } else {
                heading()
                endGame()
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

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
    }
}

@Composable
private fun RowScope.LandscapeEndGameButton(onEndGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClickLabel = "End game", onClick = onEndGame)
            .semantics { role = Role.Button }
            .background(WbAccent2.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "End Game",
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = WbAccent2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LandscapeControlsPanel(vm: GameViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(WbSurface)) {
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

        // Clear on top, Make Word below — each fills the remaining height equally so
        // both stay large, easy targets in landscape.
        LandscapeClearButton(vm = vm, modifier = Modifier.fillMaxWidth().weight(1f))
        LandscapeMakeWordButton(vm = vm, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun LandscapeStatsPanel(vm: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WbSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clearAndSetSemantics {
                contentDescription = vm.headerAccessibilityLabel
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (vm.showsTimer) {
            LandscapeStatBlock(
                label = "Time",
                value = vm.formattedTime,
                color = if (vm.timerIsWarning) WbAccent2 else WbTimerGreen
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            LandscapeStatBlock(label = "Score", value = "${vm.score}", color = WbAccent1, modifier = Modifier.weight(1f))
            LandscapeStatBlock(label = "Words", value = "${vm.wordCount}", color = WbAccent4, modifier = Modifier.weight(1f))
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun LandscapeStatBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted
        )
        Text(
            text = value,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
private fun LandscapeClearButton(vm: GameViewModel, modifier: Modifier = Modifier) {
    val clearLabel = vm.clearActionTitle
    Box(
        modifier = modifier
            .heightIn(min = 64.dp)
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
private fun LandscapeMakeWordButton(vm: GameViewModel, modifier: Modifier = Modifier) {
    val makeWordEnabled = vm.makeWordEnabled
    Box(
        modifier = modifier
            .heightIn(min = 64.dp)
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
