package com.marconius.wordbopper.data

import android.content.Context
import com.marconius.wordbopper.model.BopQuestEvent
import com.marconius.wordbopper.model.BopQuestProgress
import com.marconius.wordbopper.model.DictionaryLanguage
import com.marconius.wordbopper.model.PlayerRank
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BopQuestService(context: Context) {
    private val assets = context.applicationContext.assets
    private val dictionary = DictionaryService.getInstance(context.applicationContext)

    fun loadActiveQuest(
        date: Date = Date(),
        calendar: Calendar = Calendar.getInstance()
    ): BopQuestEvent? {
        val startOfToday = calendar.apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        return selectActiveQuest(loadQuestIds().mapNotNull(::loadQuest), startOfToday)
    }

    fun loadPlayerRanks(): List<PlayerRank> {
        val parsed = readAsset("ranks-en.txt")
            ?.lineSequence()
            ?.mapNotNull(::parseRankLine)
            ?.sortedBy(PlayerRank::threshold)
            ?.toList()
            .orEmpty()
        return parsed.ifEmpty { FALLBACK_PLAYER_RANKS }
    }

    private fun loadQuestIds(): List<String> = readAsset("$QUEST_ROOT/events.txt")
        ?.lineSequence()
        ?.map(String::trim)
        ?.filter { it.isNotEmpty() && !it.startsWith("#") }
        ?.toList()
        .orEmpty()

    private fun loadQuest(id: String): BopQuestEvent? {
        val stem = resourceStem(id)
        val info = readAsset("$QUEST_ROOT/$id/$stem-info.txt")
            ?.let(::parseInfo)
            ?: return null
        val title = info["title"] ?: return null
        val starts = info["starts"]?.let(::parseDate) ?: return null
        val ends = info["ends"]?.let(::parseDate) ?: return null
        val wordsByLanguage = DictionaryLanguage.entries.mapNotNull { language ->
            val suffix = languageAssetSuffix(language)
            val words = readAsset("$QUEST_ROOT/$id/$stem-$suffix.txt")
                ?.let(::parseWords)
                ?.map { dictionary.normalized(it, language) }
                ?.distinct()
                .orEmpty()
            if (words.isEmpty()) null else language to words
        }.toMap()
        if (wordsByLanguage.isEmpty()) return null
        return BopQuestEvent(
            id = id,
            title = title,
            starts = starts,
            ends = ends,
            completionBonus = info["bonus"]?.toIntOrNull() ?: 0,
            wordsByLanguage = wordsByLanguage
        )
    }

    private fun readAsset(path: String): String? = runCatching {
        assets.open(path).bufferedReader().use { it.readText() }
    }.getOrNull()

    internal companion object {
        private const val QUEST_ROOT = "BopQuests"

        val FALLBACK_PLAYER_RANKS = listOf(
            PlayerRank(0, "WordBopper Newbie"),
            PlayerRank(10, "Bubble Scout"),
            PlayerRank(20, "Bop Apprentice")
        )

        data class ProgressUpdate(
            val progress: BopQuestProgress,
            val pointsEarned: Int
        )

        fun selectActiveQuest(events: List<BopQuestEvent>, startOfToday: Date): BopQuestEvent? =
            events.firstOrNull { startOfToday >= it.starts && startOfToday <= it.ends }

        fun recordWord(
            progress: BopQuestProgress,
            quest: BopQuestEvent,
            language: DictionaryLanguage,
            word: String
        ): ProgressUpdate? {
            val questWords = quest.words(language)
            if (word !in questWords) return null
            val foundWords = progress.foundWordsByLanguage[language].orEmpty().toMutableSet()
            if (!foundWords.add(word)) return null

            val foundWordsByLanguage = progress.foundWordsByLanguage.toMutableMap()
            foundWordsByLanguage[language] = questWords.filter(foundWords::contains)
            val awardedLanguages = progress.awardedCompletionBonusLanguages.toMutableList()
            var pointsEarned = 1
            if (foundWords.size == questWords.size && language !in awardedLanguages) {
                awardedLanguages.add(language)
                pointsEarned += questWords.size
            }
            return ProgressUpdate(
                progress = progress.copy(
                    foundWordsByLanguage = foundWordsByLanguage,
                    awardedCompletionBonusLanguages = awardedLanguages
                ),
                pointsEarned = pointsEarned
            )
        }

        fun parseInfo(text: String): Map<String, String> = buildMap {
            text.lineSequence().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                val separator = trimmed.indexOf(':')
                if (separator <= 0) return@forEach
                val key = trimmed.substring(0, separator).trim().lowercase(Locale.ROOT)
                val value = trimmed.substring(separator + 1).trim()
                if (value.isNotEmpty()) put(key, value)
            }
        }

        fun parseWords(text: String): List<String> {
            val seen = mutableSetOf<String>()
            return text.lineSequence().mapNotNull { line ->
                val word = line.trim().lowercase(Locale.ROOT)
                word.takeIf {
                    it.length >= 3 && !it.startsWith("#") && seen.add(it)
                }
            }.toList()
        }

        fun parseRankLine(line: String): PlayerRank? {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return null
            val parts = trimmed.split(Regex("\\s+"), limit = 2)
            if (parts.size != 2) return null
            val threshold = parts[0].toIntOrNull() ?: return null
            val title = parts[1].trim().takeIf(String::isNotEmpty) ?: return null
            return PlayerRank(threshold, title)
        }

        fun resourceStem(eventId: String): String {
            val parts = eventId.split("-", limit = 4)
            return if (parts.size == 4) "${parts[0]}-${parts[3]}" else eventId
        }

        private fun parseDate(value: String): Date? = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply { isLenient = false }.parse(value)

        private fun languageAssetSuffix(language: DictionaryLanguage): String = when (language) {
            DictionaryLanguage.ENGLISH -> "en"
            DictionaryLanguage.SPANISH -> "es"
            DictionaryLanguage.FRENCH -> "fr"
            DictionaryLanguage.GERMAN -> "de"
            DictionaryLanguage.DUTCH -> "nl"
            DictionaryLanguage.ITALIAN -> "it"
            DictionaryLanguage.BRAZILIAN_PORTUGUESE -> "pt-BR"
        }
    }
}
