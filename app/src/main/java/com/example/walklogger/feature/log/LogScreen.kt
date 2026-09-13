package com.example.walklogger.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.component.DogBadge
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.Steel
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * The Log tab. UI only for now: content is static sample data that mirrors the mockup.
 * A ViewModel will replace [sampleDay] and [sampleWeek] once the data layer is wired up.
 */
@Composable
fun LogScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = "Walk log",
            actions = { DogBadge(name = "Sixten") },
        ) {
            Spacer(Modifier.height(20.dp))
            WeekStrip(days = sampleWeek)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            DayHeading(title = "Saturday 12 Sep", summary = "2 walks · avg 5.0")
            Spacer(Modifier.height(16.dp))
            WalkTimeline(walks = sampleDay)
            AddWalkPlaceholder(dayLabel = "Sat", modifier = Modifier.padding(start = TimelineGutter))
        }
    }
}

// --- Static sample content ---------------------------------------------------------------

private data class SampleWalk(
    val startTime: String,
    val duration: String,
    val equipment: String,
    val rating: Int,
    val note: String,
)

private val sampleWeek = listOf(
    SampleDayCell("Mon", 7, hasWalks = true, selected = false),
    SampleDayCell("Tue", 8, hasWalks = true, selected = false),
    SampleDayCell("Wed", 9, hasWalks = false, selected = false),
    SampleDayCell("Thu", 10, hasWalks = true, selected = false),
    SampleDayCell("Fri", 11, hasWalks = true, selected = false),
    SampleDayCell("Sat", 12, hasWalks = true, selected = true),
    SampleDayCell("Sun", 13, hasWalks = false, selected = false),
)

private val sampleDay = listOf(
    SampleWalk("07:05", "38 min", "Harness", 6, "Long morning loop, calm the whole way."),
    SampleWalk("11:30", "17 min", "Collar", 4, "Quick errand walk, distracted by the market."),
)

// --- Pieces -------------------------------------------------------------------------------

private val TimelineGutter = 84.dp

@Composable
private fun DayHeading(title: String, summary: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.width(12.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = InkMuted,
            modifier = Modifier.padding(bottom = 3.dp),
        )
    }
}

@Composable
private fun WalkTimeline(walks: List<SampleWalk>) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        walks.forEach { walk ->
            Row {
                Column(
                    modifier = Modifier.width(TimelineGutter - 16.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(text = walk.startTime, style = MaterialTheme.typography.titleMedium)
                    Text(text = walk.duration, style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(Modifier.size(8.dp).background(Steel))
                }
                WalkCard(walk = walk, modifier = Modifier.weight(1f))
            }
        }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun WalkCard(walk: SampleWalk, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardSurface)
            .border(1.dp, Outline)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = walk.equipment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .background(ChipSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.weight(1f))
            RatingBar(rating = walk.rating)
        }
        Text(text = walk.note, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RatingBar(rating: Int, max: Int = 7) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(max) { index ->
            Box(
                Modifier
                    .size(width = 9.dp, height = 16.dp)
                    .background(if (index < rating) Steel else Outline),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text = rating.toString(), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AddWalkPlaceholder(dayLabel: String, modifier: Modifier = Modifier) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawBehind {
                drawRect(color = Outline, style = Stroke(width = 3f, pathEffect = dash))
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+ Add a walk for $dayLabel",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LogScreenPreview() {
    WalkLoggerTheme {
        LogScreen()
    }
}
