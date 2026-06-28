package com.marconius.wordbopper.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marconius.wordbopper.model.BestGame
import com.marconius.wordbopper.model.BubbleColorTheme
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.BubbleTextColorOption
import com.marconius.wordbopper.model.DictionaryLanguage
import com.marconius.wordbopper.model.GameAnnouncementVerbosity
import com.marconius.wordbopper.model.GameMode
import com.marconius.wordbopper.model.GridSizeOption
import com.marconius.wordbopper.model.LanguageModeBestGame
import com.marconius.wordbopper.model.LetterPositionMode
import com.marconius.wordbopper.ui.theme.WbAccent1
import com.marconius.wordbopper.ui.theme.WbAccent2
import com.marconius.wordbopper.ui.theme.WbAccent5
import com.marconius.wordbopper.ui.theme.WbBackground
import com.marconius.wordbopper.ui.theme.WbMuted
import com.marconius.wordbopper.ui.theme.WbPanel
import com.marconius.wordbopper.ui.theme.WbSurface
import com.marconius.wordbopper.ui.theme.WbText
import com.marconius.wordbopper.ui.theme.bubbleFills
import com.marconius.wordbopper.ui.theme.bubbleTextColor
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
            .semantics { paneTitle = "WordBopper" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(top = 24.dp, bottom = 8.dp)
                .focusRequester(headingFocusRequester)
                .semantics(mergeDescendants = true) {
                    heading()
                }
                .focusable()
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
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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
                bestGame.languageModeBestGames
                    .filter { it.language != DictionaryLanguage.ENGLISH }
                    .sortedWith(compareBy<LanguageModeBestGame> { it.language.label }.thenBy { it.mode.ordinal })
                    .forEach { record ->
                        LanguageModeBestGameSection(record)
                    }
            }
        }
    }
}

@Composable
private fun LanguageModeBestGameSection(record: LanguageModeBestGame) {
    BestGameSection(
        title = record.heading,
        stats = if (record.mode == GameMode.BOPPLE) {
            listOf(
                Pair("Best score", "${record.highestScore}"),
                Pair("Longest word", record.longestWord.ifEmpty { "None yet" }),
                Pair("Most words", "${record.mostWords}")
            )
        } else {
            listOf(
                Pair(if (record.mode == GameMode.TIMED) "Highest score" else "Best score", "${record.highestScore}"),
                Pair("Longest word", record.longestWord.ifEmpty { "None yet" }),
                Pair("Most words", "${record.mostWords}"),
                Pair("Largest chain", "${record.largestLetterChain}")
            )
        }
    )
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
        "Tap letter bubbles anywhere on the grid to build words.",
        "Build words with at least 3 letters in a row that are next to each other in the grid to earn a chain bonus. Do this three times in a row to activate a timed 3x score multiplier.",
        "Hit Make Word to score. Hit Clear Letters to deselect all selected letters and get 15 seconds added to the timer in Timed mode.",
        "When BopAway is on, each letter you tap moves into the word tray and gets replaced right away. Hit Clear Word to erase the current word from the tray.",
        "Timed mode has 2 minutes on the clock, and letters change as you use them. Non-Stop mode turns off the timer and lets you Bop til you drop!",
        "For TalkBack users, use Explore by Touch or linear navigation to quickly navigate the grid.",
        "Using a keyboard? Press Tab to move between the bubbles and buttons, then Enter or Space to choose one. Press Backspace to clear your letters, and Escape to pause the game or, in Non-Stop mode, to open Game Options. Press Escape again to jump back in."
    )
    val boppleInstructions = listOf(
        "Words must be made up of letters that are next to each other in the grid.",
        "Letters stay in place after you make words.",
        "3 or 4 letter words score 1 point, 5 letters score 2, 6 letters score 3, 7 letters score 5, and 8 or more letters score 11.",
        "Play together with friends at the same time to see who can Bopple the best! All on their own devices, of course."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { SheetCloseButton(onDismiss = onDismiss) }
        item {
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
        }

        instructions.forEach { instruction ->
            item { InstructionRow(instruction) }
        }

        item {
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
        }

        boppleInstructions.forEach { instruction ->
            item { InstructionRow(instruction) }
        }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { SheetCloseButton(onDismiss = onDismiss) }
        item {
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
        }

        item {
            SettingsPickerBlock(title = "Game Mode") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    GameMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = vm.gameMode == mode,
                            onClick = { vm.setGameMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, GameMode.entries.size)
                        ) {
                            Text(mode.label)
                        }
                    }
                }
            }
        }
        item { SettingsDescription(vm.gameMode.settingsBlurb) }

        item {
            SettingsDropdown(
                title = "Grid Size",
                selectedLabel = vm.gridSizeOption.label,
                options = GridSizeOption.entries.map { option -> option.label to { vm.setGridSizeOption(option) } }
            )
        }
        item { SettingsDescription("Choose a smaller grid for a quicker, easier bop, or a larger grid for a bigger challenge. 5 by 5 is the classic size.") }

        item {
            SettingsDropdown(
                title = "Bubble Language",
                selectedLabel = vm.dictionaryLanguage.label,
                options = DictionaryLanguage.entries.map { language -> language.label to { vm.setDictionaryLanguage(language) } }
            )
        }
        item { SettingsDescription("Choose the language you want to Bop in. The rest of the app stays in English for now.") }

        item {
            SettingsDropdown(
                title = "Letter Positions",
                selectedLabel = vm.letterPositionMode.label,
                options = LetterPositionMode.entries.map { mode -> mode.label to { vm.setLetterPositionMode(mode) } }
            )
        }
        item { SettingsDescription(vm.letterPositionMode.settingsBlurb) }

        item { SettingsSectionLabel("Letter Phonetics") }

        item {
            SettingsToggleRow(
                title = "Speak Letter Phonetics",
                checked = vm.speakLetterPhonetics,
                onCheckedChange = { vm.setSpeakLetterPhonetics(it) }
            )
        }
        item { SettingsDescription("Adds the phonetic version of the bubble letters to the announcement, such as \"a, Alpha.\"") }

        item { SettingsSectionLabel("BopAway") }

        item {
            SettingsToggleRow(
                title = "BopAway",
                checked = vm.bopAway,
                onCheckedChange = { vm.setBopAway(it) }
            )
        }
        item { SettingsDescription("For an extra challenge, BopAway instantly moves each tapped letter into the word tray and replaces it with a new letter. If you clear the word, those letters are lost. Bop wisely!") }

        item {
            SettingsPickerBlock(title = "Bubble Letter Style") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    BubbleLetterStyle.entries.forEachIndexed { index, style ->
                        SegmentedButton(
                            selected = vm.bubbleLetterStyle == style,
                            onClick = { vm.setBubbleLetterStyle(style) },
                            shape = SegmentedButtonDefaults.itemShape(index, BubbleLetterStyle.entries.size)
                        ) {
                            Text(style.label, fontFamily = style.fontFamily)
                        }
                    }
                }
            }
        }
        item { SettingsDescription("Choose the letter shape that is easiest for you to read in the bubbles and word tray.") }

        item {
            SettingsPickerBlock(title = "Bubble Text Color") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    BubbleTextColorOption.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = vm.bubbleTextColorOption == option,
                            onClick = { vm.setBubbleTextColorOption(option) },
                            shape = SegmentedButtonDefaults.itemShape(index, BubbleTextColorOption.entries.size)
                        ) {
                            Text(option.label)
                        }
                    }
                }
            }
        }
        item { SettingsDescription("Pick your preference of light or dark text for the bubbles. Either option will still have colorful bubbles to bop!") }

        item {
            SettingsDropdown(
                title = "Bubble Color Theme",
                selectedLabel = vm.bubbleColorTheme.label,
                options = BubbleColorTheme.optionsFor(vm.bubbleTextColorOption)
                    .map { theme -> theme.label to { vm.setBubbleColorTheme(theme) } }
            )
        }
        item { SettingsDescription("Choose a bubble color set that feels good to play with. Every theme keeps the letter contrast strong.") }
        item {
            BubbleThemePreview(
                textColorOption = vm.bubbleTextColorOption,
                colorTheme = vm.bubbleColorTheme,
                letterStyle = vm.bubbleLetterStyle
            )
        }

        item { SettingsSectionLabel("Game Volume") }
        item {
            SettingsSliderRow(
                title = "Game Volume",
                value = vm.gameVolume,
                onValueChange = { vm.setGameVolume(it) }
            )
        }
        item { SettingsDescription("Set how loud the game sounds should be.") }

        item {
            SettingsPickerBlock(title = "Game Announcements") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    GameAnnouncementVerbosity.entries.forEachIndexed { index, verbosity ->
                        SegmentedButton(
                            selected = vm.gameAnnouncementVerbosity == verbosity,
                            onClick = { vm.setGameAnnouncementVerbosity(verbosity) },
                            shape = SegmentedButtonDefaults.itemShape(index, GameAnnouncementVerbosity.entries.size)
                        ) {
                            Text(verbosity.label)
                        }
                    }
                }
            }
        }
        item { SettingsDescription("Controls spoken game announcements for scoring, invalid words, and cleared letters.") }

        item { SettingsSectionLabel("Left-Handed Mode") }

        item {
            SettingsToggleRow(
                title = "Left-Handed Mode",
                checked = vm.leftHandedMode,
                onCheckedChange = { vm.setLeftHandedMode(it) }
            )
        }
        item { SettingsDescription("Mirrors gameplay controls for easier left-handed play.") }

        item { SettingsSectionLabel("Game Haptics") }

        item {
            SettingsToggleRow(
                title = "Game Haptics",
                checked = vm.gameHapticsEnabled,
                onCheckedChange = { vm.setGameHapticsEnabled(it) }
            )
        }
        item { SettingsDescription("Adds vibration feedback during gameplay.") }

        item {
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
    }

    if (showAbout) {
        ModalBottomSheet(
            onDismissRequest = { showAbout = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = WbBackground,
            dragHandle = null
        ) {
            AboutSheetContent(vm = vm) { showAbout = false }
        }
    }
}

@Composable
private fun BubbleThemePreview(
    textColorOption: BubbleTextColorOption,
    colorTheme: BubbleColorTheme,
    letterStyle: BubbleLetterStyle
) {
    val palette = bubbleFills(textColorOption, colorTheme)
    val textColor = bubbleTextColor(textColorOption)
    val sampleLetters = listOf("B", "O", "P")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(WbBackground)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            sampleLetters.forEachIndexed { index, letter ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(palette[index % palette.size]),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = letterStyle.fontFamily,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                }
            }
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
            .semantics { heading() }
    )
}

@Composable
private fun SettingsPickerBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = WbText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .semantics { heading() }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    title: String,
    selectedLabel: String,
    options: List<Pair<String, () -> Unit>>
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsPickerBlock(title = title) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
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
private fun SettingsSliderRow(title: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = WbText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheetContent(vm: GameViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" }
        catch (_: Exception) { "1.0" }
    }
    var showAcknowledgements by remember { mutableStateOf(false) }

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

        AboutLinkRow(label = "Report Missing Word") {
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

        AboutLinkRow(label = "Privacy Policy") {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://marconius.com/wbPrivacy/"))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable { showAcknowledgements = true }
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = "Acknowledgements"
                    onClick(label = "open") {
                        showAcknowledgements = true
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
                text = "▸",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = WbMuted
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

    if (showAcknowledgements) {
        ModalBottomSheet(
            onDismissRequest = { showAcknowledgements = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = WbBackground,
            dragHandle = null
        ) {
            AcknowledgementsSheetContent { showAcknowledgements = false }
        }
    }
}

@Composable
private fun AcknowledgementsSheetContent(onDismiss: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            SheetCloseButton(onDismiss = onDismiss)
        }
        item {
            Text(
                text = "Acknowledgements",
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
        }
        item {
            Text(
                text = "Massive thanks to the following developers and resources for our language word lists.",
                fontSize = 16.sp,
                lineHeight = 21.sp,
                color = WbText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        item {
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(top = 8.dp))
        }
        languageAcknowledgements.forEach { acknowledgement ->
            item {
                LanguageAcknowledgementDisclosure(acknowledgement)
            }
        }
    }
}

@Composable
private fun LanguageAcknowledgementDisclosure(acknowledgement: LanguageAcknowledgement) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { isExpanded = !isExpanded }
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = acknowledgement.language
                    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                    onClick(label = if (isExpanded) "collapse" else "expand") {
                        isExpanded = !isExpanded
                        true
                    }
                }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = acknowledgement.language,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = WbAccent5,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isExpanded) "▾" else "▸",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = WbMuted
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                acknowledgement.items.forEach { item ->
                    when (item) {
                        is AcknowledgementItem.Text -> AcknowledgementTextRow(item.text)
                        is AcknowledgementItem.Link -> AcknowledgementLinkRow(item.title, item.url)
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
    }
}

@Composable
private fun AcknowledgementTextRow(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = WbMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun AcknowledgementLinkRow(title: String, url: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = title
                onClick {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = WbAccent5
        )
    }
}

private data class LanguageAcknowledgement(
    val language: String,
    val items: List<AcknowledgementItem>
)

private sealed interface AcknowledgementItem {
    data class Text(val text: String) : AcknowledgementItem
    data class Link(val title: String, val url: String) : AcknowledgementItem
}

private val languageAcknowledgements = listOf(
    LanguageAcknowledgement(
        language = "English",
        items = listOf(
            AcknowledgementItem.Text("Word list copyright 2000-2026 by Kevin Atkinson."),
            AcknowledgementItem.Text("Permission to use, copy, modify, distribute, and sell any part of the English Speller Database (ESDB, previously known as SCOWLv2), or word lists created from it, is hereby granted without fee, provided that the above copyright notice appears in all copies and that both the above copyright notice and this notice appear in supporting documentation. Kevin Atkinson makes no representations about the suitability of this database for any purpose. It is provided \"as is\" without express or implied warranty."),
            AcknowledgementItem.Text("ESDB is derived from many sources, most of which are in the Public Domain. Data from the Corpus of Contemporary American English (COCA) was also used."),
            AcknowledgementItem.Text("More information about COCA is available at:"),
            AcknowledgementItem.Link("Corpus of Contemporary American English", "https://www.english-corpora.org/coca/"),
            AcknowledgementItem.Text("The primary source of words for ESDB comes from 12dicts and ENABLE2K. Both are in the Public Domain, but Alan Beale deserves special credit as the author of 12dicts and a major contributor to ENABLE2K."),
            AcknowledgementItem.Text("The English word list also includes words from the Wordnik Wordlist, an open-source word list for game developers."),
            AcknowledgementItem.Text("Wordnik Wordlist copyright 2020 Wordnik. The Wordnik Wordlist is made available under the MIT License. Permission is granted, free of charge, to use, copy, modify, merge, publish, distribute, sublicense, and sell copies, provided that the copyright notice and permission notice are included in copies or substantial portions of the software.")
        )
    ),
    LanguageAcknowledgement(
        language = "Spanish",
        items = listOf(
            AcknowledgementItem.Text("The Spanish word list is derived from Letterpress word lists made available under the Creative Commons CC0 1.0 Universal public domain dedication."),
            AcknowledgementItem.Link("Creative Commons CC0 1.0 Universal", "https://creativecommons.org/publicdomain/zero/1.0/")
        )
    ),
    LanguageAcknowledgement(
        language = "French",
        items = listOf(
            AcknowledgementItem.Text("The French word list is derived from Letterpress word lists made available under the Creative Commons CC0 1.0 Universal public domain dedication."),
            AcknowledgementItem.Link("Creative Commons CC0 1.0 Universal", "https://creativecommons.org/publicdomain/zero/1.0/")
        )
    ),
    LanguageAcknowledgement(
        language = "German",
        items = listOf(
            AcknowledgementItem.Text("The German word list is derived from Letterpress word lists made available under the Creative Commons CC0 1.0 Universal public domain dedication."),
            AcknowledgementItem.Link("Creative Commons CC0 1.0 Universal", "https://creativecommons.org/publicdomain/zero/1.0/")
        )
    ),
    LanguageAcknowledgement(
        language = "Dutch",
        items = listOf(
            AcknowledgementItem.Text("The Dutch word list is derived from the Dutch word list by OpenTaal."),
            AcknowledgementItem.Link("OpenTaal", "https://opentaal.org"),
            AcknowledgementItem.Text("OpenTaal makes the Dutch language files freely available under the Revised BSD License and/or the Creative Commons Attribution 3.0 Unported License."),
            AcknowledgementItem.Link("Revised BSD License", "https://opensource.org/licenses/BSD-3-Clause"),
            AcknowledgementItem.Link("Creative Commons Attribution 3.0 Unported License", "https://creativecommons.org/licenses/by/3.0/legalcode.txt"),
            AcknowledgementItem.Text("Dutch word list copyright 2020 OpenTaal; 2006-2011 OpenTaal; 2001-2005 Simon Brouwer and others; 1996 Nederlandstalige TeX Gebruikersgroep.")
        )
    ),
    LanguageAcknowledgement(
        language = "Italian",
        items = listOf(
            AcknowledgementItem.Text("The Italian word list includes words derived from Letterpress word lists made available under the Creative Commons CC0 1.0 Universal public domain dedication."),
            AcknowledgementItem.Link("Creative Commons CC0 1.0 Universal", "https://creativecommons.org/publicdomain/zero/1.0/"),
            AcknowledgementItem.Text("The Italian word list also includes forms derived from Morph-it!, a free morphological lexicon for the Italian language by Marco Baroni and Eros Zanchetta."),
            AcknowledgementItem.Text("Morph-it! is dual-licensed under the Creative Commons Attribution ShareAlike 2.0 License and the GNU Lesser General Public License. Morph-it! copyright 2004-2007 Marco Baroni and Eros Zanchetta."),
            AcknowledgementItem.Link("Creative Commons Attribution ShareAlike 2.0 License", "https://creativecommons.org/licenses/by-sa/2.0/"),
            AcknowledgementItem.Link("GNU Lesser General Public License", "https://www.gnu.org/licenses/lgpl-3.0.html")
        )
    ),
    LanguageAcknowledgement(
        language = "Brazilian Portuguese",
        items = listOf(
            AcknowledgementItem.Text("The Brazilian Portuguese word list is derived from the pythonprobr/palavras word list, which is based primarily on the LibreOffice Brazilian Portuguese spelling dictionary and made available under the Mozilla Public License 2.0."),
            AcknowledgementItem.Link("pythonprobr/palavras", "https://github.com/pythonprobr/palavras"),
            AcknowledgementItem.Link("Mozilla Public License 2.0", "https://www.mozilla.org/MPL/2.0/")
        )
    )
)

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
