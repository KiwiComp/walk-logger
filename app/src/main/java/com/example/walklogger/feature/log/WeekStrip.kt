package com.example.walklogger.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.theme.Navy
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.OutlineOnNavy
import com.example.walklogger.core.ui.theme.SteelLight

/** Seven-day strip that lives inside the Log header. */
@Composable
internal fun WeekStrip(days: List<SampleDayCell>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { day -> DayCell(day) }
    }
}

/** What a single cell needs to draw. Will map from a domain model later. */
internal data class SampleDayCell(
    val label: String,
    val number: Int,
    val hasWalks: Boolean,
    val selected: Boolean,
)

@Composable
private fun RowScope.DayCell(day: SampleDayCell) {
    val background = if (day.selected) OnNavy else Color.Transparent
    val foreground = if (day.selected) Navy else OnNavy
    Column(
        modifier = Modifier
            .weight(1f)
            .height(76.dp)
            .background(background)
            .border(1.dp, if (day.selected) OnNavy else OutlineOnNavy)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionLabel(text = day.label, color = foreground)
        Spacer(Modifier.height(4.dp))
        Text(
            text = day.number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = foreground,
        )
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(6.dp)
                .background(if (day.hasWalks) SteelLight else Color.Transparent),
        )
    }
}
