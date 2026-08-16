package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalPrimaryDarkTheme,
    onPrimary = Color(0xFF383000),
    primaryContainer = Color(0xFF534800),
    onPrimaryContainer = Color(0xFFFFE261),
    secondary = Color(0xFFD6C3A1),
    onSecondary = Color(0xFF3B2F15),
    secondaryContainer = Color(0xFF534427),
    onSecondaryContainer = Color(0xFFF3DFBB),
    tertiary = Color(0xFFB8CEAF),
    onTertiary = Color(0xFF243621),
    background = NaturalBackgroundDark,
    surface = NaturalSurfaceDark,
    surfaceVariant = NaturalSurfaceVariantDark,
    onBackground = NaturalOnSurfaceDark,
    onSurface = NaturalOnSurfaceDark,
    onSurfaceVariant = NaturalOnSurfaceVariantDark,
    outline = Color(0xFF4D483F),
    outlineVariant = Color(0xFF33302B)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalPrimary,
    onPrimary = Color.White,
    primaryContainer = NaturalPrimaryContainer,
    onPrimaryContainer = NaturalOnPrimaryContainer,
    secondary = NaturalSecondary,
    onSecondary = Color.White,
    secondaryContainer = NaturalSecondaryContainer,
    onSecondaryContainer = NaturalSecondary,
    tertiary = NaturalTertiary,
    onTertiary = Color.White,
    background = NaturalBackground,
    surface = NaturalSurface,
    surfaceVariant = NaturalSurfaceVariant,
    onBackground = NaturalOnBackground,
    onSurface = NaturalOnSurface,
    onSurfaceVariant = NaturalOnSurfaceVariant,
    outline = NaturalOutline,
    outlineVariant = NaturalOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent Natural Tones branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


