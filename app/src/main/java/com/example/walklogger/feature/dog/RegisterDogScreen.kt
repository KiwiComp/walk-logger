package com.example.walklogger.feature.dog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun RegisterDogRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterDogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onDone()
    }
    RegisterDogScreen(
        uiState = uiState,
        onNameChange = viewModel::setName,
        onBreedChange = viewModel::setBreed,
        onBirthDateChange = viewModel::setBirthDate,
        onWeightChange = viewModel::setWeight,
        onSave = viewModel::save,
        onCancel = onDone,
        modifier = modifier,
    )
}

@Composable
fun RegisterDogScreen(
    uiState: RegisterDogUiState,
    onNameChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onBirthDateChange: (LocalDate?) -> Unit,
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(R.string.register_dog_title),
            actions = {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.register_dog_cancel), style = MaterialTheme.typography.titleSmall)
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            AppTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.register_dog_name),
            )
            AppTextField(
                value = uiState.breed,
                onValueChange = onBreedChange,
                label = stringResource(R.string.register_dog_breed),
            )
            BirthDateField(birthDate = uiState.birthDate, onBirthDateChange = onBirthDateChange)
            AppTextField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = stringResource(R.string.register_dog_weight),
                suffix = stringResource(R.string.register_dog_weight_unit),
                isError = uiState.isWeightInvalid,
                supportingText = stringResource(R.string.register_dog_weight_error).takeIf { uiState.isWeightInvalid },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
        AppButton(
            text = stringResource(R.string.register_dog_save),
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier
                .padding(horizontal = AppSpacing.xl)
                .padding(bottom = AppSpacing.xxl)
                .imePadding(),
        )
    }
}

/** Read-only text field that opens a date picker when pressed. */
@Composable
private fun BirthDateField(birthDate: LocalDate?, onBirthDateChange: (LocalDate?) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) showPicker = true }
    }
    AppTextField(
        value = birthDate?.format(formatter).orEmpty(),
        onValueChange = {},
        label = stringResource(R.string.register_dog_birth_date),
        placeholder = stringResource(R.string.register_dog_birth_date_none),
        readOnly = true,
        interactionSource = interactionSource,
    )
    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBirthDateChange(pickerState.selectedDateMillis?.let { it.toLocalDate() })
                        showPicker = false
                    },
                ) {
                    Text(stringResource(R.string.register_dog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.register_dog_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// DatePicker reports UTC midnight of the chosen day.
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun RegisterDogScreenPreview() {
    WalkLoggerTheme {
        RegisterDogScreen(
            uiState = RegisterDogUiState(name = "Sixten", breed = "Border collie", weight = "18"),
            onNameChange = {},
            onBreedChange = {},
            onBirthDateChange = {},
            onWeightChange = {},
            onSave = {},
            onCancel = {},
        )
    }
}
