package com.example.walklogger

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.walklogger.core.ui.theme.WalkLoggerTheme
import com.example.walklogger.navigation.WalkLoggerApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. All screens are Composables navigated to via [WalkLoggerApp];
 * this class should stay thin.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Every tab has a dark header, so status-bar icons are always light.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            WalkLoggerTheme {
                WalkLoggerApp()
            }
        }
    }
}
