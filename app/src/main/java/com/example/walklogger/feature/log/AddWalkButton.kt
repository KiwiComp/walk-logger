package com.example.walklogger.feature.log

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

private val AddWalkButtonHeight = 64.dp

/** Dashed call-to-action shown under the day's walks. */
@Composable
internal fun AddWalkButton(dayLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AddWalkButtonHeight)
            .drawBehind {
                drawRect(color = Outline, style = Stroke(width = 3f, pathEffect = dash))
            }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+ Add a walk for $dayLabel",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWalkButtonPreview() {
    WalkLoggerTheme {
        AddWalkButton(dayLabel = "Sat", onClick = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
