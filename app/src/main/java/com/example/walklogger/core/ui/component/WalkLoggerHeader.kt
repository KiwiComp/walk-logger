package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/**
 * The dark header every tab shares. [navigationIcon], [title] (with an optional muted [subtitle]
 * beneath it) and [actions] sit on the first row; [content] is an optional slot below it for
 * tab-specific chrome such as the week strip.
 */
@Composable
fun WalkLoggerHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    CompositionLocalProvider(LocalContentColor provides AppTheme.colors.onHeader) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(AppTheme.colors.header)
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.xl)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xl),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                navigationIcon?.invoke()
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.headlineLarge)
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onHeaderMuted,
                        )
                    }
                }
                actions()
            }
            content()
        }
    }
}

@Preview
@Composable
private fun WalkLoggerHeaderPreview() {
    WalkLoggerTheme {
        WalkLoggerHeader(
            title = stringResource(R.string.log_title),
            actions = { DogBadge(name = "Sixten") },
        )
    }
}
