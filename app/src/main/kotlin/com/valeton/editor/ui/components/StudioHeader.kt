package com.valeton.editor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.ui.theme.StudioPalette

@Composable
fun StudioHeader(
    connected: Boolean,
    patchName: String,
    currentSlot: Int,
    hasUnsavedChanges: Boolean,
    syncProgress: Int,
    onPatchClick: () -> Unit,
    onSaveClick: () -> Unit,
    onImportClick: (() -> Unit)? = null,
    saving: Boolean = false,
    modifier: Modifier = Modifier
) {
    val slotLabel = try {
        SysExCodec.slotToLabel(currentSlot)
    } catch (_: Exception) {
        "%02d".format(currentSlot)
    }

    // Pulse animation for connection indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val saveBtnBorder by animateColorAsState(
        targetValue = if (hasUnsavedChanges) StudioPalette.Accent else StudioPalette.Border,
        animationSpec = tween(200),
        label = "saveBorder"
    )

    val saveBtnText by animateColorAsState(
        targetValue = if (hasUnsavedChanges) StudioPalette.Accent else StudioPalette.TextMuted,
        animationSpec = tween(200),
        label = "saveText"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioPalette.ChromeBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // 2. Central Unified Preset Card [Slot/Status Sub-line + Large Preset Name]
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioPalette.Surface)
                    .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                    .clickable(onClick = onPatchClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sub-line: [Dot] [SlotLabel] · PATCH    [✎]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Connection Status Dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (connected) StudioPalette.Accent.copy(alpha = dotAlpha)
                                    else StudioPalette.AccentRed
                                )
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        // Slot Badge e.g. 01-A
                        Text(
                            text = slotLabel,
                            color = StudioPalette.Accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeight = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "·",
                            color = StudioPalette.TextMuted,
                            fontSize = 12.sp,
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeight = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "PRESET",
                            color = StudioPalette.TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeight = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f))


                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Nome",
                            tint = StudioPalette.TextMuted.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Main Line: Preset Name
                    Text(
                        text = patchName.ifBlank { "Sem nome" },
                        color = StudioPalette.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // 2. Action: IMPORT Button (if provided)
            if (onImportClick != null) {
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioPalette.Surface)
                        .border(1.dp, StudioPalette.Border, RoundedCornerShape(8.dp))
                        .clickable(onClick = onImportClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = "Importar Patch",
                        tint = StudioPalette.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 5. Action: Dedicated SAVE Button
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (hasUnsavedChanges) StudioPalette.AccentGlow.copy(alpha = 0.2f)
                        else StudioPalette.Surface
                    )
                    .border(1.dp, saveBtnBorder, RoundedCornerShape(8.dp))
                    .clickable(enabled = !saving, onClick = onSaveClick)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasUnsavedChanges) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StudioPalette.Accent)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (saving) "SALVANDO…" else "SALVAR",
                        color = saveBtnText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Bottom Synchronization Progress Bar (3dp)
        AnimatedVisibility(visible = syncProgress in 1..255) {
            val progressFraction = (syncProgress / 256f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(StudioPalette.Border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .background(StudioPalette.Accent)
                )
            }
        }
    }
}
