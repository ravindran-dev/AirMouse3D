package com.airmouse3d.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airmouse3d.model.ClickType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A dedicated touch surface for click input. Deliberately touch-driven rather than derived
 * from accelerometer "tap spikes": a physical tap on glass is unambiguous and has none of the
 * false-positive risk of trying to detect a knuckle-tap from noisy linear-acceleration data,
 * while the gyroscope stream stays completely free to drive pointer movement without a tap's
 * jolt corrupting it.
 */
@Composable
fun ClickPad(onClick: (ClickType) -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var flashType by remember { mutableStateOf<ClickType?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (flashType != null) 0.96f else 1f,
        animationSpec = spring(),
        label = "click-pad-scale",
    )

    fun flash(type: ClickType) {
        onClick(type)
        flashType = type
        scope.launch { delay(180); flashType = null }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ),
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { flash(ClickType.LEFT) },
                    onDoubleTap = { flash(ClickType.RIGHT) },
                    onLongPress = { flash(ClickType.MIDDLE) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = if (flashType != null) Icons.Filled.Adjust else Icons.Filled.TouchApp,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 2.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = flashType?.let { "${it.name} CLICK" } ?: "Click Pad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendChip("Tap · Left")
                LegendChip("Double · Right")
                LegendChip("Hold · Middle")
            }
        }
    }
}

@Composable
private fun LegendChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
    )
}
