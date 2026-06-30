package com.isitveganapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val veganGreen: Color,
    val veganGreenSurface: Color,
    val dangerRed: Color,
    val dangerRedSurface: Color,
    val warnAmber: Color,
    val warnAmberSurface: Color,
    val textSecondary: Color,
    val textTertiary: Color,
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors() }

fun lightAppColors() = AppColors(
    veganGreen        = VeganGreen,
    veganGreenSurface = VeganGreenSurface,
    dangerRed         = DangerRed,
    dangerRedSurface  = DangerRedSurface,
    warnAmber         = WarnAmber,
    warnAmberSurface  = WarnAmberSurface,
    textSecondary     = Gray700,
    textTertiary      = Gray500,
)

fun darkAppColors() = AppColors(
    veganGreen        = VeganGreenDark,
    veganGreenSurface = VeganGreenSurfaceDark,
    dangerRed         = DangerRedDark,
    dangerRedSurface  = DangerRedSurfaceDark,
    warnAmber         = WarnAmberDark,
    warnAmberSurface  = WarnAmberSurfaceDark,
    textSecondary     = Gray300,
    textTertiary      = Gray400,
)

val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current
