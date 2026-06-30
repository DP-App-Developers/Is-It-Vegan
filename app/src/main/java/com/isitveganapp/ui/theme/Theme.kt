package com.isitveganapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = Brand700,
    onPrimary            = Color.White,
    primaryContainer     = Brand100,
    onPrimaryContainer   = Brand900,
    secondary            = Brand500,
    onSecondary          = Color.White,
    secondaryContainer   = Brand50,
    onSecondaryContainer = Brand900,
    background           = Gray50,
    onBackground         = Gray900,
    surface              = Color.White,
    onSurface            = Gray900,
    surfaceVariant       = Gray100,
    onSurfaceVariant     = Gray700,
    outline              = Gray300,
    outlineVariant       = Gray100,
    error                = DangerRed,
    onError              = Color.White,
    errorContainer       = DangerRedSurface,
    onErrorContainer     = Color(0xFF7F1D1D),
)

private val DarkColorScheme = darkColorScheme(
    primary              = Brand500,
    onPrimary            = Brand900,
    primaryContainer     = Brand900,
    onPrimaryContainer   = Brand100,
    secondary            = Brand500,
    onSecondary          = Brand900,
    secondaryContainer   = Color(0xFF1B4332),
    onSecondaryContainer = Brand100,
    background           = Color(0xFF111827),
    onBackground         = Gray100,
    surface              = Color(0xFF1F2937),
    onSurface            = Gray100,
    surfaceVariant       = Color(0xFF374151),
    onSurfaceVariant     = Gray300,
    outline              = Gray500,
    outlineVariant       = Color(0xFF374151),
    error                = DangerRedDark,
    onError              = Color(0xFF450A0A),
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Color(0xFFFECACA),
)

@Composable
fun IsItVeganTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) darkAppColors() else lightAppColors()
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography  = AppTypography,
            content     = content
        )
    }
}
