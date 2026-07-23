package com.airmouse3d.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airmouse3d.model.SensorStatus
import com.airmouse3d.model.TrackingState
import com.airmouse3d.ui.theme.StatusError
import com.airmouse3d.ui.theme.StatusGood
import com.airmouse3d.ui.theme.StatusWarning
import com.airmouse3d.viewmodel.HomeUiState

private enum class HeroTone { SUCCESS, WARNING, ERROR, NEUTRAL }

private data class HeroPresentation(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tone: HeroTone,
    val pulsing: Boolean,
)

/**
 * The one thing this screen leads with: is the phone actually driving the PC's cursor right
 * now. Pairing is a single QR scan (no typing, no Firebase project setup); this card turns
 * that state -- and the live, ack-based reachability of the PC once tracking starts -- into
 * one glanceable, animated status instead of a row of raw labels.
 */
@Composable
fun ConnectionHero(uiState: HomeUiState, modifier: Modifier = Modifier) {
    val presentation = resolveHeroPresentation(uiState)
    val toneColor = toneColor(presentation.tone)

    val infiniteTransition = rememberInfiniteTransition(label = "hero-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-scale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(toneColor.copy(alpha = 0.28f), toneColor.copy(alpha = 0.06f)),
                ),
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(if (presentation.pulsing) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(toneColor.copy(alpha = if (presentation.pulsing) ringAlpha else 0.16f)),
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(toneColor.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = presentation.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            AnimatedContent(targetState = presentation.title, label = "hero-title") { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = presentation.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun toneColor(tone: HeroTone): Color = when (tone) {
    HeroTone.SUCCESS -> StatusGood
    HeroTone.WARNING -> StatusWarning
    HeroTone.ERROR -> StatusError
    HeroTone.NEUTRAL -> MaterialTheme.colorScheme.primary
}

private fun resolveHeroPresentation(uiState: HomeUiState): HeroPresentation = when {
    uiState.sensorStatus == SensorStatus.UNAVAILABLE -> HeroPresentation(
        icon = Icons.Filled.SensorsOff,
        title = "Sensors Unavailable",
        subtitle = "This device has no gyroscope to track motion",
        tone = HeroTone.ERROR,
        pulsing = false,
    )
    !uiState.isOnline -> HeroPresentation(
        icon = Icons.Filled.WifiOff,
        title = "No Wi-Fi",
        subtitle = "Join the same Wi-Fi network as your PC",
        tone = HeroTone.ERROR,
        pulsing = false,
    )
    uiState.pcAddress == null -> HeroPresentation(
        icon = Icons.Filled.QrCodeScanner,
        title = "Not Paired",
        subtitle = "Scan the QR code shown by pc_receiver to connect",
        tone = HeroTone.NEUTRAL,
        pulsing = false,
    )
    uiState.trackingState == TrackingState.STOPPED -> HeroPresentation(
        icon = Icons.Filled.QrCodeScanner,
        title = "Ready to Connect",
        subtitle = "Paired with ${uiState.pcAddress.display()} — tap Start below",
        tone = HeroTone.NEUTRAL,
        pulsing = false,
    )
    uiState.isReachable -> HeroPresentation(
        icon = Icons.Filled.CheckCircle,
        title = "Connected",
        subtitle = uiState.pcAddress.display(),
        tone = HeroTone.SUCCESS,
        pulsing = true,
    )
    else -> HeroPresentation(
        icon = Icons.Filled.Sync,
        title = "Searching for PC…",
        subtitle = "Make sure pc_receiver is running on ${uiState.pcAddress.display()}",
        tone = HeroTone.WARNING,
        pulsing = true,
    )
}
