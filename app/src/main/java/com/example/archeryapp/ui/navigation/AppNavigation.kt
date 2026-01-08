package com.example.archeryapp.ui.navigation

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.ui.screens.camera.CameraScreen
import com.example.archeryapp.ui.screens.home.HomeScreen
import com.example.archeryapp.ui.screens.results.ResultsScreen
import com.example.archeryapp.ui.screens.review.ReviewScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Review : Screen("review")
    object Results : Screen("results")
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Shared state for passing data between screens
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scoringResult by remember { mutableStateOf<ScoringResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanTarget = {
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { bitmap ->
                    capturedBitmap = bitmap
                    navController.navigate(Screen.Review.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Review.route) {
            capturedBitmap?.let { bitmap ->
                ReviewScreen(
                    capturedImage = bitmap,
                    onConfirm = { result ->
                        scoringResult = result
                        navController.navigate(Screen.Results.route)
                    },
                    onRetake = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Results.route) {
            scoringResult?.let { result ->
                ResultsScreen(
                    scoringResult = result,
                    onNewScan = {
                        capturedBitmap = null
                        scoringResult = null
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }
        }
    }
}
