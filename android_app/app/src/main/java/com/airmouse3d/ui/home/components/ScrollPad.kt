package com.airmouse3d.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Two-finger vertical drag surface for scrolling, kept separate from [ClickPad] so a scroll
 * gesture can never be misread as a tap. The scroll delta is reported per-frame via
 * [onScroll]; upstream this feeds straight into [com.airmouse3d.sensor.GestureInputBus].
 */
@Composable
fun ScrollPad(onScroll: (Double) -> Unit, modifier: Modifier = Modifier) {
    var isActive by remember { mutableStateOf(false) }

    val highlight by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiaryContainer,
        label = "scroll-pad-highlight",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(highlight, MaterialTheme.colorScheme.tertiaryContainer)),
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    var previousAverageY: Float? = null
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val activeChanges = event.changes.filter { it.pressed }

                        if (activeChanges.size >= 2) {
                            isActive = true
                            val averageY = activeChanges.map { it.position.y }.average().toFloat()
                            val previous = previousAverageY
                            if (previous != null) {
                                onScroll((averageY - previous).toDouble())
                            }
                            previousAverageY = averageY
                            activeChanges.forEach { it.consume() }
                        } else {
                            previousAverageY = null
                        }

                        if (event.changes.all { !it.pressed }) {
                            isActive = false
                            break
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = Icons.Filled.SwapVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text = "Two-Finger Scroll",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}
