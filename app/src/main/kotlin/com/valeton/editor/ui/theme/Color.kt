package com.valeton.editor.ui.theme

import androidx.compose.ui.graphics.Color

// Compatibility aliases for active screens migrating to the canonical studio palette.
val BackgroundDark = StudioPalette.Case
val BackgroundCard = StudioPalette.SurfaceRaised
val SurfaceDark = StudioPalette.Surface
val SurfaceVariantDark = StudioPalette.SurfaceElevated
val SurfaceElevated = StudioPalette.SurfaceElevated

val ValetonBlue = StudioPalette.Accent
val ValetonBlueDark = StudioPalette.AccentDim
val ValetonBlueLight = StudioPalette.Accent
val BypassRed = StudioPalette.AccentRed
val AmberAccent = StudioPalette.AmberLed
val AmberAccentDim = StudioPalette.AmberLed.copy(alpha = 0.72f)
val LedActive = StudioPalette.AmberLed
val LedInactive = StudioPalette.TextMuted

val TextPrimary = StudioPalette.TextPrimary
val TextSecondary = StudioPalette.TextSecondary
val TextMuted = StudioPalette.TextMuted
val BorderDark = StudioPalette.Border
val BorderFocused = StudioPalette.Accent

// Module Colors (Matching official pedal blocks)
val ModuleNrColor = Color(0xFF454E5B)
val ModulePreColor = Color(0xFFC026D3)
val ModuleDstColor = Color(0xFFE53935)
val ModuleAmpColor = Color(0xFFA06237)
val ModuleCabColor = Color(0xFF3B4252)
val ModuleEqColor = Color(0xFFC5A059)
val ModuleModColor = Color(0xFF00BCD4)
val ModuleDlyColor = Color(0xFFE91E63)
val ModuleRvbColor = Color(0xFF7C3AED)
val ModuleWahColor = Color(0xFF9333EA)
val ModuleVolColor = Color(0xFF607D8B)
