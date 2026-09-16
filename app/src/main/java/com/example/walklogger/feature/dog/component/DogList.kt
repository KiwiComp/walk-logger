package com.example.walklogger.feature.dog.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.ChipSurface
import com.example.walklogger.core.ui.theme.Outline
import com.example.walklogger.core.ui.theme.SteelLight
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.feature.dog.DogListItemUiState
import com.example.walklogger.feature.dog.preview.DogPreviewData



private val DogAvatarSize = 44.dp

/** Every registered dog with an "Active" tag or a "Switch" button. */
@Composable
internal fun DogList(
    dogs: List<DogListItemUiState>,
    onSwitchDog: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        dogs.forEach { dog ->
            DogRow(dog = dog, onSwitch = { onSwitchDog(dog.id) })
            HorizontalDivider(color = Outline)
        }
    }
}

@Composable
private fun DogRow(dog: DogListItemUiState, onSwitch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            Modifier
                .size(DogAvatarSize)
                .background(SteelLight),
        )
        Spacer(Modifier.width(AppSpacing.lg))
        Text(text = dog.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (dog.isActive) {
            Text(
                text = stringResource(R.string.dog_active),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .background(ChipSurface)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            )
        } else {
            val description = stringResource(R.string.dog_switch_to, dog.name)
            Text(
                text = stringResource(R.string.dog_switch),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .border(1.dp, Outline)
                    .clickable(role = Role.Button, onClick = onSwitch)
                    .semantics { contentDescription = description }
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DogListPreview() {
    WalkLoggerTheme {
        DogList(dogs = DogPreviewData.profile.dogs, onSwitchDog = {}, modifier = Modifier.padding(AppSpacing.lg))
    }
}
