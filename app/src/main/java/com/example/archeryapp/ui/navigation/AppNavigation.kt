package com.example.archeryapp.ui.navigation

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.data.model.TargetDetection
import com.example.archeryapp.ui.screens.arrowedit.ArrowEditScreen
import com.example.archeryapp.ui.screens.camera.CameraScreen
import com.example.archeryapp.ui.screens.home.HomeScreen
import com.example.archeryapp.ui.screens.results.ResultsScreen
import com.example.archeryapp.ui.screens.review.ReviewScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Review : Screen("review")
    object ArrowEdit : Screen("arrow_edit")
    object Results : Screen("results")
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Shared state for passing data between screens
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scoringResult by remember { mutableStateOf<ScoringResult?>(null) }
    var detectedTarget by remember { mutableStateOf<TargetDetection?>(null) }
    var detectedArrows by remember { mutableStateOf<List<ArrowDetection>>(emptyList()) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, it)
                    ) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                capturedBitmap = bitmap
                navController.navigate(Screen.Review.route)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanTarget = {
                    navController.navigate(Screen.Camera.route)
                },
                onUploadImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
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
                    onEditArrows = { target, arrows ->
                        detectedTarget = target
                        detectedArrows = arrows
                        navController.navigate(Screen.ArrowEdit.route)
                    },
                    onRetake = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.ArrowEdit.route) {
            val target = detectedTarget
            val bitmap = capturedBitmap
            if (target != null && bitmap != null) {
                ArrowEditScreen(
                    target = target,
                    initialArrows = detectedArrows,
                    onConfirm = { result ->
                        scoringResult = result
                        navController.navigate(Screen.Results.route) {
                            popUpTo(Screen.Review.route) { inclusive = true }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    },
                    capturedImage = bitmap
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
