package com.marconius.wordbopper.model

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

enum class GameAnnouncementVerbosity(val label: String) {
    NORMAL("Normal"),
    LOW("Low"),
    OFF("Off")
}
