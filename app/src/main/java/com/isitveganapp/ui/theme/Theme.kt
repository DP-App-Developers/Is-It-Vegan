package com.isitveganapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = Brand700,
    onPrimary          = Color.White,
    primaryContainer   = Brand100,
    onPrimaryContainer = Brand900,
    secondary          = Brand500,
    onSecondary        = Color.White,
    secondaryContainer = Brand50,
    onSecondaryContainer = Brand900,
    background         = Gray50,
    onBackground       = Gray900,
    surface            = Color.White,
    onSurface          = Gray900,
    surfaceVariant     = Gray100,
    onSurfaceVariant   = Gray700,
    outline            = Gray300,
    outlineVariant     = Gray100,
    error              = DangerRed,
    onError            = Color.White,
    errorContainer     = DangerRedSurface,
    onErrorContainer   = Color(0xFF7F1D1D),
)

@Composable
fun IsItVeganTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
