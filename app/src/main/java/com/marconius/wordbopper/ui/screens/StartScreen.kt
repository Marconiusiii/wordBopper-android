package com.marconius.wordbopper.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.model.BestGame
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.BubbleTextColorOption
import com.marconius.wordbopper.model.DictionaryLanguage
import com.marconius.wordbopper.model.GameAnnouncementVerbosity
import com.marconius.wordbopper.model.GameMode
import com.marconius.wordbopper.ui.theme.WbAccent1
import com.marconius.wordbopper.ui.theme.WbAccent2
import com.marconius.wordbopper.ui.theme.WbAccent5
import com.marconius.wordbopper.ui.theme.WbBackground
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbPanel
import com.marconius.wordbopper.ui.theme.WbSurface
import com.marconius.wordbopper.ui.theme.WbText
import com.marconius.wordbopper.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(vm: GameViewModel) {
    val headingFocusRequester = remember { FocusRequester() }
    var showInstructions by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headingFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WbBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(top = 24.dp, bottom = 8.dp)
                .focusRequester(headingFocusRequester)
                .semantics(mergeDescendants = true) {
                    heading()
                    traversalIndex = -1f
                }
        ) {
            Text(
                text = "WordBopper",
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Black,
                color = WbText
            )
            Text(
                text = "By Chancey Fleet and Marco Salsiccia",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WbMuted
            )
        }

        Row(modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)) {
            TextLinkButton(
                text = "How to Play",
                modifier = Modifier.weight(1f),
                onClick = { showInstructions = true }
            )
            TextLinkButton(
                text = "Game Settings",
                modifier = Modifier.weight(1f),
                onClick = { showSettings = true }
            )
        }

        StartGameButton(onClick = { vm.startGame() })

        Spacer(modifier = Modifier.height(16.dp))

        BestGameCard(bestGame = vm.bestGame)

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showInstructions) {
        ModalBottomSheet(
            onDismissRequest = { showInstructions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = WbBackground,
            dragHandle = null
        ) {
            InstructionsSheetContent { showInstructions = false }
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = WbBackground,
            dragHandle = null
        ) {
            GameSettingsSheetContent(vm = vm, onDismiss = { showSettings = false })
        }
    }
}

@Composable
private fun StartGameButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(WbAccent1, WbAccent2)))
            .clickable(onClickLabel = "Start game", onClick = onClick)
            .semantics { role = Role.Button },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Start Game",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun TextLinkButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = WbAccent5,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BestGameCard(bestGame: BestGame) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WbSurface)
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clickable {
                    isExpanded = !isExpanded
                }
                .clearAndSetSemantics {
                    heading()
                    contentDescription = "Your best game"
                    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                    role = Role.Button
                    onClick(label = if (isExpanded) "collapse" else "expand") {
                        isExpanded = !isExpanded
                        true
                    }
                }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your best game",
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Black,
                color = WbText,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isExpanded) "▾" else "▸",
                fontSize = 12.sp,
                color = WbMuted
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                BestGameSection(
                    title = "Timed Mode",
                    stats = listOf(
                        Pair("Highest score", "${bestGame.highestScore}"),
                        Pair("Longest word", bestGame.longestWord.ifEmpty { "None yet" }),
                        Pair("Most words", "${bestGame.mostWords}"),
                        Pair("Largest chain", "${bestGame.largestLetterChain}")
                    )
                )
                BestGameSection(
                    title = "Bopple Mode",
                    stats = listOf(
                        Pair("Best score", "${bestGame.highestBoppleScore}"),
                        Pair("Longest word", bestGame.longestBoppleWord.ifEmpty { "None yet" }),
                        Pair("Most words", "${bestGame.mostBoppleWords}")
                    )
                )
                BestGameSection(
                    title = "Non-Stop Mode",
                    stats = listOf(
                        Pair("Best score", "${bestGame.highestNonStopScore}"),
                        Pair("Longest word", bestGame.longestNonStopWord.ifEmpty { "None yet" }),
                        Pair("Most words", "${bestGame.mostNonStopWords}"),
                        Pair("Largest chain", "${bestGame.largestNonStopLetterChain}")
                    )
                )
            }
        }
    }
}

@Composable
private fun BestGameSection(title: String, stats: List<Pair<String, String>>) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = WbMuted,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .padding(horizontal = 14.dp)
            .semantics { heading() }
    )
    val chunked = stats.chunked(2)
    for (row in chunked) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for ((label, value) in row) {
                BestStatCell(label = label, value = value, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BestStatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clearAndSetSemantics {
                contentDescription = "$label: $value"
            }
    ) {
        Text(label, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = WbMuted)
        Text(value, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = WbText)
    }
}

@Composable
private fun InstructionsSheetContent(onDismiss: () -> Unit) {
    val instructions = listOf(
        "Tap letter bubbles anywhere on the 5 by 5 grid to build words.",
        "Build words with at least 3 letters in a row that are next to each other in the grid to earn a chain bonus. Do this three times in a row to activate a timed 3x score multiplier.",
        "Hit Make Word to score. Hit Clear Letters to deselect all selected letters and get 15 seconds added to the timer in Timed mode.",
        "When BopAway is on, each letter you tap moves into the word tray and gets replaced right away. Hit Clear Word to erase the current word from the tray.",
        "Timed mode has 2 minutes on the clock, and letters change as you use them. Non-Stop mode turns off the timer and lets you Bop til you drop!",
        "For TalkBack users, use Explore by Touch or linear navigation to quickly navigate the grid."
    )
    val boppleInstructions = listOf(
        "Words must be made up of letters that are next to each other in the grid.",
        "Letters stay in place after you make words.",
        "3 or 4 letter words score 1 point, 5 letters score 2, 6 letters score 3, 7 letters score 5, and 8 or more letters score 11.",
        "Play together with friends at the same time to see who can Bopple the best! All on their own devices, of course."
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SheetCloseButton(onDismiss = onDismiss)
        Text(
            text = "How to Play",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = WbText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .semantics { heading() }
        )

        instructions.forEach { InstructionRow(it) }

        Text(
            text = "Bopple",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = WbText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .semantics { heading() }
        )

        boppleInstructions.forEach { InstructionRow(it) }
    }
}

@Composable
private fun InstructionRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("•", color = WbAccent5, fontSize = 16.sp)
        Text(text, color = WbText, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameSettingsSheetContent(vm: GameViewModel, onDismiss: () -> Unit) {
    var showAbout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SheetCloseButton(onDismiss = onDismiss)
        Text(
            text = "Game Settings",
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Black,
            color = WbText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .semantics { heading() }
        )

        SettingsSectionLabel("Game Mode")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            GameMode.values().forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = vm.gameMode == mode,
                    onClick = { vm.setGameMode(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, GameMode.values().size)
                ) {
                    Text(mode.label)
                }
            }
        }
        SettingsDescription(vm.gameMode.settingsBlurb)

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 4.dp))

        SettingsDropdown(
            title = "Bubble Language",
            selectedLabel = vm.dictionaryLanguage.label,
            options = DictionaryLanguage.values().map { language -> language.label to { vm.setDictionaryLanguage(language) } }
        )
        SettingsDescription("Choose the language you want to Bop in. The rest of the app stays in English for now.")

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 4.dp))

        SettingsToggleRow(
            title = "Speak Letter Positions",
            checked = vm.speakLetterPositions,
            onCheckedChange = { vm.setSpeakLetterPositions(it) }
        )
        SettingsDescription("Adds Column and Row locations to the letters, like \"B, 2 5\" for Column 2, Row 5.")

        SettingsToggleRow(
            title = "Speak Letter Phonetics",
            checked = vm.speakLetterPhonetics,
            onCheckedChange = { vm.setSpeakLetterPhonetics(it) }
        )
        SettingsDescription("Adds the phonetic version of the bubble letters to the announcement, such as \"a, Alpha.\"")

        SettingsToggleRow(
            title = "BopAway",
            checked = vm.bopAway,
            onCheckedChange = { vm.setBopAway(it) }
        )
        SettingsDescription("For an extra challenge, BopAway instantly moves each bopped letter into the Word Tray and replaces it with a new letter in the grid. If you clear the word, those letters will be lost. Bop Wisely!")

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 4.dp))

        SettingsSectionLabel("Bubble Letter Style")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            BubbleLetterStyle.values().forEachIndexed { index, style ->
                SegmentedButton(
                    selected = vm.bubbleLetterStyle == style,
                    onClick = { vm.setBubbleLetterStyle(style) },
                    shape = SegmentedButtonDefaults.itemShape(index, BubbleLetterStyle.values().size)
                ) {
                    Text(style.label, fontFamily = style.fontFamily)
                }
            }
        }
        SettingsDescription("Choose the letter shape that is easiest for you to read in the bubbles and word tray.")

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 4.dp))

        SettingsSectionLabel("Bubble Text Color")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            BubbleTextColorOption.values().forEachIndexed { index, option ->
                SegmentedButton(
                    selected = vm.bubbleTextColorOption == option,
                    onClick = { vm.setBubbleTextColorOption(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, BubbleTextColorOption.values().size)
                ) {
                    Text(option.label)
                }
            }
        }
        SettingsDescription("Pick your preference of light or dark text for the bubbles. Either option will still have colorful bubbles to bop!")

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 4.dp))

        SettingsSectionLabel("Game Announcements")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            GameAnnouncementVerbosity.values().forEachIndexed { index, verbosity ->
                SegmentedButton(
                    selected = vm.gameAnnouncementVerbosity == verbosity,
                    onClick = { vm.setGameAnnouncementVerbosity(verbosity) },
                    shape = SegmentedButtonDefaults.itemShape(index, GameAnnouncementVerbosity.values().size)
                ) {
                    Text(verbosity.label)
                }
            }
        }
        SettingsDescription("Controls spoken game announcements for scoring, invalid words, and cleared letters.")

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable { showAbout = true }
                .semantics { role = Role.Button }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "About WordBopper",
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = WbAccent5
            )
        }
    }

    if (showAbout) {
        ModalBottomSheet(
            onDismissRequest = { showAbout = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = WbBackground,
            dragHandle = null
        ) {
            AboutSheetContent { showAbout = false }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = WbText,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    title: String,
    selectedLabel: String,
    options: List<Pair<String, () -> Unit>>
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsSectionLabel(title)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (label, action) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        action()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsDescription(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = WbMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable { onCheckedChange(!checked) }
            .clearAndSetSemantics {
                contentDescription = title
                stateDescription = if (checked) "On" else "Off"
                role = Role.Switch
                onClick(label = if (checked) "turn off" else "turn on") {
                    onCheckedChange(!checked)
                    true
                }
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = WbText,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WbBackground,
                checkedTrackColor = WbAccent5
            )
        )
    }
}

@Composable
private fun AboutSheetContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" }
        catch (_: Exception) { "1.0" }
    }
    var isAcknowledgementsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SheetCloseButton(onDismiss = onDismiss)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "About WordBopper",
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Black,
                color = WbText,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .padding(vertical = 16.dp)
                    .semantics { heading() }
            )

            Text(
                text = "Chancey wanted this game to exist and vibe coded the initial version, then passed it to Marco to refine it into the original web game. Marco then decided to rewrite the whole game for Android, and now here you are bopping away. Thanks for playing!",
                fontSize = 16.sp,
                lineHeight = 21.sp,
                color = WbText
            )

        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(top = 16.dp))

        AboutLinkRow(label = "Send Game Feedback") {
            val subject = Uri.encode("WordBopper Android Feedback")
            context.startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:marco@marconius.com?subject=$subject"))
            )
        }

        AboutLinkRow(label = "Privacy Policy") {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://marconius.com/wbPrivacy/"))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable { isAcknowledgementsExpanded = !isAcknowledgementsExpanded }
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = "Acknowledgements"
                    stateDescription = if (isAcknowledgementsExpanded) "Expanded" else "Collapsed"
                    onClick(label = if (isAcknowledgementsExpanded) "collapse" else "expand") {
                        isAcknowledgementsExpanded = !isAcknowledgementsExpanded
                        true
                    }
                }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Acknowledgements",
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = WbAccent5,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isAcknowledgementsExpanded) "▾" else "▸",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = WbMuted
            )
        }

        AnimatedVisibility(visible = isAcknowledgementsExpanded) {
            Text(
                text = """Word list copyright 2000-2026 by Kevin Atkinson.

Permission to use, copy, modify, distribute, and sell any part of the English Speller Database (ESDB, previously known as SCOWLv2), or word lists created from it, is hereby granted without fee, provided that the above copyright notice appears in all copies and that both the above copyright notice and this notice appear in supporting documentation. Kevin Atkinson makes no representations about the suitability of this database for any purpose. It is provided "as is" without express or implied warranty.

ESDB is derived from many sources, most of which are in the Public Domain. Data from the Corpus of Contemporary American English (COCA) was also used.

More information about COCA is available at https://www.english-corpora.org/coca/.

The primary source of words for ESDB comes from 12dicts and ENABLE2K. Both are in the Public Domain, but Alan Beale deserves special credit as the author of 12dicts and a major contributor to ENABLE2K.

The Spanish, French, and German word lists are derived from Letterpress word lists made available under the Creative Commons CC0 1.0 Universal public domain dedication.

The Italian word list includes words derived from Letterpress word lists made available under the Creative Commons CC0 1.0 Universal public domain dedication.

The Italian word list also includes forms derived from Morph-it!, a free morphological lexicon for the Italian language by Marco Baroni and Eros Zanchetta.""",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = WbMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

        Text(
            text = "© 2026 — WordBopper Version $versionName",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = WbMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun AboutLinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = label
                onClick { onClick(); true }
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = WbAccent5
        )
    }
}

@Composable
private fun SheetCloseButton(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(onClick = onDismiss)
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = "Close"
                    onClick { onDismiss(); true }
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Close",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = WbAccent5
            )
        }
    }
}
