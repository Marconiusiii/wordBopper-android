package com.marconius.wordbopper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.ui.theme.WbAccent1
import com.marconius.wordbopper.ui.theme.WbAccent4
import com.marconius.wordbopper.ui.theme.WbAccent5
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbPanel
import com.marconius.wordbopper.ui.theme.WbSurface

@Composable
fun ChainMeter(
    connectedWordStreak: Int,
    chainPowerUpActive: Boolean,
    chainPowerUpSecondsLeft: Int,
    chainMeterProgress: Double,
    chainMeterValue: String,
    modifier: Modifier = Modifier
) {
    val progressFraction by animateFloatAsState(
        targetValue = (chainMeterProgress / 3.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 100),
        label = "chain_progress"
    )

    val chainGradient = if (chainPowerUpActive)
        Brush.horizontalGradient(listOf(WbAccent1, WbAccent5, WbAccent4))
    else
        Brush.horizontalGradient(listOf(WbAccent5, WbAccent4))

    val displayText = if (chainPowerUpActive) "3x ${chainPowerUpSecondsLeft}s"
    else "$connectedWordStreak/3"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WbSurface)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clearAndSetSemantics {
                contentDescription = "Chained words"
                stateDescription = chainMeterValue
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Chained Words",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted
        )

        FractionBar(
            fraction = progressFraction,
            gradient = chainGradient,
            modifier = Modifier.weight(1f).height(8.dp)
        )

        Text(
            text = displayText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = WbAccent5,
            modifier = Modifier.widthIn(min = 44.dp)
        )
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun FractionBar(fraction: Float, gradient: Brush, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(WbPanel)
        )
        Layout(
            content = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(gradient)
                )
            }
        ) { measurables, constraints ->
            val filled = measurables.first().measure(
                constraints.copy(maxWidth = (constraints.maxWidth * fraction).toInt().coerceAtLeast(0))
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                filled.placeRelative(0, 0)
            }
        }
    }
}
