package com.airmouse3d.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airmouse3d.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Text(
                "AirMouse3D turns your phone into a wireless air mouse. Hold the phone flat, " +
                    "like a physical mouse: tilting it rotates the on-screen cursor, a dedicated " +
                    "pad handles left/right/middle clicks, and a two-finger drag scrolls.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("How it works", style = MaterialTheme.typography.titleMedium)
            Text(
                "There is no Bluetooth and no direct Wi-Fi socket between the phone and the " +
                    "computer. The phone filters its gyroscope into stable dx/dy/scroll/click " +
                    "values and writes them to a Firebase Realtime Database session; a small " +
                    "Rust program on the PC polls that same session and moves the OS cursor.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Session pairing", style = MaterialTheme.typography.titleMedium)
            Text(
                "Every time the app opens it creates a brand new session id, deactivates any " +
                    "other session in the database, and marks itself active -- so the PC " +
                    "receiver always has exactly one unambiguous session to follow.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Built with", style = MaterialTheme.typography.titleMedium)
            Text(
                "Kotlin, Jetpack Compose, Hilt, Coroutines/Flow, and the Firebase Realtime " +
                    "Database SDK, following an MVVM + repository architecture.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
