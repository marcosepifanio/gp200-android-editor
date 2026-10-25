package com.valeton.editor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = StudioPalette.Accent,
    onPrimary = StudioPalette.Case,
    primaryContainer = StudioPalette.AccentDim,
    onPrimaryContainer = StudioPalette.TextPrimary,
    secondary = StudioPalette.AmberLed,
    onSecondary = StudioPalette.Case,
    background = StudioPalette.Case,
    onBackground = StudioPalette.TextPrimary,
    surface = StudioPalette.Surface,
    onSurface = StudioPalette.TextPrimary,
    surfaceVariant = StudioPalette.SurfaceElevated,
    onSurfaceVariant = StudioPalette.TextSecondary,
    outline = StudioPalette.Border,
    error = StudioPalette.Error,
    onError = StudioPalette.TextPrimary
)

@Composable
fun GP200Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = androidx.compose.material3.Typography(
            titleLarge = StudioTypography.Title,
            titleMedium = StudioTypography.Section,
            bodyLarge = StudioTypography.Body,
            bodyMedium = StudioTypography.Body,
            labelLarge = StudioTypography.Label,
            labelMedium = StudioTypography.Label,
            labelSmall = StudioTypography.Metadata
        ),
        content = content
    )
}
