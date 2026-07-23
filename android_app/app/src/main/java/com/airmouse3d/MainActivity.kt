package com.airmouse3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.airmouse3d.navigation.AirMouseNavGraph
import com.airmouse3d.ui.theme.AirMouse3DTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. All screens (Splash, Home, About) are Compose destinations reached
 * through [AirMouseNavGraph]; this class only sets up the theme and navigation root.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AirMouse3DTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AirMouseNavGraph()
                }
            }
        }
    }
}
