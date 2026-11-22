package com.example.walklogger.core.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.walklogger.R
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.ui.theme.AppRadius
import com.example.walklogger.core.ui.theme.AppSize
import com.example.walklogger.core.ui.theme.AppSpacing
import com.example.walklogger.core.ui.theme.AppTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * A walk's route on a Google map, zoomed so every point shows; nothing for fewer than two points.
 * Lite mode draws the map as a still image that takes no gestures, so it can sit in a scrolling
 * screen. The map itself stays light in the dark theme, as lite mode has no colour scheme (#83).
 */
@Composable
fun WalkRouteMap(route: List<Coordinates>, modifier: Modifier = Modifier) {
    val points = remember(route) { route.map { LatLng(it.latitude, it.longitude) } }
    if (points.size < 2) return
    val description = stringResource(R.string.walk_route_map)
    val shape = RoundedCornerShape(AppRadius.xl)
    val cameraPositionState = rememberCameraPositionState()
    val padding = with(LocalDensity.current) { AppSpacing.xxl.roundToPx() }
    var isLoaded by remember { mutableStateOf(false) }
    // CameraUpdateFactory only works once the map exists, so the camera moves after it has loaded.
    LaunchedEffect(points, isLoaded) {
        if (!isLoaded) return@LaunchedEffect
        val bounds = LatLngBounds.builder().apply { points.forEach(::include) }.build()
        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.mapHeight)
            .clip(shape)
            .border(1.dp, AppTheme.colors.outline, shape)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            googleMapOptionsFactory = {
                GoogleMapOptions().liteMode(true).mapToolbarEnabled(false)
            },
            uiSettings = MapUiSettings(mapToolbarEnabled = false, zoomControlsEnabled = false),
            // Without a click listener a lite mode map opens the Google Maps app when tapped.
            onMapClick = {},
            onMapLoaded = { isLoaded = true },
        ) {
            Polyline(points = points, color = AppTheme.colors.action)
        }
    }
}
