package com.example.walklogger.feature.logwalk

const val MAX_NOTES_LENGTH = 500

enum class SaveBlocker { HANDLER, TIMES, END_BEFORE_START, IN_FUTURE, RATING }
