package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Collapsible "Weather" form section; every condition is optional so it starts folded away. */
@Composable
internal fun WeatherSection(
    rain: Rain?,
    temperature: Temperature?,
    wind: Wind?,
    snow: Snow?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit,
    onRainSelected: (Rain) -> Unit,
    onTemperatureSelected: (Temperature) -> Unit,
    onWindSelected: (Wind) -> Unit,
    onSnowSelected: (Snow) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormSection(
        label = stringResource(R.string.log_past_walk_weather),
        expanded = expanded,
        onToggle = onToggle,
        actionLabel = if (listOf(rain, temperature, wind, snow).any { it != null }) stringResource(R.string.weather_clear) else null,
        onAction = onClear,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            WeatherRow(
                label = stringResource(R.string.weather_rain),
                options = Rain.entries,
                selected = rain,
                onSelect = onRainSelected,
                optionLabel = { stringResource(it.labelRes) },
            )
            WeatherRow(
                label = stringResource(R.string.weather_snow),
                options = Snow.entries,
                selected = snow,
                onSelect = onSnowSelected,
                optionLabel = { stringResource(it.labelRes) },
            )
            WeatherRow(
                label = stringResource(R.string.weather_temperature),
                options = Temperature.entries,
                selected = temperature,
                onSelect = onTemperatureSelected,
                optionLabel = { stringResource(it.labelRes) },
            )
            WeatherRow(
                label = stringResource(R.string.weather_wind),
                options = Wind.entries,
                selected = wind,
                onSelect = onWindSelected,
                optionLabel = { stringResource(it.labelRes) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherSectionPreview() {
    WalkLoggerTheme {
        WeatherSection(
            rain = Rain.LIGHT,
            temperature = Temperature.COLD,
            wind = null,
            snow = Snow.NONE,
            expanded = true,
            onToggle = {},
            onClear = {},
            onRainSelected = {},
            onTemperatureSelected = {},
            onWindSelected = {},
            onSnowSelected = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
