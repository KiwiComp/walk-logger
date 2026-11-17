package com.example.walklogger.feature.dog.registerdog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.walklogger.R
import com.example.walklogger.core.ui.component.AppButton
import com.example.walklogger.core.ui.component.AppConfirmDialog
import com.example.walklogger.core.ui.component.AppDateField
import com.example.walklogger.core.ui.component.AppTextField
import com.example.walklogger.core.ui.component.HeaderIconButton
import com.example.walklogger.core.ui.component.WalkLoggerHeader
import com.example.walklogger.core.ui.message.UserMessageEffect
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Suppress("ktlint:compose:parameter-naming")
@Composable
fun RegisterDogRoute(
    dogId: Long?,
    onDone: () -> Unit,
    onNotFound: () -> Unit,
    snackbarHostState: SnackbarHostState,
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
    UserMessageEffect(
        message = uiState.userMessage,
        snackbarHostState = snackbarHostState,
        onConsume = viewModel::userMessageShown,
    )
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
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DogFormCard(
                uiState = uiState,
                onNameChange = onNameChange,
                onBreedChange = onBreedChange,
                onBirthDateChange = onBirthDateChange,
                onWeightChange = onWeightChange,
            )
            if (uiState.isEditing && uiState.canDelete) {
                DeleteDogButton(onClick = { showDeleteConfirm = true })
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.card)
                .imePadding(),
        ) {
            HorizontalDivider(color = AppTheme.colors.outline)
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val saveLabel = if (uiState.isEditing) {
                    R.string.edit_dog_save
                } else {
                    R.string.register_dog_save
                }
                AppButton(
                    text = stringResource(saveLabel),
                    onClick = onSave,
                    enabled = uiState.canSave || uiState.isSaving,
                    loading = uiState.isSaving,
                )
                if (uiState.name.isBlank() && !uiState.isLoading) {
                    Text(
                        text = stringResource(R.string.register_dog_needs_name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.contentSecondary,
                    )
                }
            }
        }
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

@Composable
private fun DogFormCard(
    uiState: RegisterDogUiState,
    onNameChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onBirthDateChange: (LocalDate?) -> Unit,
    onWeightChange: (String) -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.xl)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.card)
            .border(1.dp, AppTheme.colors.outline, shape)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        AppTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.register_dog_name),
            placeholder = stringResource(R.string.register_dog_name_placeholder),
        )
        AppTextField(
            value = uiState.breed,
            onValueChange = onBreedChange,
            label = stringResource(R.string.register_dog_breed),
            labelHint = stringResource(R.string.register_dog_optional),
            placeholder = stringResource(R.string.register_dog_breed_placeholder),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            AppDateField(
                date = uiState.birthDate,
                onDateChange = onBirthDateChange,
                label = stringResource(R.string.register_dog_birth_date),
                placeholder = stringResource(R.string.register_dog_birth_date_none),
                formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                modifier = Modifier.weight(1f),
            )
            AppTextField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = stringResource(R.string.register_dog_weight),
                placeholder = stringResource(R.string.register_dog_weight_placeholder),
                suffix = stringResource(R.string.register_dog_weight_unit),
                isError = uiState.isWeightInvalid,
                supportingText = stringResource(R.string.register_dog_weight_error).takeIf {
                    uiState.isWeightInvalid
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(R.string.register_dog_optional_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.contentSecondary,
        )
    }
}

@Composable
private fun DeleteDogButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
            tint = AppTheme.colors.danger,
        )
        Text(
            text = stringResource(R.string.edit_dog_delete),
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.danger,
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
