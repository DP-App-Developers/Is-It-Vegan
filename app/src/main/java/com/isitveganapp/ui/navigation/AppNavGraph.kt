package com.isitveganapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.isitveganapp.data.model.VeganStatus
import com.isitveganapp.domain.model.AnalysisResult
import com.isitveganapp.ui.camera.CameraScreen
import com.isitveganapp.ui.results.ResultsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val analytics = Firebase.analytics

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
                    onScanAgain = { navController.popBackStack() },
                    onFeedback = { isCorrect ->
                        val verdict = when (it.overallStatus) {
                            VeganStatus.VEGAN -> "vegan"
                            VeganStatus.NOT_VEGAN -> "not_vegan"
                            VeganStatus.UNCERTAIN -> "uncertain"
                        }
                        val flagged = it.flaggedIngredients
                            .joinToString(", ") { f -> f.ingredient.displayName }
                            .take(100)
                        val scanned = it.parsedTokens
                            .joinToString(", ")
                            .take(100)
                        analytics.logEvent("scan_feedback", bundleOf(
                            "verdict" to verdict,
                            "feedback" to if (isCorrect) "thumbs_up" else "thumbs_down",
                            "flagged_ingredients" to flagged,
                            "scanned_tokens" to scanned
                        ))
                    }
                )
            }
        }
    }
}
