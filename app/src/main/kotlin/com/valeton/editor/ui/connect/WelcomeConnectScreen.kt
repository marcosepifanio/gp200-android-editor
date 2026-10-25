package com.valeton.editor.ui.connect

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.BuildConfig
import com.valeton.editor.ui.theme.*
import com.valeton.editor.usb.MidiConnectionState

@Composable
fun WelcomeConnectScreen(
    connectionState: MidiConnectionState,
    onConnectClick: () -> Unit,
    onProceedOfflineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState is MidiConnectionState.Connected
    val isConnecting = connectionState is MidiConnectionState.Connecting || connectionState is MidiConnectionState.Handshaking

    // Subtle pulsing animation for connecting / idle state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF191D26),
                        Color(0xFF14171E)
                    )
                )
            )
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ValetonBlue.copy(alpha = 0.15f))
                        .border(1.dp, ValetonBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "EDITOR INDEPENDENTE",
                        color = ValetonBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "GP-200",
                    color = TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Editor de presets para Android",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Aplicativo não oficial da Valeton",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Center Visual & Status Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Hardware Icon with Glowing Ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(if (isConnecting) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                        .border(
                            2.dp,
                            if (isConnected) LedActive else if (isConnecting) ValetonBlue else BorderDark,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Cable,
                        contentDescription = "Status Hardware",
                        tint = if (isConnected) LedActive else if (isConnecting) ValetonBlue else TextSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Connection State Information Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val ledColor = when (connectionState) {
                                is MidiConnectionState.Connected -> LedActive
                                is MidiConnectionState.Connecting, is MidiConnectionState.Handshaking -> ValetonBlue
                                is MidiConnectionState.Error -> BypassRed
                                else -> LedInactive
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ledColor)
                            )

                            Text(
                                text = when (connectionState) {
                                    is MidiConnectionState.Connected -> "Pedaleira Conectada"
                                    is MidiConnectionState.Connecting -> "Conectando ao dispositivo..."
                                    is MidiConnectionState.Handshaking -> "Sincronizando com a Pedaleira..."
                                    is MidiConnectionState.Error -> "Erro de Conexão"
                                    else -> "Nenhuma Pedaleira Conectada"
                                },
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (connectionState) {
                                is MidiConnectionState.Connected -> connectionState.deviceName
                                is MidiConnectionState.Connecting -> "Identificando portas USB MIDI..."
                                is MidiConnectionState.Handshaking -> "Carregando configurações e patch ativo..."
                                is MidiConnectionState.Error -> connectionState.message
                                else -> "Conecte sua GP-200 via cabo USB-C OTG para sincronização em tempo real."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        if (isConnecting) {
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ValetonBlue,
                                trackColor = BorderDark
                            )
                        }
                    }
                }
            }

            // Bottom Actions (Connect / Continue Offline)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isConnected) {
                    // Enter Editor button when connected
                    Button(
                        onClick = onProceedOfflineClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ValetonBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Entrar no Editor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Connect Button
                    Button(
                        onClick = onConnectClick,
                        enabled = !isConnecting,
                        colors = ButtonDefaults.buttonColors(containerColor = ValetonBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (connectionState is MidiConnectionState.Handshaking) {
                                    "Sincronizando patch..."
                                } else {
                                    "Conectando..."
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Usb,
                                contentDescription = "Conectar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conectar Pedaleira",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Proceed Without Connecting (Offline Mode)
                OutlinedButton(
                    onClick = onProceedOfflineClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Continuar sem conectar (Modo Offline)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Dica: Use um cabo USB-C para USB-C ou adaptador OTG de alta velocidade para resposta imediata.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
