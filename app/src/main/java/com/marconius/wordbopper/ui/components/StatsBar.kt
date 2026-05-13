package com.marconius.wordbopper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.ui.theme.WbAccent1
import com.marconius.wordbopper.ui.theme.WbAccent2
import com.marconius.wordbopper.ui.theme.WbAccent4
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbSurface
import com.marconius.wordbopper.ui.theme.WbTimerGreen

@Composable
fun StatsBar(
    showsTimer: Boolean,
    formattedTime: String,
    timerIsWarning: Boolean,
    score: Int,
    wordCount: Int,
    headerAccessibilityLabel: String,
    onEndGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WbSurface)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clearAndSetSemantics {
                    contentDescription = headerAccessibilityLabel
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showsTimer) {
                StatBlock(
                    label = "Time",
                    value = formattedTime,
                    color = if (timerIsWarning) WbAccent2 else WbTimerGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            StatBlock(
                label = "Score",
                value = "$score",
                color = WbAccent1,
                modifier = Modifier.weight(1f)
            )
            StatBlock(
                label = "Words",
                value = "$wordCount",
                color = WbAccent4,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .clickable(
                    onClickLabel = "End game",
                    onClick = onEndGame
                )
                .semantics { role = Role.Button }
                .background(WbAccent2.copy(alpha = 0.15f))
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "End Game",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WbAccent2
            )
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun StatBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}
