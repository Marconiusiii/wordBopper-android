package com.marconius.wordbopper.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.ui.theme.WbAccent1
import com.marconius.wordbopper.ui.theme.WbAccent2
import com.marconius.wordbopper.ui.theme.WbAccent3
import com.marconius.wordbopper.ui.theme.WbAccent4
import com.marconius.wordbopper.ui.theme.WbAccent5
import com.marconius.wordbopper.ui.theme.WbBackground
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbPanel
import com.marconius.wordbopper.ui.theme.WbSurface
import com.marconius.wordbopper.ui.theme.WbText
import com.marconius.wordbopper.viewmodel.GameViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role

@Composable
fun ResultsScreen(vm: GameViewModel) {
    val view = LocalView.current

    LaunchedEffect(Unit) {
        view.announceForAccessibility("Round Complete")
    }

    BackHandler {
        vm.goHome()
    }

    Column(modifier = Modifier.fillMaxSize().background(WbBackground)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Round Complete",
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Black,
                color = WbText,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    heading()
                    traversalIndex = -1f
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "${vm.score} points"
                }
            ) {
                Text(
                    text = "${vm.score}",
                    fontSize = 48.sp,
                    lineHeight = 54.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(WbAccent1, WbAccent3))
                    )
                )
                Text(text = "points", fontSize = 16.sp, lineHeight = 20.sp, color = WbMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultStat("${vm.wordCount}", "Words made", WbAccent4, Modifier.weight(1f))
                ResultStat("${vm.totalLettersUsed}", "Letters used", WbAccent5, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultStat(averageLength(vm), "Average length", WbAccent1, Modifier.weight(1f))
                ResultStat(longestWord(vm), "Longest word", WbAccent3, Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(WbSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your words",
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = WbText,
                    modifier = Modifier.semantics { heading() }
                )

                if (vm.madeWords.isEmpty()) {
                    Text(
                        text = "No words made — try again!",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = WbMuted
                    )
                } else {
                    vm.madeWords.forEach { word ->
                        Text(
                            text = word,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = WbText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(WbPanel)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(vertical = 6.dp, horizontal = 10.dp)
                                .semantics { contentDescription = word }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        ResultsActionBar(vm = vm)
    }
}

@Composable
private fun ResultsActionBar(vm: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WbBackground)
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        ActionButton(
            text = "Play Again",
            brush = Brush.linearGradient(listOf(WbAccent1, WbAccent2)),
            textColor = Color.Black,
            modifier = Modifier.weight(1f)
        ) { vm.startGame() }

        ActionButton(
            text = "Return Home",
            brush = Brush.linearGradient(listOf(WbPanel, WbPanel)),
            textColor = WbMuted,
            border = true,
            modifier = Modifier.weight(1f)
        ) { vm.goHome() }
    }
}

@Composable
private fun ActionButton(
    text: String,
    brush: Brush,
    textColor: Color,
    border: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(brush)
            .then(if (border) Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50)) else Modifier)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .heightIn(min = 56.dp)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clearAndSetSemantics {
        contentDescription = "$label: $value"
    },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted,
            textAlign = TextAlign.Center
        )
    }
}

private fun averageLength(vm: GameViewModel): String {
    if (vm.wordCount == 0) return "—"
    return "%.1f".format(vm.totalLettersUsed.toDouble() / vm.wordCount)
}

private fun longestWord(vm: GameViewModel): String =
    vm.madeWords.maxByOrNull { it.length } ?: "—"
