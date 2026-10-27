package com.example.walklogger.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.example.walklogger.core.ui.theme.WalkLoggerTheme

/** "Collar" / "Harness" tag shown on a logged walk. */
@Composable
fun EquipmentChip(equipment: Equipment, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(
            when (equipment) {
                Equipment.COLLAR -> R.string.equipment_collar
                Equipment.HARNESS -> R.string.equipment_harness
            },
        ),
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .background(AppTheme.colors.chip)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
}

@Preview(showBackground = true)
@Composable
private fun EquipmentChipPreview() {
    WalkLoggerTheme { EquipmentChip(equipment = Equipment.HARNESS) }
}
