package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.common.toEpochMilli
import com.example.walklogger.core.common.toLocalDateTime
import com.example.walklogger.core.model.Walk
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds a form's walk date and times for its ViewModel. [onTimesChanged] gets both times in epoch millis
 * whenever they are set or change, so the weather can follow them.
 */
class WalkTimesEditor(
    private val clock: Clock,
    date: LocalDate,
    private val onTimesChanged: (startMillis: Long, endMillis: Long) -> Unit,
) {
    private val input =
        MutableStateFlow(WalkTimesState(date = date, now = LocalDateTime.now(clock)))

    val state: StateFlow<WalkTimesState> = input.asStateFlow()

    /** Start in epoch millis; null until a start time is set. */
    val startMillis: Long? get() = state.value.run { startTime?.let { millisOf(date, it) } }

    /** End in epoch millis; null until an end time is set. */
    val endMillis: Long? get() = state.value.run { endTime?.let { millisOf(date, it) } }

    fun setDate(date: LocalDate) = edit { it.copy(date = date) }

    fun setStartTime(time: LocalTime) = edit { it.copy(startTime = time) }

    fun setEndTime(time: LocalTime) = edit { it.copy(endTime = time) }

    /** Takes the date and times of a finished [walk]. */
    fun prefillFrom(walk: Walk) {
        val start = walk.startTime.toLocalDateTime(clock.zone)
        val end = checkNotNull(walk.endTime) {
            "walk ${walk.id} has not finished"
        }.toLocalDateTime(clock.zone)
        input.update {
            it.copy(
                date = start.toLocalDate(),
                startTime = start.toLocalTime(),
                endTime = end.toLocalTime(),
            )
        }
        notifyTimes()
    }

    /** Re-reads the clock alongside the edit so a form left open still validates against the present. */
    private fun edit(change: (WalkTimesState) -> WalkTimesState) {
        input.update { change(it).copy(now = LocalDateTime.now(clock)) }
        notifyTimes()
    }

    private fun notifyTimes() {
        val start = startMillis ?: return
        val end = endMillis ?: return
        onTimesChanged(start, end)
    }

    private fun millisOf(date: LocalDate, time: LocalTime): Long =
        date.atTime(time).toEpochMilli(clock.zone)
}
