package com.valeton.editor.ui.sync

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.core.codec.SysExCodec
import com.valeton.editor.ui.theme.StudioPalette
import com.valeton.editor.usb.FullSyncProgress

/**
 * Pure helper function for testability and label display.
 * Formats slot and preset name into Studio Rack format:
 * e.g. [14-B · #54/256] MODERN HIGHGAIN
 */
fun formatSyncStatusText(slot: Int, name: String): String {
    val clampedSlot = slot.coerceIn(0, 255)
    val rawLabel = SysExCodec.slotToLabel(clampedSlot)
    val bank = rawLabel.dropLast(1).padStart(2, '0')
    val letter = rawLabel.takeLast(1)
    val bankLabel = "$bank-$letter"
    val displaySlot = (clampedSlot + 1).toString().padStart(2, '0')
    val displayName = if (name.isNotBlank()) name.uppercase().trim() else "CARREGANDO..."
    return "[$bankLabel · #$displaySlot/256] $displayName"
}

/**
 * Fullscreen Lockout Screen displayed during hardware library synchronization (0..255).
 * Completely intercepts and consumes all touch events and system back navigation
 * while providing an authentic GP-200 Studio Rack animation with VU meters and glowing tube.
 */
@Composable
fun StudioFullSyncScreen(
    progress: FullSyncProgress?,
    modifier: Modifier = Modifier
) {
    // 1. Lockout: Suppress back navigation during sync
    BackHandler(enabled = true) {
        // No-op: Lockout during sync to protect USB transmission
    }

    // Smooth progress fraction
    val targetFraction = progress?.progressFraction ?: 0f
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(180, easing = LinearEasing),
        label = "animatedFraction"
    )

    // Memoize formatted status text to avoid string re-allocations when slot/name haven't changed
    val activeSlot = progress?.currentSlot?.minus(1) ?: 0
    val activeName = progress?.currentPatchName ?: ""
    val statusText = remember(activeSlot, activeName) {
        formatSyncStatusText(activeSlot, activeName)
    }

    // 2. Interception: Root Box consumes 100% of touch events on PointerEventPass.Initial
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioPalette.BackgroundDark)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Header Section: Logo + Pulsing Amber Indicator + Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PulsingStatusDot()

                    Text(
                        text = "VALETON GP-200 EDITOR",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 2.sp,
                        color = StudioPalette.TextPrimary
                    )
                }

                Text(
                    text = "SINCRONIZAÇÃO DA BIBLIOTECA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = StudioPalette.AmberLed
                )
            }

            // Centerpiece: Stereo VU Meters + Central Vacuum Tube
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioPalette.Surface)
                    .border(1.dp, StudioPalette.BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(vertical = 18.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left VU Meter Ladder (isolated animations)
                    VuMeterLadder(
                        channelLabel = "L",
                        animDurationMs = 650,
                        minLevel = 0.35f,
                        maxLevel = 0.92f
                    )

                    // Vacuum Tube Centerpiece with Breathing Glow (isolated animations & pre-cached drawing)
                    VacuumTubeCenterpiece()

                    // Right VU Meter Ladder (isolated animations)
                    VuMeterLadder(
                        channelLabel = "R",
                        animDurationMs = 820,
                        minLevel = 0.40f,
                        maxLevel = 0.86f
                    )
                }
            }

            // Sync Progress Section: Percentage + Custom Progress Bar + Status Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Percentage Text
                val percentage = (animatedFraction * 100).toInt().coerceIn(0, 100)
                Text(
                    text = "$percentage%",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = StudioPalette.TextPrimary
                )

                // Custom Studio Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioPalette.SurfaceRaised)
                        .border(1.dp, StudioPalette.BorderSubtle, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedFraction.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(StudioPalette.AmberLed, StudioPalette.Accent)
                                )
                            )
                    )
                }

                // Slot and Patch Status Label Text
                Text(
                    text = statusText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = StudioPalette.AmberLed,
                    textAlign = TextAlign.Center
                )
            }

            // USB Cable Protection Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioPalette.SurfaceElevated)
                    .border(1.dp, StudioPalette.BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Usb,
                        contentDescription = "USB",
                        tint = StudioPalette.AmberLed,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = "Sincronizando biblioteca completa com a pedaleira... Não desconecte o cabo USB nem feche o app.",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = StudioPalette.TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Isolated pulsing status indicator dot in the header.
 */
@Composable
private fun PulsingStatusDot(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "headerDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = modifier
            .size(10.dp)
            .graphicsLayer {
                scaleX = dotScale
                scaleY = dotScale
                alpha = dotAlpha
            }
            .clip(CircleShape)
            .background(StudioPalette.AmberLed)
    )
}

/**
 * 12-segment Studio LED VU Meter ladder with isolated animation.
 * - Bottom 7 segments (~60%): Green (StudioPalette.AccentGreen)
 * - Mid 3 segments (25%): Amber (StudioPalette.AmberLed)
 * - Top 2 segments (~15%): Red
 */
@Composable
private fun VuMeterLadder(
    channelLabel: String,
    animDurationMs: Int = 650,
    minLevel: Float = 0.35f,
    maxLevel: Float = 0.90f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vuMeter_$channelLabel")
    val level by infiniteTransition.animateFloat(
        initialValue = minLevel,
        targetValue = maxLevel,
        animationSpec = infiniteRepeatable(
            animation = tween(animDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vuLevel_$channelLabel"
    )

    val totalSegments = 12
    val activeCount = (level.coerceIn(0f, 1f) * totalSegments).toInt()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        // Render from top (segment 11) to bottom (segment 0)
        for (i in (totalSegments - 1) downTo 0) {
            val segmentColor = when {
                i >= 10 -> Color(0xFFFF4D4D) // Red (top 15%)
                i >= 7 -> StudioPalette.AmberLed // Amber (mid 25%)
                else -> StudioPalette.AccentGreen // Green (bottom 60%)
            }

            val isActive = i < activeCount
            val displayColor = if (isActive) segmentColor else segmentColor.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(displayColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = channelLabel,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = StudioPalette.TextMuted
        )
    }
}

/**
 * Central Vacuum Tube Graphic with Warm Breathing Glow.
 * Pre-caches Strokes, Corners, and Path to eliminate per-frame allocations during animation.
 */
@Composable
private fun VacuumTubeCenterpiece(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vacuumTube")
    val tubeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tubeGlowAlpha"
    )
    val tubeFilamentPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tubeFilamentPulse"
    )

    // Remember density-dependent drawing parameters and path outside onDraw
    val density = LocalDensity.current
    val strokeGlass = remember(density) { Stroke(width = with(density) { 1.5.dp.toPx() }) }
    val strokeGlassHighlight = remember(density) { CornerRadius(with(density) { 1.5.dp.toPx() }, with(density) { 1.5.dp.toPx() }) }
    val strokePlateBorder = remember(density) { Stroke(width = with(density) { 1.dp.toPx() }) }
    val strokeFilamentOuter = remember(density) { Stroke(width = with(density) { 4.5.dp.toPx() }, cap = StrokeCap.Round) }
    val strokeFilamentMid = remember(density) { Stroke(width = with(density) { 2.5.dp.toPx() }, cap = StrokeCap.Round) }
    val strokeFilamentInner = remember(density) { Stroke(width = with(density) { 1.2.dp.toPx() }, cap = StrokeCap.Round) }
    val strokeBase = remember(density) { Stroke(width = with(density) { 1.dp.toPx() }) }
    val filamentPath = remember { Path() }

    Box(
        modifier = modifier.size(width = 110.dp, height = 140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height * 0.46f

            // 1. Warm radial breathing glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StudioPalette.AmberLed.copy(alpha = 0.50f * tubeGlowAlpha),
                        Color(0xFFFF6F00).copy(alpha = 0.22f * tubeGlowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = size.width * 0.70f
                ),
                center = Offset(centerX, centerY),
                radius = size.width * 0.70f
            )

            // 2. Glass Envelope
            val tubeWidth = size.width * 0.52f
            val tubeHeight = size.height * 0.68f
            val tubeLeft = centerX - (tubeWidth / 2f)
            val tubeTop = size.height * 0.08f

            // Glass background tint
            drawRoundRect(
                color = Color(0x18ECEAE5),
                topLeft = Offset(tubeLeft, tubeTop),
                size = Size(tubeWidth, tubeHeight),
                cornerRadius = CornerRadius(tubeWidth / 2f, tubeWidth / 2f)
            )

            // Glass stroke outline
            drawRoundRect(
                color = Color(0x55ECEAE5),
                topLeft = Offset(tubeLeft, tubeTop),
                size = Size(tubeWidth, tubeHeight),
                cornerRadius = CornerRadius(tubeWidth / 2f, tubeWidth / 2f),
                style = strokeGlass
            )

            // Glass subtle reflection highlight on left side
            drawRoundRect(
                color = Color(0x35FFFFFF),
                topLeft = Offset(tubeLeft + 3.dp.toPx(), tubeTop + 6.dp.toPx()),
                size = Size(3.dp.toPx(), tubeHeight * 0.55f),
                cornerRadius = strokeGlassHighlight
            )

            // 3. Internal Anode / Plate Structure (Studio Grey Grid)
            val plateWidth = tubeWidth * 0.65f
            val plateHeight = tubeHeight * 0.50f
            val plateLeft = centerX - (plateWidth / 2f)
            val plateTop = tubeTop + (tubeHeight * 0.22f)

            drawRoundRect(
                color = Color(0x552B2E33),
                topLeft = Offset(plateLeft, plateTop),
                size = Size(plateWidth, plateHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            drawRoundRect(
                color = StudioPalette.BorderSubtle.copy(alpha = 0.7f),
                topLeft = Offset(plateLeft, plateTop),
                size = Size(plateWidth, plateHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = strokePlateBorder
            )

            // 4. Glowing Hot Filament (Inverted U wire with bright core)
            val filamentLeft = centerX - 6.dp.toPx()
            val filamentRight = centerX + 6.dp.toPx()
            val filamentBaseY = plateTop + plateHeight - 4.dp.toPx()
            val filamentPeakY = plateTop + 8.dp.toPx()

            filamentPath.reset()
            filamentPath.moveTo(filamentLeft, filamentBaseY)
            filamentPath.lineTo(filamentLeft, filamentPeakY + 4.dp.toPx())
            filamentPath.quadraticTo(centerX, filamentPeakY, filamentRight, filamentPeakY + 4.dp.toPx())
            filamentPath.lineTo(filamentRight, filamentBaseY)

            // Outer filament glow
            drawPath(
                path = filamentPath,
                color = Color(0xFFFF5722).copy(alpha = 0.85f * tubeFilamentPulse),
                style = strokeFilamentOuter
            )

            // Mid warm amber glow
            drawPath(
                path = filamentPath,
                color = StudioPalette.AmberLed.copy(alpha = tubeFilamentPulse),
                style = strokeFilamentMid
            )

            // Bright hot filament center
            drawPath(
                path = filamentPath,
                color = Color(0xFFFFF9C4).copy(alpha = tubeFilamentPulse),
                style = strokeFilamentInner
            )

            // 5. Metal Base and Contact Pins
            val baseWidth = tubeWidth * 1.12f
            val baseHeight = size.height * 0.14f
            val baseLeft = centerX - (baseWidth / 2f)
            val baseTop = tubeTop + tubeHeight - (baseHeight * 0.35f)

            // Anodized metal base
            drawRoundRect(
                color = StudioPalette.ChromeFace,
                topLeft = Offset(baseLeft, baseTop),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            drawRoundRect(
                color = StudioPalette.BorderActive,
                topLeft = Offset(baseLeft, baseTop),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = strokeBase
            )

            // Tube socket pins without allocating List
            val pinY = baseTop + baseHeight
            val pinHeight = 5.dp.toPx()
            val pinWidth = 2.5.dp.toPx()
            val pinStep = 8.dp.toPx()

            for (i in -2..2) {
                val xOff = i * pinStep
                drawRect(
                    color = Color(0xFF888B92),
                    topLeft = Offset(centerX + xOff - (pinWidth / 2f), pinY),
                    size = Size(pinWidth, pinHeight)
                )
            }
        }
    }
}
