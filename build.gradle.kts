// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.spotless)
}

// Passed explicitly: Spotless does not pick these up from .editorconfig.
val ktlintSettings = mapOf(
    "ktlint_code_style" to "android_studio",
    "ij_kotlin_allow_trailing_comma" to "true",
    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
    // Test names in backticks are sentences and can't be wrapped.
    "ktlint_ignore_back_ticked_identifier" to "true",
)

spotless {
    kotlin {
        target("app/src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .customRuleSets(listOf(libs.compose.rules.ktlint.get().toString()))
            .editorConfigOverride(
                ktlintSettings +
                    // AppTheme.colors reads this local so components never see the private palette.
                    mapOf("compose_allowed_composition_locals" to "LocalAppColors"),
            )
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintSettings)
    }
}
