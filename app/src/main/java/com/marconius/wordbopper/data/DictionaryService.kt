package com.marconius.wordbopper.data

import android.content.Context
import com.marconius.wordbopper.model.DictionaryLanguage
import java.text.Normalizer
import java.util.Locale

class DictionaryService private constructor(context: Context) {
    private val resources = context.resources
    private val wordsByLanguage = mutableMapOf<DictionaryLanguage, Set<String>>()

    fun contains(word: String, language: DictionaryLanguage = DictionaryLanguage.ENGLISH): Boolean {
        return words(language).contains(normalized(word, language))
    }

    private fun words(language: DictionaryLanguage): Set<String> {
        wordsByLanguage[language]?.let { return it }
        val words = resources
            .openRawResource(language.rawResourceId)
            .bufferedReader()
            .useLines { lines ->
                lines.mapNotNull { line ->
                    normalized(line, language).takeIf { it.isNotEmpty() }
                }.toHashSet()
            }
        wordsByLanguage[language] = words
        return words
    }

    fun normalized(word: String, language: DictionaryLanguage): String {
        val protectedCharacters = when (language) {
            DictionaryLanguage.ENGLISH,
            DictionaryLanguage.ITALIAN -> emptyMap()
            DictionaryLanguage.SPANISH -> mapOf("ñ" to "__WB_NTILDE__")
            DictionaryLanguage.FRENCH,
            DictionaryLanguage.BRAZILIAN_PORTUGUESE -> mapOf("ç" to "__WB_CCEDILLA__")
            DictionaryLanguage.GERMAN -> mapOf("ß" to "__WB_ESZETT__")
        }

        var normalizedWord = word.trim().lowercase(Locale.forLanguageTag(language.speechLanguage))
        protectedCharacters.forEach { (character, token) ->
            normalizedWord = normalizedWord.replace(character, token)
        }
        normalizedWord = Normalizer.normalize(normalizedWord, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
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
        @Volatile private var instance: DictionaryService? = null

        fun getInstance(context: Context): DictionaryService =
            instance ?: synchronized(this) {
                instance ?: DictionaryService(context.applicationContext).also { instance = it }
            }
    }
}
