package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.logwalk.PlaceSearch

/** Searches for a town by name; tapping a result picks it. */
@Composable
internal fun PlaceSearchDialog(
    search: PlaceSearch,
    onSearch: (String) -> Unit,
    onSelect: (Place) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraSmall,
        containerColor = AppTheme.colors.background,
        title = { Text(stringResource(R.string.weather_place_search_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.weather_place_search_hint),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                )
                val message = when {
                    search.isSearching -> R.string.weather_place_searching
                    search.isFailed -> R.string.weather_place_search_failed
                    search.results?.isEmpty() == true -> R.string.weather_place_no_results
                    else -> null
                }
                message?.let {
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.contentMuted,
                    )
                }
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    search.results.orEmpty().forEach { place ->
                        PlaceRow(place = place, onClick = { onSelect(place) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSearch(query) },
                enabled =
                    query.isNotBlank() && !search.isSearching,
            ) {
                Text(stringResource(R.string.weather_place_search_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Composable
private fun PlaceRow(place: Place, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = AppSpacing.sm),
    ) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.content,
        )
        place.region?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.contentMuted,
            )
        }
    }
}

@Preview
@Composable
private fun PlaceSearchDialogPreview() {
    WalkLoggerTheme {
        PlaceSearchDialog(
            search = PlaceSearch(
                results = listOf(
                    Place("Uppsala", "Uppsala län, Sverige", Coordinates(59.86, 17.64)),
                    Place("Uppsala", "Örebro län, Sverige", Coordinates(58.82, 15.03)),
                ),
            ),
            onSearch = {},
            onSelect = {},
            onDismiss = {},
        )
    }
}
