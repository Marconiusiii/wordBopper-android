package com.marconius.wordbopper.ui.accessibility

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.marconius.wordbopper.model.DictionaryLanguage

fun languageTaggedText(text: String, language: DictionaryLanguage): AnnotatedString {
    return buildAnnotatedString {
        withStyle(SpanStyle(localeList = language.localeList)) {
            append(text)
        }
    }
}

fun languageTaggedPartText(
    text: String,
    part: String,
    language: DictionaryLanguage
): AnnotatedString {
    if (part.isBlank()) return AnnotatedString(text)

    val start = text.indexOf(part, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)

    val end = start + part.length
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(localeList = language.localeList)) {
            append(text.substring(start, end))
        }
        append(text.substring(end))
    }
}
