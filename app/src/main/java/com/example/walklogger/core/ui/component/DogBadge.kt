package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.walklogger.core.ui.theme.NavyLight
import com.example.walklogger.core.ui.theme.OnNavy

/** The small "Sixten" tag shown in the header. */
@Composable
fun DogBadge(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        color = OnNavy,
        modifier = modifier
            .background(NavyLight)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
