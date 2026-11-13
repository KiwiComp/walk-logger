package com.example.walklogger.core.ui.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * What tapping [AddWalkButton] on the Log and Calendar tabs does for [date]: without a dog that can log walks a
 * snackbar offers the dogs tab; otherwise today opens the timer and other days log a past walk.
 */
@Composable
fun addWalkClick(
    date: LocalDate,
    canAddWalk: Boolean,
    isActiveDogDeleted: Boolean,
    canStartLiveWalk: Boolean,
    snackbarHostState: SnackbarHostState,
    onAddWalk: (LocalDate) -> Unit,
    onLogPastWalk: (LocalDate) -> Unit,
    onGoToDogs: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    val noDogMessage = stringResource(
        if (isActiveDogDeleted) R.string.log_deleted_dog_message else R.string.log_no_dog_message,
    )
    val noDogAction = stringResource(R.string.log_no_dog_action)
    return {
        when {
            !canAddWalk -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = noDogMessage,
                    actionLabel = noDogAction,
                )
                if (result == SnackbarResult.ActionPerformed) onGoToDogs()
            }

            canStartLiveWalk -> onAddWalk(date)

            else -> onLogPastWalk(date)
        }
    }
}
