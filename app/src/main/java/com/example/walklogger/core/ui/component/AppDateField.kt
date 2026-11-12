package com.example.walklogger.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * [AppPickerField] that opens a [DatePickerDialog]. [onDateChange] receives null when the
 * dialog is confirmed with no day selected. [formatter] is applied with the current locale;
 * days after [maxDate] are disabled in the picker.
 */
@Composable
fun AppDateField(
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG),
    maxDate: LocalDate? = null,
) {
    val locale = LocalConfiguration.current.locales[0]
    val localizedFormatter = remember(formatter, locale) { formatter.withLocale(locale) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    AppPickerField(
        value = date?.format(localizedFormatter).orEmpty(),
        label = label,
        onPress = { showPicker = true },
        placeholder = placeholder,
        modifier = modifier,
    )
    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(
                ZoneOffset.UTC,
            )?.toInstant()?.toEpochMilli(),
            selectableDates = remember(maxDate) {
                maxDate?.let(::UpToDate)
                    ?: DatePickerDefaults.AllDates
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateChange(pickerState.selectedDateMillis?.toLocalDate())
                        showPicker = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private class UpToDate(private val max: LocalDate) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        !utcTimeMillis.toLocalDate().isAfter(max)
    override fun isSelectableYear(year: Int): Boolean = year <= max.year
}

// DatePicker reports UTC midnight of the chosen day.
private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Preview(showBackground = true)
@Composable
private fun AppDateFieldPreview() {
    WalkLoggerTheme {
        AppDateField(
            date = LocalDate.of(2026, 9, 12),
            onDateChange = {},
            label = "Date",
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
