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

private val DarkColorScheme = darkColorScheme(
  primary = AuraIndigoLight,
  onPrimary = Color.White,
  primaryContainer = AuraNavy,
  onPrimaryContainer = AuraCyanLight,
  secondary = AuraCyan,
  onSecondary = Color.Black,
  secondaryContainer = AuraCardDark,
  onSecondaryContainer = AuraTextPrimaryDark,
  tertiary = AuraViolet,
  onTertiary = Color.White,
  background = AuraObsidian,
  onBackground = AuraTextPrimaryDark,
  surface = AuraCardDark,
  onSurface = AuraTextPrimaryDark,
  surfaceVariant = AuraNavy,
  onSurfaceVariant = AuraTextSecondaryDark,
  outline = AuraBorderDark,
  outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
  primary = AuraIndigo,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFEEF2FF),
  onPrimaryContainer = AuraIndigoDark,
  secondary = AuraCyan,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFECFEFF),
  onSecondaryContainer = Color(0xFF0E7490),
  tertiary = AuraViolet,
  onTertiary = Color.White,
  background = AuraSnow,
  onBackground = AuraTextPrimaryLight,
  surface = AuraWhite,
  onSurface = AuraTextPrimaryLight,
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = AuraTextSecondaryLight,
  outline = AuraBorderLight,
  outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun AuraTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve bespoke aura branding
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
