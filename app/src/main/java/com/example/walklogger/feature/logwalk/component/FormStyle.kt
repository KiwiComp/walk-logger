package com.example.walklogger.feature.logwalk.component

import androidx.compose.ui.graphics.Color
import com.example.walklogger.core.ui.theme.Ink
import com.example.walklogger.core.ui.theme.InkMuted
import com.example.walklogger.core.ui.theme.OnNavy
import com.example.walklogger.core.ui.theme.OnNavyMuted
import com.example.walklogger.core.ui.theme.Steel

/** Surface a form section sits on; mirrors [com.example.walklogger.core.ui.component.AppTextFieldStyle]. */
internal enum class FormStyle(
    val text: Color,
    val muted: Color,
    val action: Color,
) {
    /** Ink on a light surface; the after-walk and past-walk forms. */
    Default(text = Ink, muted = InkMuted, action = Steel),

    /** Light text on the header surface; the stool section during a walk. */
    Inverse(text = OnNavy, muted = OnNavyMuted, action = OnNavy),
}
