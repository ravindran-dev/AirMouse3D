package com.airmouse3d.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airmouse3d.R
import com.airmouse3d.model.ClickType
import com.airmouse3d.ui.home.components.ClickPad
import com.airmouse3d.ui.home.components.ConnectionHero
import com.airmouse3d.ui.home.components.ControlButtons
import com.airmouse3d.ui.home.components.ScrollPad
import com.airmouse3d.ui.home.components.SensitivityControl
import com.airmouse3d.viewmodel.HomeUiState
import com.airmouse3d.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAboutClick: () -> Unit,
    onScanClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Start proceeds regardless; without it the notification is just hidden. */ },
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.about))
                    }
                },
            )
        },
    ) { padding ->
        HomeContent(
            uiState = uiState,
            onStartClick = viewModel::onStartClicked,
            onStopClick = viewModel::onStopClicked,
            onScanClick = onScanClick,
            onForgetPcClick = viewModel::onForgetPcClicked,
            onSensitivityChange = viewModel::onSensitivityChanged,
            onClickGesture = viewModel::onClickGesture,
            onScrollDelta = viewModel::onScrollDelta,
            contentPadding = padding,
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onScanClick: () -> Unit,
    onForgetPcClick: () -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onClickGesture: (ClickType) -> Unit,
    onScrollDelta: (Double) -> Unit,
    contentPadding: PaddingValues,
) {
    val sections: List<@Composable () -> Unit> = listOf(
        { ConnectionHero(uiState = uiState) },
        {
            if (uiState.isPaired) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ControlButtons(isRunning = uiState.isRunning, onStartClick = onStartClick, onStopClick = onStopClick)
                    TextButton(onClick = onForgetPcClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Not this PC? Re-scan")
                    }
                }
            } else {
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Text(text = "  Scan QR Code", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        { ClickPad(onClick = onClickGesture) },
        { ScrollPad(onScroll = onScrollDelta) },
        {
            SensitivityControl(
                sensitivity = uiState.settings.sensitivity,
                onSensitivityChange = onSensitivityChange,
            )
        },
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
    ) {
        items(sections) { section -> section() }
    }
}
