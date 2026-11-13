package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.TemperatureBand
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppButtonStyle
import com.example.walklogger.core.ui.component.AppButtonWidth
import com.example.walklogger.core.ui.component.AppPickerField
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.WeatherFormActions
import com.example.walklogger.feature.logwalk.WeatherFormState
import com.example.walklogger.feature.logwalk.WeatherStatus
import com.example.walklogger.feature.logwalk.preview.LogWalkPreviewData

/**
 * Collapsible "Weather" section: the fetched weather with a fetch button, then the temperature and
 * condition the handler can correct, the fallback place when location is unavailable, and the
 * attribution Open-Meteo's CC BY 4.0 licence requires.
 */
@Composable
internal fun WeatherSection(
    weather: WeatherFormState,
    actions: WeatherFormActions,
    modifier: Modifier = Modifier,
) {
    val walkWeather = weather.walkWeather
    FormSection(
        label = stringResource(R.string.log_past_walk_weather),
        expanded = weather.isExpanded,
        onToggle = actions::toggleWeather,
        actionLabel = if (walkWeather !=
            null
        ) {
            stringResource(R.string.form_section_clear)
        } else {
            null
        },
        onAction = actions::clearWeather,
        leadingActionLabel = weather.fetchedWeather?.let {
            stringResource(
                if (weather.isEditing) R.string.weather_done else R.string.weather_edit,
            )
        },
        onLeadingAction = actions::toggleWeatherEditing,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            val statusMessage = weather.status.messageRes
            if (walkWeather != null || statusMessage != null || weather.showsFetch) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.outline, MaterialTheme.shapes.extraSmall)
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    walkWeather?.let { WeatherSummary(weather = it) }
                    statusMessage?.let { MutedText(stringResource(it)) }
                    if (weather.showsFetch) {
                        AppButton(
                            text = stringResource(R.string.weather_fetch),
                            onClick = actions::fetchWeather,
                            style = AppButtonStyle.Secondary,
                            width = AppButtonWidth.Wrap,
                            loading = weather.status == WeatherStatus.LOADING,
                        )
                    }
                }
            }
            if (weather.showsManualEntry) {
                AppTextField(
                    value = weather.temperatureInput,
                    onValueChange = actions::setTemperature,
                    label = stringResource(R.string.weather_temperature),
                    suffix = stringResource(R.string.weather_temperature_unit),
                    supportingText = weather.temperatureCelsius?.let {
                        stringResource(TemperatureBand.of(it).labelRes)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.weather_condition),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.contentMuted,
                    )
                    WeatherConditionPicker(
                        selected = weather.condition,
                        isDay = weather.fetchedWeather?.isDay ?: true,
                        onSelect = actions::selectCondition,
                    )
                }
            }
            if (!weather.hasLocationPermission) {
                AppPickerField(
                    value = weather.place?.name.orEmpty(),
                    label = stringResource(R.string.weather_place),
                    placeholder = stringResource(R.string.weather_place_search_title),
                    onPress = actions::openPlaceSearch,
                )
            }
            MutedText(stringResource(R.string.weather_attribution))
        }
    }
    weather.placeSearch?.let { search ->
        PlaceSearchDialog(
            search = search,
            onSearch = actions::searchPlaces,
            onSelect = actions::selectPlace,
            onDismiss = actions::closePlaceSearch,
        )
    }
}

private val WeatherStatus.messageRes: Int?
    get() = when (this) {
        WeatherStatus.IDLE, WeatherStatus.LOADING -> null
        WeatherStatus.TOO_OLD -> R.string.weather_too_old
        WeatherStatus.NO_LOCATION -> R.string.weather_no_location
        WeatherStatus.FAILED -> R.string.weather_fetch_failed
    }

@Composable
private fun MutedText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.contentMuted,
    )
}

@Preview(showBackground = true)
@Composable
private fun WeatherSectionPreview() {
    WalkLoggerTheme {
        WeatherSection(
            weather = LogWalkPreviewData.finishWalk.form.weather.copy(isExpanded = true),
            actions = LogWalkPreviewData.formActions,
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherSectionEditingPreview() {
    WalkLoggerTheme {
        WeatherSection(
            weather = LogWalkPreviewData.finishWalk.form.weather.copy(isEditing = true),
            actions = LogWalkPreviewData.formActions,
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherSectionFailedPreview() {
    WalkLoggerTheme {
        WeatherSection(
            weather = WeatherFormState(
                isExpanded = true,
                hasLocationPermission = true,
                hasTimes = true,
                status = WeatherStatus.FAILED,
            ),
            actions = LogWalkPreviewData.formActions,
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherSectionWithoutLocationPreview() {
    WalkLoggerTheme {
        WeatherSection(
            weather = WeatherFormState(
                isExpanded = true,
                status = WeatherStatus.NO_LOCATION,
            ),
            actions = LogWalkPreviewData.formActions,
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
