package com.example.walklogger.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.walklogger.R
import com.example.walklogger.feature.calendar.navigation.CalendarDestination
import com.example.walklogger.feature.dog.navigation.DogDestination
import com.example.walklogger.feature.log.navigation.LogDestination
import com.example.walklogger.feature.patterns.navigation.PatternsDestination
import kotlin.reflect.KClass

/** The four bottom-bar tabs, in display order. */
enum class TopLevelDestination(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    val route: KClass<*>,
) {
    LOG(label = R.string.tab_log, icon = R.drawable.ic_tab_log, route = LogDestination::class),
    CALENDAR(label = R.string.tab_calendar, icon = R.drawable.ic_tab_calendar, route = CalendarDestination::class),
    PATTERNS(label = R.string.tab_patterns, icon = R.drawable.ic_tab_patterns, route = PatternsDestination::class),
    DOG(label = R.string.tab_dogs, icon = R.drawable.ic_tab_dog, route = DogDestination::class),
}
