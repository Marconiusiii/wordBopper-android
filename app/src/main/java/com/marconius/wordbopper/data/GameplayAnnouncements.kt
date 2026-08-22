package com.marconius.wordbopper.data

import com.marconius.wordbopper.model.GameAnnouncementVerbosity

object GameplayAnnouncements {
    const val CLEARED = "Cleared."
    const val WORD_CLEARED = "Word cleared."
    const val CLEARED_WITH_TIME_BONUS = "Cleared. 15 seconds added."
    const val DISCONNECTED_BOPPLE_WORD = "Bopple words must use letters that are next to each other."

    fun invalidWord(word: String) = "$word, not valid."
    fun duplicateWord(word: String) = "$word, already found."

    fun scoredWord(
        word: String,
        points: Int,
        chainBonus: Int,
        multiplier: Int,
        powerUpActivated: Boolean,
        verbosity: GameAnnouncementVerbosity,
        dailyBopActivated: Boolean = false,
        bopQuestFound: Boolean = false
    ): String {
        val pointText = if (points == 1) "1 point" else "$points points"

        if (verbosity == GameAnnouncementVerbosity.LOW) {
            if (dailyBopActivated) return "Daily Bop found! 3 times boost active!"
            if (bopQuestFound) return "BopQuest word found!"
            return if (powerUpActivated) "3 times active!" else "$pointText."
        }

        if (dailyBopActivated) return "$word, Daily Bop found! 3 times boost active!"
        if (powerUpActivated) return "3 times active!"

        val parts = mutableListOf("$word, $pointText")
        if (multiplier > 1) parts.add("3 times")
        else if (chainBonus > 0) parts.add("chain bonus")
        if (bopQuestFound) parts.add("BopQuest word found")

        return parts.joinToString(", ") + "."
    }
}
