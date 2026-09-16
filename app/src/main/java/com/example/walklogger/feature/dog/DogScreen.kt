package com.example.walklogger.feature.dog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.DashedButton
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.dog.component.DogList
import com.example.walklogger.feature.dog.preview.DogPreviewData
import java.text.NumberFormat



@Composable
fun DogRoute(
    onAddDog: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DogScreen(
        uiState = uiState,
        onSwitchDog = viewModel::switchDog,
        onAddDog = onAddDog,
        modifier = modifier,
    )
}

/** The dog's own tab. Title is the dog's name, so no badge in the header here. */
@Composable
fun DogScreen(
    uiState: DogUiState,
    onSwitchDog: (Long) -> Unit,
    onAddDog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            DogUiState.Loading -> Unit
            DogUiState.NoDog -> NoDogContent(onAddDog = onAddDog)
            is DogUiState.Profile -> ProfileContent(uiState = uiState, onSwitchDog = onSwitchDog, onAddDog = onAddDog)
        }
    }
}

@Composable
private fun NoDogContent(onAddDog: () -> Unit) {
    WalkLoggerHeader(title = stringResource(R.string.dog_title))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.dog_none_registered),
            style = MaterialTheme.typography.bodyLarge,
            color = InkMuted,
        )
        DashedButton(text = stringResource(R.string.dog_add_first), onClick = onAddDog)
    }
}

@Composable
private fun ProfileContent(
    uiState: DogUiState.Profile,
    onSwitchDog: (Long) -> Unit,
    onAddDog: () -> Unit,
) {
    WalkLoggerHeader(title = uiState.active.name) {
        val subtitle = uiState.active.subtitle()
        if (subtitle.isNotEmpty()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = OnNavyMuted)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
    ) {
        SectionLabel(text = stringResource(R.string.dog_section_dogs), color = InkMuted)
        Spacer(Modifier.height(AppSpacing.sm))
        DogList(dogs = uiState.dogs, onSwitchDog = onSwitchDog)
        DashedButton(
            text = stringResource(R.string.dog_add_another),
            onClick = onAddDog,
            modifier = Modifier.padding(top = AppSpacing.lg),
        )
    }
}

/** "Border collie · 3 years · 18 kg", skipping whatever isn't known. */
@Composable
private fun DogProfileUiState.subtitle(): String {
    val locale = LocalConfiguration.current.locales[0]
    val weight = weightKg?.let { kg ->
        val formatted = NumberFormat.getInstance(locale).apply { maximumFractionDigits = 1 }.format(kg)
        stringResource(R.string.dog_weight_kg, formatted)
    }
    return listOfNotNull(
        breed.takeIf { it.isNotBlank() },
        ageYears?.let { pluralStringResource(R.plurals.dog_age_years, it, it) },
        weight,
    ).joinToString(" · ")
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DogScreenPreview() {
    WalkLoggerTheme {
        DogScreen(uiState = DogPreviewData.profile, onSwitchDog = {}, onAddDog = {})
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DogScreenNoDogPreview() {
    WalkLoggerTheme {
        DogScreen(uiState = DogUiState.NoDog, onSwitchDog = {}, onAddDog = {})
    }
}
