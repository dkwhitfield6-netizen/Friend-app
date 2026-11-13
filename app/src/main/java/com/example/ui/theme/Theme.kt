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
    primary = CyanPrimary,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = IndigoSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2C2F6E),
    onSecondaryContainer = Color(0xFFE0E0FF),
    tertiary = MintSuccess,
    onTertiary = Color(0xFF003920),
    background = SlateDarkBackground,
    onBackground = TextDarkPrimary,
    surface = SlateDarkSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = SlateDarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = CyanPrimaryLight,
    onPrimaryContainer = Color(0xFF001F29),
    secondary = IndigoSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E0FF),
    onSecondaryContainer = IndigoSecondaryDark,
    tertiary = MintSuccess,
    onTertiary = Color.White,
    background = SlateLightBackground,
    onBackground = TextLightPrimary,
    surface = SlateLightSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = SlateLightOutline
  )

@Composable
fun ReConnectTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to preserve the high-tech identity
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
