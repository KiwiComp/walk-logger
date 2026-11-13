package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.formatWeatherValue

/** The weather of a walk in a few lines: condition and temperature, then precipitation and wind when known. */
@Composable
internal fun WeatherSummary(weather: WalkWeather, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            weather.condition?.let {
                Icon(
                    painter = painterResource(it.iconRes(weather.isDay)),
                    contentDescription = null,
                    tint = AppTheme.colors.content,
                )
            }
            val headline = listOfNotNull(
                weather.condition?.let { stringResource(it.labelRes) },
                weather.temperatureCelsius?.let {
                    stringResource(R.string.weather_temperature_value, formatWeatherValue(it))
                },
            )
            if (headline.isNotEmpty()) {
                Text(
                    text = headline.joinToString(" · "),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.colors.content,
                )
            }
        }
        if (weather.rainMm != null || weather.snowfallCm != null) {
            SummaryLine(
                stringResource(
                    R.string.weather_precipitation_value,
                    formatWeatherValue(weather.rainMm ?: 0.0),
                    formatWeatherValue(weather.snowfallCm ?: 0.0),
                ),
            )
        }
        weather.windSpeedMs?.let { speed ->
            SummaryLine(
                weather.windGustsMs?.let { gusts ->
                    stringResource(
                        R.string.weather_wind_value,
                        formatWeatherValue(speed),
                        formatWeatherValue(gusts),
                    )
                } ?: stringResource(R.string.weather_wind_speed_value, formatWeatherValue(speed)),
            )
        }
    }
}

@Composable
private fun SummaryLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.contentMuted,
    )
}

@Preview(showBackground = true)
@Composable
private fun WeatherSummaryPreview() {
    WalkLoggerTheme {
        WeatherSummary(
            weather = WalkWeather(
                condition = WeatherCondition.LIGHT_RAIN,
                temperatureCelsius = 7.0,
                rainMm = 0.4,
                snowfallCm = 0.0,
                windSpeedMs = 4.2,
                windGustsMs = 9.1,
            ),
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
