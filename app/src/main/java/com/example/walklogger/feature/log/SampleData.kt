package com.example.walklogger.feature.log

/**
 * Static sample content mirroring the mockup. Placeholder until a ViewModel supplies
 * real data from the data layer.
 */
internal data class SampleWalk(
    val startTime: String,
    val duration: String,
    val equipment: String,
    val rating: Int,
    val note: String,
)

internal val sampleWeek = listOf(
    SampleDayCell("Mon", 7, hasWalks = true, selected = false),
    SampleDayCell("Tue", 8, hasWalks = true, selected = false),
    SampleDayCell("Wed", 9, hasWalks = false, selected = false),
    SampleDayCell("Thu", 10, hasWalks = true, selected = false),
    SampleDayCell("Fri", 11, hasWalks = true, selected = false),
    SampleDayCell("Sat", 12, hasWalks = true, selected = true),
    SampleDayCell("Sun", 13, hasWalks = false, selected = false),
)

internal val sampleDay = listOf(
    SampleWalk("07:05", "38 min", "Harness", 6, "Long morning loop, calm the whole way."),
    SampleWalk("11:30", "17 min", "Collar", 4, "Quick errand walk, distracted by the market."),
)
