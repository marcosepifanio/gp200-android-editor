package com.valeton.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

private val SVG_DECODER_FACTORY = SvgDecoder.Factory()

@Composable
fun PedalArtwork(
    svgFile: String,
    name: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (svgFile.isBlank()) {
        PedalArtworkFallback(
            name = name,
            accentColor = accentColor,
            modifier = modifier
        )
    } else {
        val context = LocalContext.current
        val imageRequest = remember(svgFile, context) {
            ImageRequest.Builder(context)
                .data("file:///android_asset/pedals/$svgFile")
                .decoderFactory(SVG_DECODER_FACTORY)
                .build()
        }
        var isSuccess by remember(svgFile) { mutableStateOf(false) }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if (!isSuccess) {
                PedalArtworkFallback(
                    name = name,
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                onSuccess = { isSuccess = true },
                onError = { isSuccess = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PedalArtworkFallback(
    name: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val words = name.trim().split(Regex("[\\s\\-_/]+")).filter { it.isNotEmpty() }
    val initials = if (words.size >= 2) {
        val first = words[0].firstOrNull { it.isLetterOrDigit() } ?: ' '
        val second = words[1].firstOrNull { it.isLetterOrDigit() } ?: ' '
        "$first$second".trim().uppercase()
    } else {
        name.filter { it.isLetterOrDigit() }.take(2).uppercase()
    }.ifEmpty { "??" }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun PedalArtworkFallbackPreview() {
    Box(modifier = Modifier.size(80.dp, 40.dp)) {
        PedalArtworkFallback(
            name = "Bass Pre",
            accentColor = Color(0xFFE08040),
            modifier = Modifier.fillMaxSize()
        )
    }
}
