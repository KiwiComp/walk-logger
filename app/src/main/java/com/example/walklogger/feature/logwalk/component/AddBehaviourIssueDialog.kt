package com.example.walklogger.feature.logwalk.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R

/** Asks for a new behavioural issue's name; see [AddNameDialog]. */
@Composable
internal fun AddBehaviourIssueDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    AddNameDialog(
        title = stringResource(R.string.behaviour_add_issue_title),
        label = stringResource(R.string.behaviour_add_issue_name),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
