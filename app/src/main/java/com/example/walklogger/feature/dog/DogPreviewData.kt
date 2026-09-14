package com.example.walklogger.feature.dog

/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object DogPreviewData {
    val profile = DogUiState.Profile(
        active = DogProfileUiState(name = "Sixten", breed = "Border collie", ageYears = 3, weightKg = 18.0),
        dogs = listOf(
            DogListItemUiState(id = 1, name = "Sixten", isActive = true),
            DogListItemUiState(id = 2, name = "Mira", isActive = false),
        ),
    )
}
