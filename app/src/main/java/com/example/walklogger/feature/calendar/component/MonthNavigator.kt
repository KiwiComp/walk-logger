package com.example.walklogger.feature.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.PillButton
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * ‹ "November 2026" › with the month's walk count under the name, and a Today button while
 * another month than the current one is shown.
 */
@Composable
internal fun MonthNavigator(
    month: YearMonth,
    walkCount: Int,
    isCurrentMonth: Boolean,
    canShowNextMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_previous_month),
                onClick = onPreviousMonth,
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = month.format(formatter).replaceFirstChar { it.titlecase(locale) },
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppTheme.colors.content,
                )
                Text(
                    text = pluralStringResource(R.plurals.walk_count, walkCount, walkCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.contentMuted,
                )
            }
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.calendar_next_month),
                onClick = onNextMonth,
                enabled = canShowNextMonth,
            )
        }
        if (!isCurrentMonth) {
            PillButton(
                text = stringResource(R.string.calendar_today),
                onClick = onShowCurrentMonth,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthNavigatorPreview() {
    WalkLoggerTheme {
        MonthNavigator(
            month = YearMonth.of(2026, 10),
            walkCount = 11,
            isCurrentMonth = false,
            canShowNextMonth = true,
            onPreviousMonth = {},
            onNextMonth = {},
            onShowCurrentMonth = {},
        )
    }
}
