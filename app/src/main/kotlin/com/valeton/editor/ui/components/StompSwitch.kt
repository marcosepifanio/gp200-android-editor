package com.valeton.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valeton.editor.ui.theme.LedActive
import com.valeton.editor.ui.theme.LedInactive

@Composable
fun StompSwitch(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clickable { onToggle() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status LED
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (enabled) LedActive else LedInactive)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Footswitch metallic button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF4A4D54)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8C929D))
            )
        }
    }
}
