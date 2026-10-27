package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme

/** The active dog's name tag shown in the header. Draws nothing while no dog is registered. */
@Composable
fun DogBadge(name: String?, modifier: Modifier = Modifier) {
    if (name == null) return
    Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.onHeader,
        modifier = modifier
            .background(AppTheme.colors.headerRaised)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
}
