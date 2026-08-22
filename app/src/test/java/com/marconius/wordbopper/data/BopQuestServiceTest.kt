package com.marconius.wordbopper.data

import com.marconius.wordbopper.model.BopQuestEvent
import com.marconius.wordbopper.model.BopQuestProgress
import com.marconius.wordbopper.model.DictionaryLanguage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BopQuestServiceTest {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Test
    fun infoParserIgnoresCommentsAndPreservesValuesAfterColon() {
        val info = BopQuestService.parseInfo(
            """
            # Monthly quest
            title: Back to School: BopQuest
            starts: 2026-09-01
            ends: 2026-09-30
            """.trimIndent()
        )

        assertEquals("Back to School: BopQuest", info["title"])
        assertEquals("2026-09-01", info["starts"])
        assertEquals("2026-09-30", info["ends"])
    }

    @Test
    fun wordParserLowercasesAndRemovesInvalidOrDuplicateLinesInPlace() {
        val words = BopQuestService.parseWords(
            """
            # One word per line
            Beach
            swim
            BEACH
            an

            trail
            """.trimIndent()
        )

        assertEquals(listOf("beach", "swim", "trail"), words)
    }

    @Test
    fun activeQuestSelectionIncludesStartAndEndDates() {
        val august = quest("august", "2026-08-01", "2026-08-31", listOf("beach"))
        val september = quest("september", "2026-09-01", "2026-09-30", listOf("school"))
        val events = listOf(august, september)

        assertEquals(august, BopQuestService.selectActiveQuest(events, date("2026-08-01")))
        assertEquals(august, BopQuestService.selectActiveQuest(events, date("2026-08-31")))
        assertEquals(september, BopQuestService.selectActiveQuest(events, date("2026-09-01")))
        assertNull(BopQuestService.selectActiveQuest(events, date("2026-10-01")))
    }

    @Test
    fun recordingWordsAwardsEachWordOnceAndListSizedCompletionBonusOnce() {
        val quest = quest("august", "2026-08-01", "2026-08-31", listOf("beach", "swim"))
        val empty = BopQuestProgress(questId = quest.id)

        val first = BopQuestService.recordWord(empty, quest, DictionaryLanguage.ENGLISH, "beach")!!
        assertEquals(1, first.pointsEarned)
        assertNull(BopQuestService.recordWord(first.progress, quest, DictionaryLanguage.ENGLISH, "beach"))

        val completed = BopQuestService.recordWord(
            first.progress,
            quest,
            DictionaryLanguage.ENGLISH,
            "swim"
        )!!
        assertEquals(3, completed.pointsEarned)
        assertEquals(listOf("beach", "swim"), completed.progress.foundWordsByLanguage[DictionaryLanguage.ENGLISH])
        assertTrue(DictionaryLanguage.ENGLISH in completed.progress.awardedCompletionBonusLanguages)
        assertNull(BopQuestService.recordWord(completed.progress, quest, DictionaryLanguage.ENGLISH, "swim"))
    }

    @Test
    fun rankParserAcceptsTabsOrSpacesAndRejectsMalformedLines() {
        assertEquals("Bubble Scout", BopQuestService.parseRankLine("10\tBubble Scout")?.title)
        assertEquals(20, BopQuestService.parseRankLine("20 Bop Apprentice")?.threshold)
        assertNull(BopQuestService.parseRankLine("not-a-number Rank"))
        assertNull(BopQuestService.parseRankLine("# 30 Hidden Rank"))
    }

    @Test
    fun bundledQuestAndRankAssetsMatchTheEditableCatalog() {
        val assets = File("src/main/assets")
        val questRoot = assets.resolve("BopQuests")
        val eventIds = questRoot.resolve("events.txt").readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }

        assertEquals(listOf("2026-08-01-LateSummer", "2026-09-01-BackToSchool"), eventIds)
        val expectedWordCounts = mapOf(
            "2026-08-01-LateSummer" to 15,
            "2026-09-01-BackToSchool" to 17
        )
        eventIds.forEach { eventId ->
            val stem = BopQuestService.resourceStem(eventId)
            val eventFolder = questRoot.resolve(eventId)
            val info = BopQuestService.parseInfo(eventFolder.resolve("$stem-info.txt").readText())
            val words = BopQuestService.parseWords(eventFolder.resolve("$stem-en.txt").readText())
            assertTrue(info.keys.containsAll(listOf("title", "starts", "ends")))
            assertEquals(expectedWordCounts[eventId], words.size)
        }

        val ranks = assets.resolve("ranks-en.txt").readLines()
            .mapNotNull(BopQuestService::parseRankLine)
        assertEquals(47, ranks.size)
        assertEquals(ranks.sortedBy { it.threshold }, ranks)
        assertEquals(47, ranks.map { it.threshold }.distinct().size)
    }

    private fun quest(
        id: String,
        starts: String,
        ends: String,
        words: List<String>
    ) = BopQuestEvent(
        id = id,
        title = id,
        starts = date(starts),
        ends = date(ends),
        completionBonus = 10,
        wordsByLanguage = mapOf(DictionaryLanguage.ENGLISH to words)
    )

    private fun date(value: String) = requireNotNull(dateFormat.parse(value))
}
