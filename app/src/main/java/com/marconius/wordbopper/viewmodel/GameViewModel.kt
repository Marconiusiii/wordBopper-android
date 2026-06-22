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
import com.marconius.wordbopper.model.BubbleLetterStyle
import com.marconius.wordbopper.model.BubbleTextColorOption
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

    private var timerJob: Job? = null
    private var powerUpTimerJob: Job? = null
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
        get() = if (chainPowerUpActive)
            "3 times chain bop active"
        else "$connectedWordStreak of 3 chains"

    val chainMeterProgress: Double
        get() = if (chainPowerUpActive) (chainPowerUpSecondsLeft.toDouble() / 15.0) * 3.0
        else connectedWordStreak.toDouble()

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
        bubbleLetterStyle = loadBubbleLetterStyle()
        dictionaryLanguage = loadDictionaryLanguage()
        gameAnnouncementVerbosity = loadGameAnnouncementVerbosity()
        bopAway = prefs.getBoolean("wordBopBopAway", false)
        gridSizeOption = loadGridSizeOption()
        leftHandedMode = prefs.getBoolean("wordBopLeftHandedMode", false)
        gameHapticsEnabled = prefs.getBoolean("wordBopGameHapticsEnabled", true)
        haptics.isEnabled = gameHapticsEnabled
        boardColumns = gridSizeOption.dimension
        boardRows = gridSizeOption.dimension
    }

    private var warmUpStarted = false

    // Cold-launch warm-up. Runs once, behind the Loading screen. Loads the dictionary,
    // primes the audio engine, and pre-builds a throwaway round so the gameplay code and
    // bubble objects are all initialized during loading rather than on the first real
    // Start press. Enforces a brief minimum so the spoken "Loading WordBopper" cue is
    // actually heard, then moves to the Home screen (which focuses its heading on appear).
    fun warmUp() {
        if (warmUpStarted) return
        warmUpStarted = true
        viewModelScope.launch {
            val minimumShown = launch { delay(600) }
            withContext(Dispatchers.IO) {
                dictionary.preload(dictionaryLanguage)
                audio.warmUp()
                prebuildThrowawayRound()
            }
            minimumShown.join()
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
        prefs.edit().putString("wordBopBubbleTextColorOption", option.name).apply()
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
    }

    private fun preloadDictionary(language: DictionaryLanguage) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionary.preload(language)
        }
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

    // MARK: - Game lifecycle

    // Used by the Monarch tactile display to lock the board to its fixed dimensions,
    // overriding the Grid Size preference for the duration of the Monarch session.
    fun setBoardSize(columns: Int, rows: Int) {
        if (gameActive) return
        monarchBoardLocked = true
        boardColumns = columns.coerceIn(3, 8)
        boardRows = rows.coerceIn(3, 8)
    }

    fun startGame() {
        if (gameActive || startGameJob?.isActive == true) return
        if (dictionary.isLoaded(dictionaryLanguage)) {
            beginGame()
            return
        }
        startGameJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dictionary.preload(dictionaryLanguage)
            }
            beginGame()
        }
    }

    private fun beginGame() {
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
        audio.playRoundStartSound()
        if (showsTimer) startTimer()
    }

    fun pauseGame(playSound: Boolean = true) {
        if (!gameActive || gamePaused) return
        gamePaused = true
        stopTimer()
        pausePowerUpCountdown()
        if (playSound) audio.playPauseSound()
    }

    fun resumeGame() {
        if (!gameActive || !gamePaused) return
        gamePaused = false
        audio.playResumeSound()
        if (showsTimer) startTimer()
        if (chainPowerUpActive) startPowerUpCountdown(audioDelayMs = 550)
    }

    fun endGame() {
        if (!gameActive) return
        gameActive = false
        gamePaused = false
        stopTimer()
        stopPowerUpTimer()
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
        val multiplier = if (gameMode == GameMode.BOPPLE) 1 else if (chainPowerUpActive) 3 else 1
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
            stopPowerUpTimer()
            audio.playChainMultiplierScoreSound(word.length)
            haptics.powerUpScored()
        } else {
            audio.playWordSound(word.length)
            haptics.wordScored(word.length)
        }

        val powerUpActivated = if (gameMode == GameMode.BOPPLE) false else updateChainStreak(chainBonus)

        announce(
            GameplayAnnouncements.scoredWord(word, points, chainBonus, multiplier, powerUpActivated, gameAnnouncementVerbosity),
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
            val candidate = dictionaryLanguage.letterPool.random()
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
        return dictionaryLanguage.letterPool
            .filterNot { it in adjacentLetters }
            .ifEmpty { dictionaryLanguage.letterPool }
            .random()
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
    private fun randomGameplayHeading() = if (gameMode == GameMode.BOPPLE)
        BOPPLE_GAMEPLAY_HEADINGS.random() else GAMEPLAY_HEADINGS.random()

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
        languageModeBestGames = loadLanguageModeBestGames()
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

    override fun onCleared() {
        super.onCleared()
        audio.release()
        haptics.cancel()
        stopTimer()
        stopPowerUpTimer()
    }
}
