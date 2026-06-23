package com.marconius.wordbopper.data

import android.content.Context
import com.marconius.wordbopper.model.DictionaryLanguage
import java.text.Normalizer
import java.util.concurrent.ConcurrentHashMap
import java.util.Locale

class DictionaryService private constructor(context: Context) {
    private val resources = context.resources
    private val wordsByLanguage = ConcurrentHashMap<DictionaryLanguage, Set<String>>()
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

    companion object {
        private val COMBINING_MARKS = Regex("\\p{Mn}+")

        @Volatile private var instance: DictionaryService? = null

        fun getInstance(context: Context): DictionaryService =
            instance ?: synchronized(this) {
                instance ?: DictionaryService(context.applicationContext).also { instance = it }
            }
    }
}
