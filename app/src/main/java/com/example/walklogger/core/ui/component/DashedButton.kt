package com.example.walklogger.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** Dashed, full-width call-to-action ("+ Add a walk", "+ Add another dog"). */
@Composable
fun DashedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
    val outlineColor = AppTheme.colors.outline
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.buttonHeight)
            .clip(shape)
            .drawBehind {
                drawOutline(
                    color = outlineColor,
                    outline = shape.createOutline(size, layoutDirection, this),
                    style = Stroke(width = 3f, pathEffect = dash))
            }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.content,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashedButtonPreview() {
    WalkLoggerTheme {
        DashedButton(text = "+ Add another dog", onClick = {})
    }
}
