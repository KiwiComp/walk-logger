package com.example.walklogger.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.example.walklogger.R
import com.example.walklogger.feature.calendar.navigation.CalendarDestination
import com.example.walklogger.feature.dog.navigation.DogDestination
import com.example.walklogger.feature.log.navigation.LogDestination
import com.example.walklogger.feature.patterns.navigation.PatternsDestination

/** The four bottom-bar tabs, in display order. */
enum class TopLevelDestination(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    val key: NavKey,
) {
    LOG(label = R.string.tab_log, icon = R.drawable.ic_tab_log, key = LogDestination),
    CALENDAR(
        label = R.string.tab_calendar,
        icon = R.drawable.ic_tab_calendar,
        key = CalendarDestination,
    ),
    PATTERNS(
        label = R.string.tab_patterns,
        icon = R.drawable.ic_tab_patterns,
        key = PatternsDestination,
    ),
    DOG(label = R.string.tab_dogs, icon = R.drawable.ic_tab_dog, key = DogDestination),
}
