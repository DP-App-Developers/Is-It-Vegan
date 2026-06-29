package com.isitveganapp

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.isitveganapp.ui.navigation.AppNavGraph
import com.isitveganapp.ui.theme.IsItVeganTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock phones to portrait. smallestScreenWidthDp is orientation-independent
        // (always the short axis), so sw600dp reliably identifies tablets/foldables.
        val isWideScreen = resources.configuration.smallestScreenWidthDp >= 600
        if (!isWideScreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        enableEdgeToEdge()
        setContent {
            IsItVeganTheme {
                AppNavGraph()
            }
        }
    }
}
