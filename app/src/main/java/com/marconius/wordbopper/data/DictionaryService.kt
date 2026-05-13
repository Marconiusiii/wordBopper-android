package com.marconius.wordbopper.data

import android.content.Context
import com.marconius.wordbopper.R

class DictionaryService private constructor(context: Context) {
    private val words: Set<String> = context.resources
        .openRawResource(R.raw.words)
        .bufferedReader()
        .readLines()
        .mapNotNull { it.trim().lowercase().takeIf { w -> w.isNotEmpty() } }
        .toHashSet()

    fun contains(word: String) = words.contains(word.lowercase())

    companion object {
        @Volatile private var instance: DictionaryService? = null

        fun getInstance(context: Context): DictionaryService =
            instance ?: synchronized(this) {
                instance ?: DictionaryService(context.applicationContext).also { instance = it }
            }
    }
}
