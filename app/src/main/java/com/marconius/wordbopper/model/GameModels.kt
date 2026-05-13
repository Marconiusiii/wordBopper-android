package com.marconius.wordbopper.model

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
    var largestNonStopLetterChain: Int = 0
)
