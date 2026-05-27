package com.marconius.wordbopper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.SelectedLetter
import com.marconius.wordbopper.ui.theme.WbAccent4
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbSurface

@Composable
fun WordTray(
    selected: List<SelectedLetter>,
    wordTrayLabel: String,
    letterStyle: BubbleLetterStyle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WbSurface)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clearAndSetSemantics {
                contentDescription = wordTrayLabel
            }
    ) {
        Text(
            text = "Word tray",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WbMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected.isEmpty()) {
                item {
                    Text(
                        text = "Your word appears here as you bop letters.",
                        fontSize = 14.sp,
                        color = WbMuted
                    )
                }
            } else {
                items(selected, key = { it.bubbleId }) { sel ->
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        LetterChip(letter = sel.letter, letterStyle = letterStyle)
                    }
                }
            }
        }
    }
    HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.06f), thickness = 1.dp)
}

@Composable
private fun LetterChip(letter: String, letterStyle: BubbleLetterStyle) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(WbAccent4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (letter == "ß") "ß" else letter.uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = letterStyle.fontFamily,
            color = androidx.compose.ui.graphics.Color.Black
        )
    }
}
