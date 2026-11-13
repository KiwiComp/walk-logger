package com.example.walklogger.feature.dog.registerdog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppConfirmDialog
import com.example.walklogger.core.ui.component.AppDateField
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.HeaderTextButton
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate

@Suppress("ktlint:compose:parameter-naming")
@Composable
fun RegisterDogRoute(
    dogId: Long?,
    onDone: () -> Unit,
    onNotFound: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterDogViewModel =
        hiltViewModel<RegisterDogViewModel, RegisterDogViewModel.Factory> { it.create(dogId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDone by rememberUpdatedState(onDone)
    val currentOnNotFound by rememberUpdatedState(onNotFound)
    LaunchedEffect(uiState.isSaved, uiState.isDeleted, uiState.isNotFound) {
        when {
            uiState.isSaved || uiState.isDeleted -> currentOnDone()
            uiState.isNotFound -> currentOnNotFound()
        }
    }
    RegisterDogScreen(
        uiState = uiState,
        onNameChange = viewModel::setName,
        onBreedChange = viewModel::setBreed,
        onBirthDateChange = viewModel::setBirthDate,
        onWeightChange = viewModel::setWeight,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
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
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        WalkLoggerHeader(
            title = stringResource(
                if (uiState.isEditing) R.string.edit_dog_title else R.string.register_dog_title,
            ),
            navigationIcon = {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.back),
                    onClick = onCancel,
                )
            },
            actions = {
                if (uiState.isEditing) {
                    HeaderTextButton(
                        text = stringResource(R.string.edit_dog_delete),
                        onClick = { showDeleteConfirm = true },
                        visible = uiState.canDelete,
                    )
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
            AppDateField(
                date = uiState.birthDate,
                onDateChange = onBirthDateChange,
                label = stringResource(R.string.register_dog_birth_date),
                placeholder = stringResource(R.string.register_dog_birth_date_none),
            )
            AppTextField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = stringResource(R.string.register_dog_weight),
                suffix = stringResource(R.string.register_dog_weight_unit),
                isError = uiState.isWeightInvalid,
                supportingText = stringResource(R.string.register_dog_weight_error).takeIf {
                    uiState.isWeightInvalid
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
        AppButton(
            text = stringResource(R.string.register_dog_save),
            onClick = onSave,
            enabled = uiState.canSave || uiState.isSaving,
            loading = uiState.isSaving,
            modifier = Modifier
                .padding(horizontal = AppSpacing.xl)
                .padding(bottom = AppSpacing.xxl)
                .imePadding(),
        )
    }

    if (showDeleteConfirm) {
        AppConfirmDialog(
            title = stringResource(R.string.edit_dog_delete_confirm_title),
            message = stringResource(R.string.edit_dog_delete_confirm_message),
            confirmLabel = stringResource(R.string.edit_dog_delete_confirm),
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

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
            onDelete = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun EditDogScreenPreview() {
    WalkLoggerTheme {
        RegisterDogScreen(
            uiState = RegisterDogUiState(
                name = "Sixten",
                breed = "Border collie",
                weight = "18",
                isEditing = true,
            ),
            onNameChange = {},
            onBreedChange = {},
            onBirthDateChange = {},
            onWeightChange = {},
            onSave = {},
            onDelete = {},
            onCancel = {},
        )
    }
}
