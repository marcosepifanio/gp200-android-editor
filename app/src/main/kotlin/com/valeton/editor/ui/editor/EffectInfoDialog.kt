package com.valeton.editor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.model.EffectDefinition
import com.valeton.editor.ui.components.PedalArtwork
import com.valeton.editor.ui.theme.StudioPalette

/** Whether the editor offers the full description of [effect] (BL-10). */
internal fun infoAvailable(effect: EffectDefinition?): Boolean = effect != null && effect.basedOn.isNotBlank()

/**
 * Centered dialog with the pedal artwork, the effect name and its full reference text, which the editor
 * header cuts to one line.
 */
@Composable
internal fun EffectInfoDialog(
    name: String,
    basedOn: String,
    category: String,
    svgFile: String,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioPalette.SurfaceRaised)
                        .border(1.dp, StudioPalette.Border, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PedalArtwork(svgFile = svgFile, name = name, accentColor = accentColor, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = name,
                    color = StudioPalette.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                if (category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioPalette.AccentGlow)
                            .border(1.dp, StudioPalette.Accent.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = category.uppercase(),
                            color = StudioPalette.Accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "BASEADO EM",
                        color = StudioPalette.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.6.sp
                    )
                    Text(text = basedOn, color = StudioPalette.TextSecondary, fontSize = 15.sp, lineHeight = 21.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("FECHAR", color = StudioPalette.Accent, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = StudioPalette.SurfaceElevated,
        textContentColor = StudioPalette.TextSecondary
    )
}
