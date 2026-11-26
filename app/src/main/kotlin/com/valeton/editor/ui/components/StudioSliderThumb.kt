package com.valeton.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valeton.editor.ui.theme.StudioPalette

internal val SliderThumbSize = 16.dp

/** Left edge of a thumb centred on [fraction] of [trackWidth] — the same point a tap there selects. */
internal fun sliderThumbOffset(trackWidth: Dp, fraction: Float, thumbSize: Dp = SliderThumbSize): Dp =
    trackWidth * fraction.coerceIn(0f, 1f) - thumbSize / 2

/** Round thumb for the custom horizontal sliders, centred on the end of the fill. */
@Composable
internal fun BoxWithConstraintsScope.StudioSliderThumb(fraction: Float) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = sliderThumbOffset(maxWidth, fraction))
            .size(SliderThumbSize)
            .clip(CircleShape)
            .background(StudioPalette.TextPrimary)
            .border(2.dp, StudioPalette.Accent, CircleShape)
    )
}
