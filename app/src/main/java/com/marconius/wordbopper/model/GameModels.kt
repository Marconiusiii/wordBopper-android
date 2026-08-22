package com.marconius.wordbopper.model

import java.util.Date
import java.util.UUID

data class Bubble(
    val id: UUID = UUID.randomUUID(),
    val letter: String,
    val colorIndex: Int,
    val row: Int,
    val col: Int
)

data class SelectedLetter(
    val bubbleId: UUID,
    val letter: String,
    val row: Int,
    val col: Int
)

data class BestGame(
    var highestScore: Int = 0,
    var highestBoppleScore: Int = 0,
    var highestNonStopScore: Int = 0,
    var longestWord: String = "",
    var longestBoppleWord: String = "",
    var longestNonStopWord: String = "",
    var mostWords: Int = 0,
    var mostBoppleWords: Int = 0,
    var mostNonStopWords: Int = 0,
    var largestLetterChain: Int = 0,
    var largestBoppleLetterChain: Int = 0,
    var largestNonStopLetterChain: Int = 0,
    var languageModeBestGames: List<LanguageModeBestGame> = emptyList(),
    var dailyBopLanguageStats: List<DailyBopLanguageStat> = emptyList(),
    var bopQuestRankPoints: Int = 0,
    var bopQuestProgress: List<BopQuestProgress> = emptyList()
)

data class LanguageModeBestGame(
    var language: DictionaryLanguage,
    var mode: GameMode,
    var highestScore: Int = 0,
    var longestWord: String = "",
    var mostWords: Int = 0,
    var largestLetterChain: Int = 0
) {
    val id: String get() = "${language.name}-${mode.name}"
    val heading: String get() = "${language.label} ${mode.label} Mode"
}

data class DailyBopLanguageStat(
    var language: DictionaryLanguage,
    var foundCount: Int = 0,
    var lastFoundDateKey: String = ""
) {
    val id: String get() = language.name
}

data class DailyBopEntry(
    val language: DictionaryLanguage,
    val word: String
) {
    val id: String get() = "${language.name}-$word"
}

data class PlayerRank(
    val threshold: Int,
    val title: String
)

data class BopQuestWord(
    val word: String,
    val found: Boolean
)

data class BopQuestEvent(
    val id: String,
    val title: String,
    val starts: Date,
    val ends: Date,
    val completionBonus: Int,
    val wordsByLanguage: Map<DictionaryLanguage, List<String>>
) {
    fun words(language: DictionaryLanguage): List<String> = wordsByLanguage[language].orEmpty()
}

data class BopQuestProgress(
    val questId: String,
    var foundWordsByLanguage: Map<DictionaryLanguage, List<String>> = emptyMap(),
    var awardedCompletionBonusLanguages: List<DictionaryLanguage> = emptyList()
)
