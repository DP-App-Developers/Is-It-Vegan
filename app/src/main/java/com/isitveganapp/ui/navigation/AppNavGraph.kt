package com.isitveganapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isitveganapp.domain.model.AnalysisResult
import com.isitveganapp.ui.camera.CameraScreen
import com.isitveganapp.ui.results.ResultsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") {
            CameraScreen(
                onResultReady = { result ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("analysis_result", result)
                    navController.navigate("results")
                }
            )
        }
        composable("results") {
            val result = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<AnalysisResult>("analysis_result")

            result?.let {
                ResultsScreen(
                    result = it,
                    onScanAgain = { navController.popBackStack() }
                )
            }
        }
    }
}
