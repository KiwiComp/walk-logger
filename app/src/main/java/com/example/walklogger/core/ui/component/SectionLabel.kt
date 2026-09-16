package com.example.walklogger.core.ui.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/** Small-caps label used above form sections and inside cells; [style] picks the size. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    Text(
        text = text.uppercase(),
        style = style,
        color = color,
        modifier = modifier,
    )
}
