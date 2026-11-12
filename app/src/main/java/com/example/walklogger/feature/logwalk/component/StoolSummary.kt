package com.example.walklogger.feature.logwalk.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** The folded description rows as one line, "Normal · Firm · Medium brown"; tapping it opens them again. */
@Composable
internal fun StoolSummary(
    stool: Stool?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FormStyle = FormStyle.Default,
) {
    Text(
        text = stool.summary(),
        style = MaterialTheme.typography.bodyMedium,
        color = style.text,
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = AppSpacing.xs),
    )
}

/** "No details" before any is picked. */
@Composable
private fun Stool?.summary(): String {
    val parts = listOfNotNull(
        this?.quantity?.labelRes,
        this?.texture?.labelRes,
        this?.colour?.labelRes,
    )
        .map { stringResource(it) }
    return if (parts.isEmpty()) {
        stringResource(
            R.string.stool_no_details,
        )
    } else {
        parts.joinToString(" · ")
    }
}

@Preview(showBackground = true)
@Composable
private fun StoolSummaryPreview() {
    WalkLoggerTheme {
        StoolSummary(
            stool = Stool(
                passed = true,
                quantity = StoolQuantity.NORMAL,
                texture = StoolTexture.FIRM,
                colour = StoolColour.MEDIUM_BROWN,
            ),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
