package com.example.walklogger.feature.logwalk.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.example.walklogger.core.ui.theme.AppTheme

/** Surface a form section sits on; mirrors [com.example.walklogger.core.ui.component.AppTextFieldStyle]. */
internal enum class FormStyle {
    /** Dark content on a light surface; the after-walk and past-walk forms. */
    Default,

    /** Light text on the header surface; the stool section during a walk. */
    Inverse,

    ;

    val text: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            Default -> AppTheme.colors.content
            Inverse -> AppTheme.colors.onHeader
        }

    val muted: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            Default -> AppTheme.colors.contentMuted
            Inverse -> AppTheme.colors.onHeaderMuted
        }

    val action: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            Default -> AppTheme.colors.action
            Inverse -> AppTheme.colors.onHeader
        }
}
