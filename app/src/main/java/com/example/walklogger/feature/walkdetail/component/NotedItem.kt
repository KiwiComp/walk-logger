package com.example.walklogger.feature.walkdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val BulletSize = 12.dp

/** A behaviour issue or medical concern the walk recorded, marked with a square bullet. */
@Composable
internal fun NotedItem(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(BulletSize).background(AppTheme.colors.action))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.content)
    }
}

@Preview(showBackground = true)
@Composable
private fun NotedItemPreview() {
    WalkLoggerTheme {
        NotedItem(label = "Pulling", modifier = Modifier.padding(AppSpacing.lg))
    }
}
