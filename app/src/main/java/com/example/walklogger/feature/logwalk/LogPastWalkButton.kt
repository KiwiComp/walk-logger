package com.example.walklogger.feature.logwalk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.CardSurface
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Outlined secondary action leading to the "Log a past walk" form. */
@Composable
internal fun LogPastWalkButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.buttonHeight)
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, Outline, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.new_walk_already_happened),
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogPastWalkButtonPreview() {
    WalkLoggerTheme {
        LogPastWalkButton(onClick = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
