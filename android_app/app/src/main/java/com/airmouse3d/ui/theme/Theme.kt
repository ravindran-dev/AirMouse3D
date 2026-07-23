package com.airmouse3d.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = AirBlue80,
    secondary = AirBlueGrey80,
    tertiary = AirCyan80,
    background = AirBackgroundDark,
    surface = AirSurfaceDark,
)

private val LightColors = lightColorScheme(
    primary = AirBlue40,
    secondary = AirBlueGrey40,
    tertiary = AirCyan40,
    background = AirBackgroundLight,
    surface = AirSurfaceLight,
)

@Composable
fun AirMouse3DTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AirMouseTypography,
        content = content,
    )
}
