package com.valeton.editor.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object StudioPalette {
    val Case = Color(0xFF141517)
    val BackgroundDark = Case
    val AmberLed = Color(0xFFFFA23F)
    val Surface = Color(0xFF1E2023)
    val SurfaceRaised = Color(0xFF24262A)
    val SurfaceElevated = Color(0xFF2A2D31)
    val ChromeBg = Color(0xFF202225)
    val ChromeFace = Color(0xFF2B2E33)

    val BorderSubtle = Color(0xFF33363B)
    val Border = Color(0xFF35383D)
    val BorderActive = Color(0xFF4D515A)

    val Accent = Color(0xFF4FD6EE)       // Neon Studio Cyan
    val AccentDim = Color(0xFF33AEC4)
    val AccentRed = Color(0xFFE2685C)
    val AccentGreen = Color(0xFF4FBD7D)
    val AccentGlow = Color(0x554FD6EE)

    val TextPrimary = Color(0xFFECEAE5)
    val TextSecondary = Color(0xFFC5C1B9)
    val TextMuted = Color(0xFF98938A)

    val KnobTrack = Color(0xFF3A3D42)
    val KnobFill = Accent
    val LedOff = Color(0xFF222428)

    val Error = AccentRed
}

object StudioTypography {
    val Title = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val Section = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    val Body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val Label = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
    val Metadata = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
}

data class ModuleBodySpec(
    val body: Color,
    val bodyDeep: Color,
    val ink: Color,
    val led: Color
)

val MODULE_SPECS: Map<String, ModuleBodySpec> = mapOf(
    "PRE" to ModuleBodySpec(Color(0xFFECE4D4), Color(0xFFD8CCB4), Color(0xFF2B2620), Color(0xFFFF4D4D)),
    "WAH" to ModuleBodySpec(Color(0xFF8A4DD8), Color(0xFF6E3AB5), Color(0xFFF4EEFC), Color(0xFFFF4D4D)),
    "DST" to ModuleBodySpec(Color(0xFFB8402E), Color(0xFF93301F), Color(0xFFFCEEEA), Color(0xFFFF4D4D)),
    "AMP" to ModuleBodySpec(Color(0xFF211D18), Color(0xFF171310), Color(0xFFE8C890), Color(0xFFFFA23F)),
    "NR"  to ModuleBodySpec(Color(0xFFC9CFD2), Color(0xFFA9B0B4), Color(0xFF23282B), Color(0xFF4DFF88)),
    "CAB" to ModuleBodySpec(Color(0xFF3A3D3F), Color(0xFF2C2E30), Color(0xFFDFE4E6), Color(0xFFFFA23F)),
    "EQ"  to ModuleBodySpec(Color(0xFFF1F1EC), Color(0xFFDEDEED), Color(0xFF26282A), Color(0xFFFF4D4D)),
    "MOD" to ModuleBodySpec(Color(0xFF2F6FD8), Color(0xFF2455AB), Color(0xFFEEF4FF), Color(0xFF4DA6FF)),
    "DLY" to ModuleBodySpec(Color(0xFF5A5FD8), Color(0xFF4547AB), Color(0xFFEEEEFF), Color(0xFF4DA6FF)),
    "RVB" to ModuleBodySpec(Color(0xFF2FB9C9), Color(0xFF1F8B98), Color(0xFF07272B), Color(0xFF4DFFE0)),
    "VOL" to ModuleBodySpec(Color(0xFF2E2E33), Color(0xFF232327), Color(0xFFE8E8EA), Color(0xFFFFFFFF))
)

fun getModuleSpec(moduleName: String): ModuleBodySpec {
    return MODULE_SPECS[moduleName] ?: MODULE_SPECS["VOL"]!!
}
