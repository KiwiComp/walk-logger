package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme

/** The active dog's name tag shown in the header. Draws nothing while no dog is registered. */
@Composable
fun DogBadge(name: String?, modifier: Modifier = Modifier, isDeleted: Boolean = false) {
    if (name == null) return
    Text(
        text = if (isDeleted) stringResource(R.string.dog_name_deleted, name) else name,
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colors.onActionLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .widthIn(max = AppSize.dogBadgeMaxWidth)
            .background(AppTheme.colors.actionLight, CircleShape)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    )
}
