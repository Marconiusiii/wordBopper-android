package com.marconius.wordbopper.model

import androidx.compose.ui.text.font.FontFamily
import com.marconius.wordbopper.R

enum class GameScreen { START, GAME, RESULTS }

enum class GameMode(val label: String, val settingsBlurb: String) {
    TIMED(
        "Timed",
        "Make as many words as you can in 2 minutes! Letters change as you use them."
    ),
    BOPPLE(
        "Bopple",
        "Bopped letters will not change when you make words. Words must be made up of letters that are next to each other in the grid. How many words can you create in 3 minutes?"
    ),
    NON_STOP(
        "Non-Stop",
        "Bop to the Top! Non-Stop mode takes away the game timer, so bop as many letters and make as many words as you want!"
    )
}

enum class BubbleTextColorOption(val label: String) {
    DARK("Dark Text"),
    LIGHT("Light Text")
}

enum class BubbleLetterStyle(val label: String, val fontFamily: FontFamily) {
    PLAYFUL("Playful", FontFamily.SansSerif),
    SIMPLE("Simple", FontFamily.Default),
    TYPEWRITER("Typewriter", FontFamily.Monospace)
}

enum class GameAnnouncementVerbosity(val label: String) {
    NORMAL("Normal"),
    LOW("Low"),
    OFF("Off")
}

enum class DictionaryLanguage(
    val label: String,
    val rawResourceId: Int,
    val speechLanguage: String,
    private val letterPoolText: String,
    private val phonetics: Map<String, String>
) {
    ENGLISH(
        label = "English",
        rawResourceId = R.raw.words_en,
        speechLanguage = "en",
        letterPoolText = "aaaaaaaaaabbccddddeeeeeeeeeefffggghhhhiiiiiiijkllll" +
            "mmnnnnnnoooooooppqrrrrrsssssstttttttuuuuvvwwxyyz",
        phonetics = mapOf(
            "a" to "Alpha", "b" to "Bravo", "c" to "Charlie", "d" to "Delta",
            "e" to "Echo", "f" to "Foxtrot", "g" to "Golf", "h" to "Hotel",
            "i" to "India", "j" to "Juliet", "k" to "Kilo", "l" to "Lima",
            "m" to "Mike", "n" to "November", "o" to "Oscar", "p" to "Papa",
            "q" to "Quebec", "r" to "Romeo", "s" to "Sierra", "t" to "Tango",
            "u" to "Uniform", "v" to "Victor", "w" to "Whiskey", "x" to "XRay",
            "y" to "Yankee", "z" to "Zulu"
        )
    ),
    SPANISH(
        label = "Spanish",
        rawResourceId = R.raw.words_es,
        speechLanguage = "es",
        letterPoolText = "aaaaaaaaaaaabbccddddeeeeeeeeeeffggghhiiiiiiijkllll" +
            "mmmmnnnnnñoooooooppqrrrrrrssssssttttttuuuuvxyyz",
        phonetics = mapOf(
            "a" to "Antonio", "b" to "Barcelona", "c" to "Carmen", "d" to "Dolores",
            "e" to "España", "f" to "Francia", "g" to "Granada", "h" to "Historia",
            "i" to "Inés", "j" to "José", "k" to "Kilo", "l" to "Lorenzo",
            "m" to "Madrid", "n" to "Navarra", "ñ" to "Ñoño", "o" to "Oviedo",
            "p" to "París", "q" to "Queso", "r" to "Ramón", "s" to "Sevilla",
            "t" to "Toledo", "u" to "Úrsula", "v" to "Valencia",
            "w" to "Washington", "x" to "Xilófono", "y" to "Yolanda",
            "z" to "Zaragoza"
        )
    ),
    FRENCH(
        label = "French",
        rawResourceId = R.raw.words_fr,
        speechLanguage = "fr",
        letterPoolText = "aaaaaaaaabbccçddddeeeeeeeeeeeeeeeffgghhiiiiiiijkll" +
            "llmmnnnnnoooooooppqrrrrrrssssssttttttuuuuuuvxyyz",
        phonetics = mapOf(
            "a" to "Anatole", "b" to "Berthe", "c" to "Célestin", "ç" to "C cédille",
            "d" to "Désiré", "e" to "Eugène", "f" to "François", "g" to "Gaston",
            "h" to "Henri", "i" to "Irma", "j" to "Joseph", "k" to "Kléber",
            "l" to "Louis", "m" to "Marcel", "n" to "Nicolas", "o" to "Oscar",
            "p" to "Pierre", "q" to "Quintal", "r" to "Raoul", "s" to "Suzanne",
            "t" to "Thérèse", "u" to "Ursule", "v" to "Victor", "x" to "Xavier",
            "y" to "Yvonne", "z" to "Zoé"
        )
    ),
    GERMAN(
        label = "German",
        rawResourceId = R.raw.words_de,
        speechLanguage = "de",
        letterPoolText = "aaaaaaaaabbcccddddeeeeeeeeeeffffgggghhhhiiiiijkllll" +
            "mmmnnnnnnooooooooppqrrrrrrssssssßttttttuuuuuuvwxyz",
        phonetics = mapOf(
            "a" to "Anton", "b" to "Berta", "c" to "Cäsar", "d" to "Dora",
            "e" to "Emil", "f" to "Friedrich", "g" to "Gustav", "h" to "Heinrich",
            "i" to "Ida", "j" to "Julius", "k" to "Kaufmann", "l" to "Ludwig",
            "m" to "Martha", "n" to "Nordpol", "o" to "Otto", "p" to "Paula",
            "q" to "Quelle", "r" to "Richard", "s" to "Samuel", "ß" to "Eszett",
            "t" to "Theodor", "u" to "Ulrich", "v" to "Viktor", "w" to "Wilhelm",
            "x" to "Xanthippe", "y" to "Ypsilon", "z" to "Zacharias"
        )
    ),
    ITALIAN(
        label = "Italian",
        rawResourceId = R.raw.words_it,
        speechLanguage = "it",
        letterPoolText = "aaaaaaaaaaaabbcccddddeeeeeeeeeeefgghhiiiiiiiilll" +
            "mmmnnnnnnoooooooooopqrrrrrrssssssttttttuuuuuvvz",
        phonetics = mapOf(
            "a" to "Ancona", "b" to "Bologna", "c" to "Como", "d" to "Domodossola",
            "e" to "Empoli", "f" to "Firenze", "g" to "Genova", "h" to "Hotel",
            "i" to "Imola", "j" to "Jolly", "k" to "Kappa", "l" to "Livorno",
            "m" to "Milano", "n" to "Napoli", "o" to "Otranto", "p" to "Palermo",
            "q" to "Quarto", "r" to "Roma", "s" to "Savona", "t" to "Torino",
            "u" to "Udine", "v" to "Venezia", "w" to "Washington",
            "x" to "Xilofono", "y" to "Yacht", "z" to "Zara"
        )
    );

    val letterPool: List<String> get() = letterPoolText.map { it.toString() }

    fun phoneticName(letter: String): String? = phonetics[letter.lowercase()]
}
