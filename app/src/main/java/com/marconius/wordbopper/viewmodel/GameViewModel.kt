package com.marconius.wordbopper.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marconius.wordbopper.audio.AudioEngine
import com.marconius.wordbopper.data.DictionaryService
import com.marconius.wordbopper.data.GameplayAnnouncements
import com.marconius.wordbopper.haptics.HapticsEngine
import com.marconius.wordbopper.model.BestGame
import com.marconius.wordbopper.model.Bubble
import com.marconius.wordbopper.model.BubbleColorTheme
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.BubbleTextColorOption
import com.marconius.wordbopper.model.DailyBopEntry
import com.marconius.wordbopper.model.DailyBopLanguageStat
import com.marconius.wordbopper.model.DictionaryLanguage
import com.marconius.wordbopper.model.GameAnnouncementVerbosity
import com.marconius.wordbopper.model.GameMode
import com.marconius.wordbopper.model.GameScreen
import com.marconius.wordbopper.model.GridSizeOption
import com.marconius.wordbopper.model.LanguageModeBestGame
import com.marconius.wordbopper.model.LetterPositionMode
import com.marconius.wordbopper.model.SelectedLetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import kotlin.math.abs
import kotlin.math.min

class GameViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TIMED_GAME_DURATION = 120
        const val BOPPLE_GAME_DURATION = 180
        const val COLOR_COUNT = 8

        private val GAMEPLAY_HEADINGS = listOf(
            "Start bopping!", "Bop to it!", "Bop out some words!", "Bop those letters!",
            "Bop to the future!", "Start your bopping!", "Bop til you Drop!",
            "Bop All The Things!", "Bop to the Top!", "Commence bopping!"
        )

        private val BOPPLE_GAMEPLAY_HEADINGS = listOf(
            "The Boppler Effect", "Bopple Away!", "All the Bopples",
            "Boplift Your Vocabulary!", "The Bopple Exquisite", "The Bopple Bops Back"
        )

        private val DAILY_BOP_GAMEPLAY_HEADINGS = listOf(
            "Bop of the Day",
            "Today's Word Wants You",
            "Daily Bop, Daily Glory",
            "The Word Is Out There",
            "Hunt the Daily Bop",
            "Bop It Before Midnight",
            "Today's Bop Begins",
            "Chase the Daily Bop",
            "Bop the Day Away",
            "The Daily Word Beckons",
            "Find It, Bop It",
            "Your Daily Bop Awaits",
            "Bop on the Daily",
            "A Good Day to Bop",
            "Get the Big Bopper",
            "Boppin' 24/7"
        )
    }

    private val dictionary = DictionaryService.getInstance(application)
    private val prefs: SharedPreferences =
        application.getSharedPreferences("word_bopper", Context.MODE_PRIVATE)
    val audio = AudioEngine(viewModelScope, application)
    private val haptics = HapticsEngine(application)

    private val _announcementEvent = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val announcementEvent: SharedFlow<String> = _announcementEvent.asSharedFlow()

    // Navigation. Start on LOADING so the Home screen (and its Start Game button) does
    // not exist until warmUp() has finished all the heavy first-run work. This is what
    // prevents the first Start press from absorbing that cost.
    var screen by mutableStateOf(GameScreen.LOADING)
        private set

    // Settings (each has a paired setter that also persists)
    var gameMode by mutableStateOf(GameMode.TIMED)
        private set
    var letterPositionMode by mutableStateOf(LetterPositionMode.OFF)
        private set
    var speakLetterPhonetics by mutableStateOf(false)
        private set
    var bubbleTextColorOption by mutableStateOf(BubbleTextColorOption.DARK)
        private set
    var bubbleColorTheme by mutableStateOf(BubbleColorTheme.CLASSIC_BRIGHT)
        private set
    var bubbleLetterStyle by mutableStateOf(BubbleLetterStyle.PLAYFUL)
        private set
    var dictionaryLanguage by mutableStateOf(DictionaryLanguage.ENGLISH)
        private set
    var gameAnnouncementVerbosity by mutableStateOf(GameAnnouncementVerbosity.NORMAL)
        private set
    var bopAway by mutableStateOf(false)
        private set
    var gridSizeOption by mutableStateOf(GridSizeOption.FIVE)
        private set
    var leftHandedMode by mutableStateOf(false)
        private set
    var gameHapticsEnabled by mutableStateOf(true)
        private set
    var gameVolume by mutableStateOf(0.82f)
        private set

    // When the Monarch tactile display drives the game, the board is locked to the
    // display's fixed dimensions and the Grid Size preference must not override it.
    private var monarchBoardLocked = false

    // Game state
    val bubbles = mutableStateListOf<Bubble>()
    val selected = mutableStateListOf<SelectedLetter>()
    val madeWords = mutableStateListOf<String>()
    var boardColumns by mutableIntStateOf(5)
        private set
    var boardRows by mutableIntStateOf(5)
        private set
    var score by mutableIntStateOf(0)
        private set
    var wordCount by mutableIntStateOf(0)
        private set
    var totalLettersUsed by mutableIntStateOf(0)
        private set
    var secondsLeft by mutableIntStateOf(TIMED_GAME_DURATION)
        private set
    var gameActive by mutableStateOf(false)
        private set
    var gamePaused by mutableStateOf(false)
        private set
    var connectedWordStreak by mutableIntStateOf(0)
        private set
    var chainPowerUpActive by mutableStateOf(false)
        private set
    var chainPowerUpSecondsLeft by mutableIntStateOf(0)
        private set
    var largestLetterChain by mutableIntStateOf(0)
        private set
    var gameplayHeading by mutableStateOf(GAMEPLAY_HEADINGS[0])
        private set

    var bestGame by mutableStateOf(BestGame())
        private set

    var dailyBopTargetWord by mutableStateOf<String?>(null)
        private set
    var dailyBopTargetLanguage by mutableStateOf<DictionaryLanguage?>(null)
        private set
    var dailyBopFoundThisRound by mutableStateOf(false)
        private set
    var dailyBopBoostActive by mutableStateOf(false)
        private set
    var dailyBopBoostSecondsLeft by mutableIntStateOf(0)
        private set
    var dailyBopEntries by mutableStateOf<List<DailyBopEntry>>(emptyList())
        private set
    var dailyBopEntriesReady by mutableStateOf(false)
        private set
    var dailyBopEntriesLoading by mutableStateOf(false)
        private set
    var dailyBopEnabledLanguages by mutableStateOf<List<DictionaryLanguage>>(emptyList())
        private set

    private var timerJob: Job? = null
    private var powerUpTimerJob: Job? = null
    private var dailyBopTimerJob: Job? = null
    private var dailyBopEntriesJob: Job? = null
    private var dailyBopEntriesDateKey: String? = null
    private var startGameJob: Job? = null
    private val consumedBopAwayBubbleIds = mutableSetOf<UUID>()

    // Computed
    val bopAwayIsActive: Boolean get() = bopAway && gameMode != GameMode.BOPPLE
    val clearActionTitle: String get() = if (bopAwayIsActive) "Clear Word" else "Clear Letters"
    val currentWord: String get() = selected.joinToString("") { it.letter }
    val makeWordEnabled: Boolean get() = selected.size >= 3
    val showsTimer: Boolean get() = gameMode != GameMode.NON_STOP
    val timerIsWarning: Boolean get() = secondsLeft <= 20
    val speakLetterPositions: Boolean get() = letterPositionMode != LetterPositionMode.OFF

    val formattedTime: String
        get() {
            val m = secondsLeft / 60
            val s = secondsLeft % 60
            return "%d:%02d".format(m, s)
        }

    val wordTrayLabel: String
        get() = if (selected.isEmpty()) "Word tray, empty"
        else "Word tray: " + selected.joinToString(", ") { it.letter.lowercase() }

    val chainMeterValue: String
        get() = if (dailyBopBoostActive)
            "Daily Bop 3 times boost active, $dailyBopBoostSecondsLeft seconds left"
        else if (chainPowerUpActive)
            "3 times chain bop active"
        else "$connectedWordStreak of 3 chains"

    val chainMeterProgress: Double
        get() = if (dailyBopBoostActive) (dailyBopBoostSecondsLeft.toDouble() / 45.0) * 3.0
        else if (chainPowerUpActive) (chainPowerUpSecondsLeft.toDouble() / 15.0) * 3.0
        else connectedWordStreak.toDouble()

    val totalDailyBopsFound: Int
        get() = bestGame.dailyBopLanguageStats.sumOf { it.foundCount }

    val currentDailyBopRank: String
        get() = dailyBopRank(totalDailyBopsFound)

    val dailyBopStats: List<DailyBopLanguageStat>
        get() = bestGame.dailyBopLanguageStats
            .filter { it.foundCount > 0 }
            .sortedBy { it.language.label }

    val headerAccessibilityLabel: String
        get() = if (!showsTimer) "Score: $score, Words: $wordCount"
        else "Time: $formattedTime, Score: $score, Words: $wordCount"

    // Pause control labels. Non-Stop mode has no timer to pause, so it presents the
    // same overlay framed as "Game Options" instead of "Pause".
    val pauseButtonTitle: String get() = if (gameMode == GameMode.NON_STOP) "Options" else "Pause"
    val pauseButtonAccessibilityLabel: String
        get() = if (gameMode == GameMode.NON_STOP) "Game Options" else "Pause Game"
    val pauseHeading: String get() = if (gameMode == GameMode.NON_STOP) "Game Options" else "Game Paused"

    init {
        bestGame = loadBestGame()
        gameMode = loadGameMode()
        letterPositionMode = loadLetterPositionMode()
        speakLetterPhonetics = prefs.getBoolean("wordBopSpeakLetterPhonetics", false)
        bubbleTextColorOption = loadBubbleTextColorOption()
        bubbleColorTheme = loadBubbleColorTheme(bubbleTextColorOption)
        bubbleLetterStyle = loadBubbleLetterStyle()
        dictionaryLanguage = loadDictionaryLanguage()
        gameAnnouncementVerbosity = loadGameAnnouncementVerbosity()
        bopAway = prefs.getBoolean("wordBopBopAway", false)
        gridSizeOption = loadGridSizeOption()
        leftHandedMode = prefs.getBoolean("wordBopLeftHandedMode", false)
        gameHapticsEnabled = prefs.getBoolean("wordBopGameHapticsEnabled", true)
        haptics.isEnabled = gameHapticsEnabled
        gameVolume = loadGameVolume()
        audio.volume = gameVolume
        dailyBopEnabledLanguages = loadDailyBopEnabledLanguages(dictionaryLanguage)
        boardColumns = gridSizeOption.dimension
        boardRows = gridSizeOption.dimension
    }

    private var warmUpStarted = false

    // Cold-launch warm-up. Runs once, behind the Loading screen. Loads the dictionary,
    // primes the audio engine, and pre-builds a throwaway round so the gameplay code and
    // bubble objects are all initialized during loading rather than on the first real
    // Start press. Moves to the Home screen the instant that work finishes — no artificial
    // minimum — so the Loading screen vanishes as quickly as the app is actually ready.
    fun warmUp() {
        if (warmUpStarted) return
        warmUpStarted = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dictionary.preload(dictionaryLanguage)
                preloadDailyBopCandidates()
                audio.warmUp()
                audio.prepareDailyBopAnthemPreview()
                prebuildThrowawayRound()
            }
            prepareDailyBopEntries()
            screen = GameScreen.START
        }
    }

    fun warmUpForPhone() {
        if (warmUpStarted) return
        warmUpStarted = true
        audio.warmUp()

        val language = dictionaryLanguage
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dictionary.preload(language)
                preloadDailyBopCandidates()
                audio.prepareDailyBopAnthemPreview()
            }
            prepareDailyBopEntries()
            screen = GameScreen.START
        }
    }

    // Builds a full board off-screen and discards it, forcing first-run initialization of
    // the letter-generation and Bubble paths without showing anything to the user.
    private fun prebuildThrowawayRound() {
        val throwaway = ArrayList<Bubble>(boardColumns * boardRows)
        for (row in 0 until boardRows) {
            for (col in 0 until boardColumns) {
                throwaway.add(Bubble(letter = randomLetter(row, col), colorIndex = randomColor(), row = row, col = col))
            }
        }
        throwaway.clear()
    }

    // MARK: - Settings setters

    @JvmName("updateGameMode")
    fun setGameMode(mode: GameMode) {
        gameMode = mode
        prefs.edit().putString("wordBopGameMode", mode.name).apply()
    }

    @JvmName("updateSpeakLetterPositions")
    fun setLetterPositionMode(mode: LetterPositionMode) {
        letterPositionMode = mode
        prefs.edit()
            .putString("wordBopLetterPositionMode", mode.name)
            .putBoolean("wordBopSpeakLetterPositions", mode != LetterPositionMode.OFF)
            .apply()
    }

    @JvmName("updateSpeakLetterPhonetics")
    fun setSpeakLetterPhonetics(value: Boolean) {
        speakLetterPhonetics = value
        prefs.edit().putBoolean("wordBopSpeakLetterPhonetics", value).apply()
    }

    @JvmName("updateBubbleTextColorOption")
    fun setBubbleTextColorOption(option: BubbleTextColorOption) {
        bubbleTextColorOption = option
        if (!bubbleColorTheme.supports(option)) {
            bubbleColorTheme = BubbleColorTheme.defaultFor(option)
            prefs.edit().putString("wordBopBubbleColorTheme", bubbleColorTheme.name).apply()
        }
        prefs.edit().putString("wordBopBubbleTextColorOption", option.name).apply()
    }

    @JvmName("updateBubbleColorTheme")
    fun setBubbleColorTheme(theme: BubbleColorTheme) {
        bubbleColorTheme = if (theme.supports(bubbleTextColorOption)) {
            theme
        } else {
            BubbleColorTheme.defaultFor(bubbleTextColorOption)
        }
        prefs.edit().putString("wordBopBubbleColorTheme", bubbleColorTheme.name).apply()
    }

    @JvmName("updateBubbleLetterStyle")
    fun setBubbleLetterStyle(style: BubbleLetterStyle) {
        bubbleLetterStyle = style
        prefs.edit().putString("wordBopBubbleLetterStyle", style.name).apply()
    }

    @JvmName("updateDictionaryLanguage")
    fun setDictionaryLanguage(language: DictionaryLanguage) {
        if (gameActive) return
        dictionaryLanguage = language
        selected.clear()
        madeWords.clear()
        audio.resetSelectSound()
        prefs.edit().putString("wordBopDictionaryLanguage", language.name).apply()
        preloadDictionary(language)
        ensureDailyBopLanguageEnabled(language)
    }

    private fun preloadDictionary(language: DictionaryLanguage) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionary.preload(language)
        }
    }

    fun isDailyBopLanguageEnabled(language: DictionaryLanguage): Boolean {
        return normalizedDailyBopLanguages().contains(language)
    }

    fun setDailyBopLanguage(language: DictionaryLanguage, enabled: Boolean) {
        val languages = normalizedDailyBopLanguages().toMutableList()
        if (enabled) {
            if (!languages.contains(language)) languages.add(language)
        } else {
            if (languages.size <= 1) return
            languages.remove(language)
        }
        dailyBopEnabledLanguages = sortedDailyBopLanguages(languages)
        saveDailyBopEnabledLanguages()
        reloadDailyBopEntries()
    }

    @JvmName("updateGameAnnouncementVerbosity")
    fun setGameAnnouncementVerbosity(verbosity: GameAnnouncementVerbosity) {
        gameAnnouncementVerbosity = verbosity
        prefs.edit().putString("wordBopGameAnnouncementVerbosity", verbosity.name).apply()
    }

    @JvmName("updateBopAway")
    fun setBopAway(value: Boolean) {
        bopAway = value
        prefs.edit().putBoolean("wordBopBopAway", value).apply()
    }

    @JvmName("updateGridSizeOption")
    fun setGridSizeOption(option: GridSizeOption) {
        gridSizeOption = option
        prefs.edit().putInt("wordBopGridSize", option.dimension).apply()
        if (gameActive || monarchBoardLocked) return
        boardColumns = option.dimension
        boardRows = option.dimension
    }

    @JvmName("updateLeftHandedMode")
    fun setLeftHandedMode(value: Boolean) {
        leftHandedMode = value
        prefs.edit().putBoolean("wordBopLeftHandedMode", value).apply()
    }

    @JvmName("updateGameHapticsEnabled")
    fun setGameHapticsEnabled(value: Boolean) {
        gameHapticsEnabled = value
        haptics.isEnabled = value
        prefs.edit().putBoolean("wordBopGameHapticsEnabled", value).apply()
    }

    @JvmName("updateGameVolume")
    fun setGameVolume(value: Float) {
        gameVolume = value.coerceIn(0f, 1f)
        audio.volume = gameVolume
        prefs.edit().putFloat("wordBopGameVolume", gameVolume).apply()
    }

    // MARK: - Game lifecycle

    // Used by the Monarch tactile display to lock the board to its fixed dimensions,
    // overriding the Grid Size preference for the duration of the Monarch session.
    fun setBoardSize(columns: Int, rows: Int) {
        if (gameActive) return
        monarchBoardLocked = true
        boardColumns = columns.coerceIn(3, 8)
        boardRows = rows.coerceIn(3, 8)
    }

    fun startGame(dailyBopEntry: DailyBopEntry? = null) {
        if (gameActive || startGameJob?.isActive == true) return
        if (dailyBopEntry != null) {
            dictionaryLanguage = dailyBopEntry.language
            gameMode = GameMode.TIMED
            prefs.edit()
                .putString("wordBopDictionaryLanguage", dailyBopEntry.language.name)
                .putString("wordBopGameMode", GameMode.TIMED.name)
                .apply()
            ensureDailyBopLanguageEnabled(dailyBopEntry.language)
        }
        val language = dictionaryLanguage
        if (dictionary.isLoaded(language)) {
            beginGame(dailyBopEntry)
            return
        }
        startGameJob = viewModelScope.launch {
            if (!dictionary.isLoaded(language)) {
                withContext(Dispatchers.IO) {
                    dictionary.preload(language)
                }
            }
            beginGame(dailyBopEntry)
        }
    }

    private fun beginGame(dailyBopEntry: DailyBopEntry? = null) {
        if (gameActive) return
        bubbles.clear()
        selected.clear()
        madeWords.clear()
        score = 0
        wordCount = 0
        totalLettersUsed = 0
        secondsLeft = gameDuration
        gameActive = true
        gamePaused = false
        consumedBopAwayBubbleIds.clear()
        connectedWordStreak = 0
        chainPowerUpActive = false
        chainPowerUpSecondsLeft = 0
        stopDailyBopBoost(resetFound = true)
        dailyBopTargetWord = dailyBopEntry?.word
        dailyBopTargetLanguage = dailyBopEntry?.language
        dailyBopFoundThisRound = false
        largestLetterChain = 0
        gameplayHeading = randomGameplayHeading()
        haptics.roundStarted()

        if (!monarchBoardLocked) {
            boardColumns = gridSizeOption.dimension
            boardRows = gridSizeOption.dimension
        }

        for (row in 0 until boardRows) {
            for (col in 0 until boardColumns) {
                bubbles.add(Bubble(letter = randomLetter(row, col), colorIndex = randomColor(), row = row, col = col))
            }
        }

        screen = GameScreen.GAME
        if (dailyBopEntry != null) audio.playDailyBopIntroSound()
        else audio.playRoundStartSound(gameMode)
        if (showsTimer) startTimer()
    }

    fun playAgain() {
        val targetWord = dailyBopTargetWord
        val targetLanguage = dailyBopTargetLanguage
        if (!targetWord.isNullOrBlank() && targetLanguage != null) {
            startGame(DailyBopEntry(language = targetLanguage, word = targetWord))
        } else {
            startGame()
        }
    }

    fun pauseGame(playSound: Boolean = true) {
        if (!gameActive || gamePaused) return
        gamePaused = true
        stopTimer()
        pausePowerUpCountdown()
        pauseDailyBopBoost()
        if (playSound) audio.playPauseSound()
    }

    fun resumeGame() {
        if (!gameActive || !gamePaused) return
        gamePaused = false
        audio.playResumeSound()
        if (showsTimer) startTimer()
        if (chainPowerUpActive) startPowerUpCountdown(audioDelayMs = 550)
        if (dailyBopBoostActive) resumeDailyBopBoost(audioDelayMs = 550)
    }

    fun endGame() {
        if (!gameActive) return
        gameActive = false
        gamePaused = false
        stopTimer()
        stopPowerUpTimer()
        stopDailyBopBoost(resetFound = false)
        audio.playRoundEndSound()
        haptics.roundEnded()
        viewModelScope.launch {
            delay(850)
            showResults()
        }
    }

    private fun showResults() {
        updateBestGame()
        screen = GameScreen.RESULTS
    }

    fun goHome() {
        screen = GameScreen.START
    }

    fun isSelected(bubble: Bubble): Boolean {
        if (bopAwayIsActive) return false
        return selected.any { it.bubbleId == bubble.id }
    }

    // MARK: - Bubble interaction

    fun tapBubble(bubble: Bubble) {
        if (!gameActive || gamePaused) return
        if (bopAwayIsActive) {
            if (consumedBopAwayBubbleIds.contains(bubble.id)) return
            consumedBopAwayBubbleIds.add(bubble.id)
            selectBubble(bubble)
            replaceBubble(bubble.id)
            return
        }
        if (selected.any { it.bubbleId == bubble.id }) deselectBubble(bubble)
        else selectBubble(bubble)
    }

    private fun selectBubble(bubble: Bubble) {
        if (selected.isEmpty()) audio.resetSelectSound()
        selected.add(SelectedLetter(bubbleId = bubble.id, letter = bubble.letter, row = bubble.row, col = bubble.col))
        audio.playSelectSound()
        haptics.selectLetter()
    }

    private fun deselectBubble(bubble: Bubble) {
        selected.removeAll { it.bubbleId == bubble.id }
        audio.stepSelectSoundBack()
        audio.playDeselectSound()
        haptics.deselectLetter()
        if (selected.isEmpty()) audio.resetSelectSound()
    }

    fun clearSelection() {
        if (!gameActive || gamePaused || selected.isEmpty()) return
        selected.clear()
        audio.resetSelectSound()
        audio.playBonusSound()
        haptics.clearLetters()
        if (bopAwayIsActive) {
            announce(GameplayAnnouncements.WORD_CLEARED, includeInLowVerbosity = true)
        } else if (gameMode == GameMode.TIMED) {
            secondsLeft = min(secondsLeft + 15, gameDuration)
            announce(GameplayAnnouncements.CLEARED_WITH_TIME_BONUS, includeInLowVerbosity = true)
        } else {
            announce(GameplayAnnouncements.CLEARED, includeInLowVerbosity = true)
        }
    }

    // MARK: - Make word

    fun makeWord() {
        if (!gameActive || gamePaused || selected.size < 3) return
        val word = currentWord.lowercase()

        if (gameMode == GameMode.BOPPLE && calcChainBonus() == 0) {
            audio.playInvalidSound()
            haptics.invalidWord()
            resetChainStreak()
            selected.clear()
            audio.resetSelectSound()
            announce(GameplayAnnouncements.DISCONNECTED_BOPPLE_WORD, includeInLowVerbosity = true)
            return
        }

        if (!dictionary.contains(word, dictionaryLanguage)) {
            audio.playInvalidSound()
            haptics.invalidWord()
            resetChainStreak()
            selected.clear()
            audio.resetSelectSound()
            announce(GameplayAnnouncements.invalidWord(word), includeInLowVerbosity = true)
            return
        }

        if (gameMode == GameMode.BOPPLE && madeWords.contains(dictionary.normalized(word, dictionaryLanguage))) {
            audio.playInvalidSound()
            haptics.invalidWord()
            resetChainStreak()
            selected.clear()
            audio.resetSelectSound()
            announce(GameplayAnnouncements.duplicateWord(word), includeInLowVerbosity = true)
            return
        }

        val chainBonus = if (gameMode == GameMode.BOPPLE) 0 else calcChainBonus()
        val basePoints = calcScore(word) + chainBonus
        val dailyBopWasFound = isDailyBopWord(word)
        val dailyBopCanActivate = dailyBopWasFound && canActivateDailyBopBoostToday()
        val multiplier = if (gameMode == GameMode.BOPPLE) 1 else if (dailyBopBoostActive || chainPowerUpActive || dailyBopCanActivate) 3 else 1
        val points = basePoints * multiplier

        val scoredIds = selected.map { it.bubbleId }
        selected.clear()
        audio.resetSelectSound()

        if (gameMode != GameMode.BOPPLE && !bopAwayIsActive) {
            for (id in scoredIds) replaceBubble(id)
        }

        score += points
        wordCount += 1
        totalLettersUsed += word.length
        madeWords.add(dictionary.normalized(word, dictionaryLanguage))
        if (gameMode != GameMode.BOPPLE && chainBonus > largestLetterChain) largestLetterChain = chainBonus

        if (multiplier > 1) {
            if (chainPowerUpActive && !dailyBopBoostActive && !dailyBopCanActivate) {
                stopPowerUpTimer()
            }
            audio.playChainMultiplierScoreSound(word.length)
            haptics.powerUpScored()
        } else {
            audio.playWordSound(word.length)
            haptics.wordScored(word.length)
        }

        val dailyBopActivated = dailyBopCanActivate && activateDailyBopBoostIfNeeded()
        val powerUpActivated = if (gameMode == GameMode.BOPPLE || dailyBopActivated) false else updateChainStreak(chainBonus)

        announce(
            GameplayAnnouncements.scoredWord(
                word = word,
                points = points,
                chainBonus = chainBonus,
                multiplier = multiplier,
                powerUpActivated = powerUpActivated,
                verbosity = gameAnnouncementVerbosity,
                dailyBopActivated = dailyBopActivated
            ),
            includeInLowVerbosity = true
        )
    }

    // MARK: - Scoring

    private fun calcScore(word: String): Int {
        if (gameMode == GameMode.BOPPLE) return calcBoppleScore(word)
        var pts = word.length
        if (word.length >= 5) pts += word.length
        if (word.length >= 7) pts += word.length * 2
        return pts
    }

    private fun calcBoppleScore(word: String): Int = when (word.length) {
        3, 4 -> 1
        5 -> 2
        6 -> 3
        7 -> 5
        else -> 11
    }

    private fun calcChainBonus(): Int {
        if (selected.size < 3) return 0
        val longestRun = longestConnectedRunLength()
        return if (longestRun >= 3) longestRun else 0
    }

    private fun longestConnectedRunLength(): Int {
        var longest = 1
        var current = 1
        for ((previous, next) in selected.zipWithNext()) {
            if (areTouching(previous, next)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun areTouching(a: SelectedLetter, b: SelectedLetter): Boolean {
        val dr = abs(a.row - b.row)
        val dc = abs(a.col - b.col)
        return dr <= 1 && dc <= 1 && (dr + dc) > 0
    }

    // MARK: - Chain streak

    private fun updateChainStreak(chainBonus: Int): Boolean {
        if (chainBonus <= 0) { resetChainStreak(); return false }
        connectedWordStreak += 1
        audio.playConnectedWordSound(chainBonus)
        audio.playChainStreakSound(connectedWordStreak)
        haptics.chainWord()
        if (connectedWordStreak >= 3) { activatePowerUp(); return true }
        return false
    }

    private fun resetChainStreak() {
        if (chainPowerUpActive) return
        connectedWordStreak = 0
    }

    private fun activatePowerUp() {
        connectedWordStreak = 0
        chainPowerUpActive = true
        chainPowerUpSecondsLeft = 15
        haptics.powerUpActivated()
        startPowerUpCountdown()
    }

    // Starts (or restarts, after a pause) the chain power-up countdown for whatever
    // time remains. audioDelayMs lets the resume flourish breathe before the chimes
    // come back in, matching the iOS resume behavior.
    private fun startPowerUpCountdown(audioDelayMs: Long = 0) {
        if (!chainPowerUpActive || chainPowerUpSecondsLeft <= 0) return
        powerUpTimerJob?.cancel()
        powerUpTimerJob = viewModelScope.launch {
            if (audioDelayMs > 0) delay(audioDelayMs)
            if (!chainPowerUpActive || gamePaused) return@launch
            audio.startPowerUpChimes(chainPowerUpSecondsLeft.toDouble())
            while (chainPowerUpSecondsLeft > 0) {
                delay(1000)
                chainPowerUpSecondsLeft--
                if (chainPowerUpSecondsLeft <= 0) { stopPowerUpTimer(); break }
            }
        }
    }

    private fun pausePowerUpCountdown() {
        powerUpTimerJob?.cancel()
        powerUpTimerJob = null
        audio.stopPowerUpChimes()
    }

    private fun stopPowerUpTimer() {
        chainPowerUpActive = false
        chainPowerUpSecondsLeft = 0
        connectedWordStreak = 0
        powerUpTimerJob?.cancel()
        powerUpTimerJob = null
        audio.stopPowerUpChimes()
    }

    // MARK: - Daily Bop

    fun prepareDailyBopEntries() {
        val dateKey = dailyBopDateKey()
        if (dailyBopEntriesReady && dailyBopEntriesDateKey == dateKey) return
        if (dailyBopEntriesLoading && dailyBopEntriesDateKey == dateKey) return
        val languages = normalizedDailyBopLanguages()
        dailyBopEntriesJob?.cancel()
        dailyBopEntriesReady = false
        dailyBopEntriesLoading = true
        dailyBopEntriesDateKey = dateKey
        dailyBopEntriesJob = viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) {
                preloadDailyBopCandidates(languages)
                languages.mapNotNull { language ->
                    val word = dictionary.dailyWord(language)
                    if (word.isBlank()) null else DailyBopEntry(language, word)
                }
            }
            dailyBopEntries = entries
            dailyBopEntriesLoading = false
            dailyBopEntriesReady = true
        }
    }

    fun dailyBopWasFoundToday(language: DictionaryLanguage): Boolean {
        val dateKey = dailyBopDateKey()
        return bestGame.dailyBopLanguageStats.any {
            it.language == language && it.lastFoundDateKey == dateKey
        }
    }

    private fun reloadDailyBopEntries() {
        dailyBopEntriesJob?.cancel()
        dailyBopEntries = emptyList()
        dailyBopEntriesReady = false
        dailyBopEntriesLoading = false
        dailyBopEntriesDateKey = null
        prepareDailyBopEntries()
    }

    private fun preloadDailyBopCandidates(languages: List<DictionaryLanguage> = normalizedDailyBopLanguages()) {
        languages.forEach { language ->
            dictionary.preloadDailyBopCandidates(language)
        }
    }

    private fun ensureDailyBopLanguageEnabled(language: DictionaryLanguage) {
        val languages = normalizedDailyBopLanguages().toMutableList()
        if (languages.contains(language)) return
        languages.add(language)
        dailyBopEnabledLanguages = sortedDailyBopLanguages(languages)
        saveDailyBopEnabledLanguages()
        reloadDailyBopEntries()
    }

    private fun normalizedDailyBopLanguages(): List<DictionaryLanguage> {
        val saved = dailyBopEnabledLanguages.filter { DictionaryLanguage.entries.contains(it) }
        val languages = saved.ifEmpty { listOf(DictionaryLanguage.ENGLISH, dictionaryLanguage).distinct() }
        return sortedDailyBopLanguages(languages)
    }

    private fun sortedDailyBopLanguages(languages: Collection<DictionaryLanguage>): List<DictionaryLanguage> {
        return languages.distinct().sortedBy { DictionaryLanguage.entries.indexOf(it) }
    }

    private fun isDailyBopWord(word: String): Boolean {
        val targetWord = dailyBopTargetWord ?: return false
        val targetLanguage = dailyBopTargetLanguage ?: return false
        if (targetLanguage != dictionaryLanguage) return false
        return dictionary.normalized(word, dictionaryLanguage) == targetWord
    }

    private fun canActivateDailyBopBoostToday(): Boolean {
        if (dailyBopFoundThisRound) return false
        val language = dailyBopTargetLanguage ?: return false
        return !dailyBopWasFoundToday(language)
    }

    private fun activateDailyBopBoostIfNeeded(): Boolean {
        if (dailyBopFoundThisRound) return false
        val language = dailyBopTargetLanguage ?: return false
        if (dailyBopWasFoundToday(language)) return false
        dailyBopFoundThisRound = true
        recordDailyBopFound(language)
        pausePowerUpCountdown()
        dailyBopBoostActive = true
        dailyBopBoostSecondsLeft = 45
        haptics.powerUpActivated()
        resumeDailyBopBoost()
        return true
    }

    private fun pauseDailyBopBoost() {
        dailyBopTimerJob?.cancel()
        dailyBopTimerJob = null
        audio.stopDailyBopAnthem()
    }

    private fun resumeDailyBopBoost(audioDelayMs: Long = 0) {
        if (!dailyBopBoostActive || dailyBopBoostSecondsLeft <= 0) return
        dailyBopTimerJob?.cancel()
        dailyBopTimerJob = viewModelScope.launch {
            if (audioDelayMs > 0) delay(audioDelayMs)
            if (!dailyBopBoostActive || gamePaused) return@launch
            audio.playDailyBopAnthem()
            while (dailyBopBoostSecondsLeft > 0) {
                delay(1000)
                dailyBopBoostSecondsLeft--
                if (dailyBopBoostSecondsLeft <= 0) {
                    stopDailyBopBoost(resetFound = false)
                    break
                }
            }
        }
    }

    private fun stopDailyBopBoost(resetFound: Boolean) {
        dailyBopBoostActive = false
        dailyBopBoostSecondsLeft = 0
        dailyBopTimerJob?.cancel()
        dailyBopTimerJob = null
        audio.stopDailyBopAnthem()
        if (resetFound) dailyBopFoundThisRound = false
        if (chainPowerUpActive && !gamePaused && gameActive) {
            startPowerUpCountdown(audioDelayMs = 200)
        }
    }

    private fun recordDailyBopFound(language: DictionaryLanguage) {
        val dateKey = dailyBopDateKey()
        val stats = bestGame.dailyBopLanguageStats.toMutableList()
        val index = stats.indexOfFirst { it.language == language }
        if (index >= 0) {
            val stat = stats[index].copy()
            if (stat.lastFoundDateKey == dateKey) return
            stat.foundCount += 1
            stat.lastFoundDateKey = dateKey
            stats[index] = stat
        } else {
            stats.add(DailyBopLanguageStat(language = language, foundCount = 1, lastFoundDateKey = dateKey))
        }
        bestGame = bestGame.copy(dailyBopLanguageStats = stats)
        saveBestGame()
    }

    private fun dailyBopDateKey(calendar: Calendar = Calendar.getInstance()): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "%04d%02d%02d".format(year, month, day)
    }

    private fun dailyBopRank(count: Int): String {
        val ranks = listOf(
            "WordBopper Newbie",
            "Bubble Scout",
            "Bop Cadet",
            "Word Wrangler",
            "Bopologist",
            "Bubble Captain",
            "Grid Maestro",
            "Daily Bop Dynamo",
            "Word Wizard",
            "Bop Commander",
            "Letter Legend",
            "Bop Supreme",
            "Vocabulary Virtuoso",
            "Daily Bop Champion",
            "Grand Bopmaster"
        )
        return ranks[min(count / 10, ranks.lastIndex)]
    }

    // MARK: - Bubble management

    private fun replaceBubble(id: UUID) {
        val idx = bubbles.indexOfFirst { it.id == id }
        if (idx < 0) return
        val old = bubbles[idx]
        bubbles[idx] = Bubble(
            letter = randomLetter(old.row, old.col, replacingId = old.id),
            colorIndex = randomColor(),
            row = old.row,
            col = old.col
        )
    }

    private fun randomLetter(row: Int, col: Int, replacingId: UUID? = null): String {
        repeat(12) {
            val candidate = randomLetterCandidate()
            if (!hasAdjacentLetter(candidate, row, col, replacingId)) return candidate
        }

        val adjacentLetters = bubbles
            .filter { bubble ->
                bubble.id != replacingId &&
                    abs(bubble.row - row) <= 1 &&
                    abs(bubble.col - col) <= 1 &&
                    (bubble.row != row || bubble.col != col)
            }
            .map { it.letter }
            .toSet()
        val fallbackPool = dictionaryLanguage.letterPool
            .filterNot { it in adjacentLetters }
            .ifEmpty { dictionaryLanguage.letterPool }
        return randomDailyBopLetter()
            ?.takeIf { it in fallbackPool && (0 until 100).random() < 16 }
            ?: fallbackPool.random()
    }

    private fun randomLetterCandidate(): String {
        val dailyBopLetter = randomDailyBopLetter()
        if (dailyBopLetter != null && (0 until 100).random() < 16) {
            return dailyBopLetter
        }
        return dictionaryLanguage.letterPool.random()
    }

    private fun randomDailyBopLetter(): String? {
        val targetWord = dailyBopTargetWord ?: return null
        if (dailyBopTargetLanguage != dictionaryLanguage) return null
        val letters = targetWord.map { it.toString() }.filter { it.isNotBlank() }
        return letters.randomOrNull()
    }

    private fun hasAdjacentLetter(letter: String, row: Int, col: Int, replacingId: UUID?): Boolean {
        return bubbles.any { bubble ->
            bubble.id != replacingId &&
                bubble.letter == letter &&
                abs(bubble.row - row) <= 1 &&
                abs(bubble.col - col) <= 1 &&
                (bubble.row != row || bubble.col != col)
        }
    }

    private fun randomColor() = (0 until COLOR_COUNT).random()
    private fun randomGameplayHeading(): String {
        if (dailyBopTargetWord != null) return DAILY_BOP_GAMEPLAY_HEADINGS.random()
        if (gameMode == GameMode.BOPPLE) return BOPPLE_GAMEPLAY_HEADINGS.random()
        return GAMEPLAY_HEADINGS.random()
    }

    private val gameDuration: Int
        get() = when (gameMode) {
            GameMode.TIMED, GameMode.NON_STOP -> TIMED_GAME_DURATION
            GameMode.BOPPLE -> BOPPLE_GAME_DURATION
        }

    // MARK: - Timer

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (gameActive) {
                delay(1000)
                secondsLeft--
                if (secondsLeft in 1..10) audio.playTickSound(secondsLeft)
                if (secondsLeft <= 0) { endGame(); break }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // MARK: - Announcements

    fun announce(message: String, includeInLowVerbosity: Boolean = false) {
        if (gameAnnouncementVerbosity == GameAnnouncementVerbosity.OFF) return
        if (gameAnnouncementVerbosity == GameAnnouncementVerbosity.LOW && !includeInLowVerbosity) return
        viewModelScope.launch {
            _announcementEvent.emit(message)
        }
    }

    // MARK: - Persistence

    private fun loadBestGame() = BestGame(
        highestScore = prefs.getInt("bg_highestScore", 0),
        highestBoppleScore = prefs.getInt("bg_highestBoppleScore", 0),
        highestNonStopScore = prefs.getInt("bg_highestNonStopScore", 0),
        longestWord = prefs.getString("bg_longestWord", "") ?: "",
        longestBoppleWord = prefs.getString("bg_longestBoppleWord", "") ?: "",
        longestNonStopWord = prefs.getString("bg_longestNonStopWord", "") ?: "",
        mostWords = prefs.getInt("bg_mostWords", 0),
        mostBoppleWords = prefs.getInt("bg_mostBoppleWords", 0),
        mostNonStopWords = prefs.getInt("bg_mostNonStopWords", 0),
        largestLetterChain = prefs.getInt("bg_largestLetterChain", 0),
        largestBoppleLetterChain = prefs.getInt("bg_largestBoppleLetterChain", 0),
        largestNonStopLetterChain = prefs.getInt("bg_largestNonStopLetterChain", 0),
        languageModeBestGames = loadLanguageModeBestGames(),
        dailyBopLanguageStats = loadDailyBopLanguageStats()
    )

    private fun saveBestGame() {
        prefs.edit().run {
            putInt("bg_highestScore", bestGame.highestScore)
            putInt("bg_highestBoppleScore", bestGame.highestBoppleScore)
            putInt("bg_highestNonStopScore", bestGame.highestNonStopScore)
            putString("bg_longestWord", bestGame.longestWord)
            putString("bg_longestBoppleWord", bestGame.longestBoppleWord)
            putString("bg_longestNonStopWord", bestGame.longestNonStopWord)
            putInt("bg_mostWords", bestGame.mostWords)
            putInt("bg_mostBoppleWords", bestGame.mostBoppleWords)
            putInt("bg_mostNonStopWords", bestGame.mostNonStopWords)
            putInt("bg_largestLetterChain", bestGame.largestLetterChain)
            putInt("bg_largestBoppleLetterChain", bestGame.largestBoppleLetterChain)
            putInt("bg_largestNonStopLetterChain", bestGame.largestNonStopLetterChain)
            putString("bg_languageModeBestGames", encodeLanguageModeBestGames(bestGame.languageModeBestGames))
            putString("bg_dailyBopLanguageStats", encodeDailyBopLanguageStats(bestGame.dailyBopLanguageStats))
        }.apply()
    }

    private fun updateBestGame() {
        val longest = madeWords.maxByOrNull { it.length } ?: ""
        var changed = false
        val bg = bestGame.copy()
        if (dictionaryLanguage == DictionaryLanguage.ENGLISH) {
            when (gameMode) {
                GameMode.TIMED -> {
                    if (score > bg.highestScore) { bg.highestScore = score; changed = true }
                    if (longest.isNotEmpty() && longest.length >= bg.longestWord.length) { bg.longestWord = longest; changed = true }
                    if (wordCount > bg.mostWords) { bg.mostWords = wordCount; changed = true }
                    if (largestLetterChain > bg.largestLetterChain) { bg.largestLetterChain = largestLetterChain; changed = true }
                }
                GameMode.BOPPLE -> {
                    if (score > bg.highestBoppleScore) { bg.highestBoppleScore = score; changed = true }
                    if (longest.isNotEmpty() && longest.length >= bg.longestBoppleWord.length) { bg.longestBoppleWord = longest; changed = true }
                    if (wordCount > bg.mostBoppleWords) { bg.mostBoppleWords = wordCount; changed = true }
                }
                GameMode.NON_STOP -> {
                    if (score > bg.highestNonStopScore) { bg.highestNonStopScore = score; changed = true }
                    if (longest.isNotEmpty() && longest.length >= bg.longestNonStopWord.length) { bg.longestNonStopWord = longest; changed = true }
                    if (wordCount > bg.mostNonStopWords) { bg.mostNonStopWords = wordCount; changed = true }
                    if (largestLetterChain > bg.largestNonStopLetterChain) { bg.largestNonStopLetterChain = largestLetterChain; changed = true }
                }
            }
        }
        val languageModeUpdate = updateLanguageModeBestGame(bg, longest)
        if (languageModeUpdate.changed) {
            bg.languageModeBestGames = languageModeUpdate.records
            changed = true
        }
        if (changed) { bestGame = bg; saveBestGame() }
    }

    private data class LanguageModeUpdate(
        val records: List<LanguageModeBestGame>,
        val changed: Boolean
    )

    private fun updateLanguageModeBestGame(
        bestGame: BestGame,
        longest: String
    ): LanguageModeUpdate {
        if (dictionaryLanguage == DictionaryLanguage.ENGLISH) {
            return LanguageModeUpdate(bestGame.languageModeBestGames, false)
        }

        val records = bestGame.languageModeBestGames.toMutableList()
        val index = records.indexOfFirst { it.language == dictionaryLanguage && it.mode == gameMode }
        val record = if (index >= 0) records[index].copy()
        else LanguageModeBestGame(language = dictionaryLanguage, mode = gameMode)
        var changed = index < 0

        if (score > record.highestScore) {
            record.highestScore = score
            changed = true
        }
        if (longest.isNotEmpty() && longest.length >= record.longestWord.length) {
            record.longestWord = longest
            changed = true
        }
        if (wordCount > record.mostWords) {
            record.mostWords = wordCount
            changed = true
        }
        if (gameMode != GameMode.BOPPLE && largestLetterChain > record.largestLetterChain) {
            record.largestLetterChain = largestLetterChain
            changed = true
        }

        if (changed) {
            if (index >= 0) records[index] = record else records.add(record)
        }
        return LanguageModeUpdate(records, changed)
    }

    private fun loadLanguageModeBestGames(): List<LanguageModeBestGame> {
        val json = prefs.getString("bg_languageModeBestGames", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val languageName = item.optString("language")
                    val modeName = item.optString("mode")
                    val language = DictionaryLanguage.entries.find { it.name == languageName } ?: continue
                    val mode = GameMode.entries.find { it.name == modeName } ?: continue
                    add(
                        LanguageModeBestGame(
                            language = language,
                            mode = mode,
                            highestScore = item.optInt("highestScore"),
                            longestWord = item.optString("longestWord"),
                            mostWords = item.optInt("mostWords"),
                            largestLetterChain = item.optInt("largestLetterChain")
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun encodeLanguageModeBestGames(records: List<LanguageModeBestGame>): String {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("language", record.language.name)
                    .put("mode", record.mode.name)
                    .put("highestScore", record.highestScore)
                    .put("longestWord", record.longestWord)
                    .put("mostWords", record.mostWords)
                    .put("largestLetterChain", record.largestLetterChain)
            )
        }
        return array.toString()
    }

    private fun loadDailyBopLanguageStats(): List<DailyBopLanguageStat> {
        val json = prefs.getString("bg_dailyBopLanguageStats", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val languageName = item.optString("language")
                    val language = DictionaryLanguage.entries.find { it.name == languageName } ?: continue
                    val foundCount = item.optInt("foundCount")
                    if (foundCount <= 0) continue
                    add(
                        DailyBopLanguageStat(
                            language = language,
                            foundCount = foundCount,
                            lastFoundDateKey = item.optString("lastFoundDateKey")
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun encodeDailyBopLanguageStats(records: List<DailyBopLanguageStat>): String {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("language", record.language.name)
                    .put("foundCount", record.foundCount)
                    .put("lastFoundDateKey", record.lastFoundDateKey)
            )
        }
        return array.toString()
    }

    private fun loadGameMode(): GameMode {
        val saved = prefs.getString("wordBopGameMode", null)
        return GameMode.entries.find { it.name == saved } ?: GameMode.TIMED
    }

    private fun loadLetterPositionMode(): LetterPositionMode {
        val saved = prefs.getString("wordBopLetterPositionMode", null)
        return LetterPositionMode.entries.find { it.name == saved }
            ?: if (prefs.getBoolean("wordBopSpeakLetterPositions", false)) {
                LetterPositionMode.COLUMN_NUMBER_ROW_NUMBER
            } else {
                LetterPositionMode.OFF
            }
    }

    private fun loadBubbleTextColorOption(): BubbleTextColorOption {
        val saved = prefs.getString("wordBopBubbleTextColorOption", null)
        return BubbleTextColorOption.entries.find { it.name == saved } ?: BubbleTextColorOption.DARK
    }

    private fun loadBubbleColorTheme(option: BubbleTextColorOption): BubbleColorTheme {
        val saved = prefs.getString("wordBopBubbleColorTheme", null)
        val theme = BubbleColorTheme.entries.find { it.name == saved }
        return if (theme != null && theme.supports(option)) theme else BubbleColorTheme.defaultFor(option)
    }

    private fun loadBubbleLetterStyle(): BubbleLetterStyle {
        val saved = prefs.getString("wordBopBubbleLetterStyle", null)
        return BubbleLetterStyle.entries.find { it.name == saved } ?: BubbleLetterStyle.PLAYFUL
    }

    private fun loadDictionaryLanguage(): DictionaryLanguage {
        val saved = prefs.getString("wordBopDictionaryLanguage", null)
        return DictionaryLanguage.entries.find { it.name == saved } ?: DictionaryLanguage.ENGLISH
    }

    private fun loadGameAnnouncementVerbosity(): GameAnnouncementVerbosity {
        val saved = prefs.getString("wordBopGameAnnouncementVerbosity", null)
        return GameAnnouncementVerbosity.entries.find { it.name == saved } ?: GameAnnouncementVerbosity.NORMAL
    }

    private fun loadGridSizeOption(): GridSizeOption {
        val saved = prefs.getInt("wordBopGridSize", GridSizeOption.FIVE.dimension)
        return GridSizeOption.entries.find { it.dimension == saved } ?: GridSizeOption.FIVE
    }

    private fun loadGameVolume(): Float {
        return if (prefs.contains("wordBopGameVolume")) {
            prefs.getFloat("wordBopGameVolume", 0.82f).coerceIn(0f, 1f)
        } else {
            0.82f
        }
    }

    private fun loadDailyBopEnabledLanguages(fallback: DictionaryLanguage): List<DictionaryLanguage> {
        val saved = prefs.getString("wordBopDailyBopEnabledLanguages", null)
            ?: return sortedDailyBopLanguages(listOf(DictionaryLanguage.ENGLISH, fallback))
        val languages = saved
            .split(",")
            .mapNotNull { value -> DictionaryLanguage.entries.find { it.name == value } }
        return sortedDailyBopLanguages(languages.ifEmpty { listOf(fallback) })
    }

    private fun saveDailyBopEnabledLanguages() {
        prefs.edit()
            .putString("wordBopDailyBopEnabledLanguages", dailyBopEnabledLanguages.joinToString(",") { it.name })
            .apply()
    }

    override fun onCleared() {
        super.onCleared()
        audio.release()
        haptics.cancel()
        stopTimer()
        stopPowerUpTimer()
        stopDailyBopBoost(resetFound = false)
    }
}
