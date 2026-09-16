package com.example.walklogger.feature.logwalk.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R

/** Asks for a new medical concern's name; see [AddNameDialog]. */
@Composable
internal fun AddMedicalConcernDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    AddNameDialog(
        title = stringResource(R.string.medical_add_concern_title),
        label = stringResource(R.string.medical_add_concern_name),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
