package com.example.walklogger.feature.dog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.PillButton
import com.example.walklogger.core.ui.component.PillButtonStyle
import com.example.walklogger.core.ui.component.SectionLabel
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.dog.component.DogList
import com.example.walklogger.feature.dog.preview.DogPreviewData

@Composable
fun DogRoute(
    onAddDog: () -> Unit,
    onEditDog: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: DogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
    DogScreen(
        uiState = uiState,
        onSwitchDog = viewModel::switchDog,
        onAddDog = onAddDog,
        onEditDog = onEditDog,
        modifier = modifier,
    )
}

/** The Dogs tab: the registered dogs as cards, Add dog, and the deleted dogs apart. */
@Composable
fun DogScreen(
    uiState: DogUiState,
    onSwitchDog: (Long) -> Unit,
    onAddDog: () -> Unit,
    onEditDog: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(title = stringResource(R.string.dog_title))
        if (uiState.isLoading) return@Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            if (uiState.dogs.isEmpty()) {
                Text(
                    text = stringResource(R.string.dog_none_registered),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.contentMuted,
                )
            } else {
                SectionLabel(
                    text = stringResource(R.string.dog_section_dogs),
                    color = AppTheme.colors.contentMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
                DogList(dogs = uiState.dogs, onSwitchDog = onSwitchDog, onEditDog = onEditDog)
            }
            PillButton(
                text = stringResource(R.string.dog_add),
                onClick = onAddDog,
                icon = Icons.Filled.Add,
                style = PillButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth().heightIn(min = AppSize.buttonHeight),
            )
            if (uiState.deletedDogs.isNotEmpty()) {
                SectionLabel(
                    text = stringResource(R.string.dog_section_deleted),
                    color = AppTheme.colors.contentMuted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = AppSpacing.lg),
                )
                DogList(dogs = uiState.deletedDogs, onSwitchDog = onSwitchDog)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DogScreenPreview() {
    WalkLoggerTheme {
        DogScreen(uiState = DogPreviewData.profile, onSwitchDog = {}, onAddDog = {}, onEditDog = {})
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DogScreenNoDogPreview() {
    WalkLoggerTheme {
        DogScreen(uiState = DogUiState(isLoading = false), onSwitchDog = {
        }, onAddDog = {}, onEditDog = {})
    }
}
