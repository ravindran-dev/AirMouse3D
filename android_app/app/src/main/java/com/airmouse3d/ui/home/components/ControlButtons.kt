package com.airmouse3d.ui.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airmouse3d.R
import com.airmouse3d.ui.theme.StatusError

@Composable
fun ControlButtons(
    isRunning: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isRunning) {
        Button(
            onClick = onStopClick,
            modifier = modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusError),
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Text(
                text = "  " + stringResource(R.string.stop_air_mouse),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        Button(onClick = onStartClick, modifier = modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(
                text = "  " + stringResource(R.string.start_air_mouse),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
