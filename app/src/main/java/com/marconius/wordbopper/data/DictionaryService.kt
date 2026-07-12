package com.marconius.wordbopper.data

import android.content.Context
import com.marconius.wordbopper.model.DictionaryLanguage
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class DictionaryService private constructor(context: Context) {
    private val resources = context.resources
    private val wordsByLanguage = ConcurrentHashMap<DictionaryLanguage, Set<String>>()
    private val dailyWords = ConcurrentHashMap<String, String>()
    private val locales = DictionaryLanguage.entries.associateWith { language ->
        Locale.forLanguageTag(language.speechLanguage)
    }
    private val protectedCharactersByLanguage = mapOf(
        DictionaryLanguage.SPANISH to mapOf("ñ" to "__WB_NTILDE__"),
        DictionaryLanguage.FRENCH to mapOf("ç" to "__WB_CCEDILLA__"),
        DictionaryLanguage.BRAZILIAN_PORTUGUESE to mapOf("ç" to "__WB_CCEDILLA__"),
        DictionaryLanguage.GERMAN to mapOf("ß" to "__WB_ESZETT__")
    )

    fun contains(word: String, language: DictionaryLanguage = DictionaryLanguage.ENGLISH): Boolean {
        return words(language).contains(normalized(word, language))
    }

    fun preload(language: DictionaryLanguage) {
        words(language)
    }

    fun isLoaded(language: DictionaryLanguage): Boolean {
        return wordsByLanguage.containsKey(language)
    }

    fun dailyWord(language: DictionaryLanguage, calendar: Calendar = Calendar.getInstance()): String {
        val dateKey = dailyDateKey(calendar)
        val cacheKey = "${language.name}-$dateKey"
        dailyWords[cacheKey]?.let { return it }
        val word = words(language)
            .asSequence()
            .filter { it.length in 6..10 && it.all { character -> character.isLetter() } }
            .minByOrNull { stableSeed("${language.dailySeedName()}-$dateKey-$it") }
            .orEmpty()
        dailyWords[cacheKey] = word
        return word
    }

    private fun words(language: DictionaryLanguage): Set<String> {
        wordsByLanguage[language]?.let { return it }
        val loadedWords = resources
            .openRawResource(language.rawResourceId)
            .bufferedReader()
            .useLines { lines ->
                lines.mapNotNull { line ->
                    normalized(line, language).takeIf { it.isNotEmpty() }
                }.toHashSet()
            }
        return wordsByLanguage.putIfAbsent(language, loadedWords) ?: loadedWords
    }

    fun normalized(word: String, language: DictionaryLanguage): String {
        var normalizedWord = word.trim().lowercase(locales.getValue(language))
        if (normalizedWord.all { character -> character.code < 128 }) {
            return normalizedWord
        }

        val protectedCharacters = protectedCharactersByLanguage[language].orEmpty()
        protectedCharacters.forEach { (character, token) ->
            normalizedWord = normalizedWord.replace(character, token)
        }
        normalizedWord = Normalizer.normalize(normalizedWord, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .replace("æ", "ae")
            .replace("œ", "oe")
        protectedCharacters.forEach { (character, token) ->
            normalizedWord = normalizedWord
                .replace(token.lowercase(Locale.ROOT), character)
                .replace(token, character)
        }
        return normalizedWord
    }

    private fun dailyDateKey(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "%04d%02d%02d".format(year, month, day)
    }

    private fun stableSeed(string: String): ULong {
        var hash = 14_695_981_039_346_656_037UL
        for (byte in string.toByteArray(Charsets.UTF_8)) {
            hash = hash xor byte.toUByte().toULong()
            hash *= 1_099_511_628_211UL
        }
        return hash % Long.MAX_VALUE.toULong()
    }

    private fun DictionaryLanguage.dailySeedName(): String = when (this) {
        DictionaryLanguage.ENGLISH -> "english"
        DictionaryLanguage.SPANISH -> "spanish"
        DictionaryLanguage.FRENCH -> "french"
        DictionaryLanguage.GERMAN -> "german"
        DictionaryLanguage.DUTCH -> "dutch"
        DictionaryLanguage.ITALIAN -> "italian"
        DictionaryLanguage.BRAZILIAN_PORTUGUESE -> "brazilianPortuguese"
    }

    companion object {
        private val COMBINING_MARKS = Regex("\\p{Mn}+")

        @Volatile private var instance: DictionaryService? = null

        fun getInstance(context: Context): DictionaryService =
            instance ?: synchronized(this) {
                instance ?: DictionaryService(context.applicationContext).also { instance = it }
            }
    }
}
