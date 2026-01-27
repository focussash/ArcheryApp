package com.example.archeryapp.ui.navigation

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.data.model.TargetDetection
import com.example.archeryapp.ui.components.BottomNavBar
import com.example.archeryapp.ui.screens.arrowedit.ArrowEditScreen
import com.example.archeryapp.ui.screens.camera.CameraScreen
import com.example.archeryapp.ui.screens.history.HistoryScreen
import com.example.archeryapp.ui.screens.home.HomeScreen
import com.example.archeryapp.ui.screens.home.HomeViewModel
import com.example.archeryapp.ui.screens.results.ResultsScreen
import com.example.archeryapp.ui.screens.review.ReviewScreen
import com.example.archeryapp.ui.screens.session.SessionSelectScreen
import com.example.archeryapp.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")
    object SessionSelect : Screen("session_select")
    object Camera : Screen("camera")
    object Review : Screen("review")
    object ArrowEdit : Screen("arrow_edit")
    object Results : Screen("results")
}

// Screens that show the bottom navigation bar
private val bottomNavScreens = setOf(
    Screen.Home.route,
    Screen.History.route,
    Screen.Settings.route
)

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared ViewModel for home screen session state
    val homeViewModel: HomeViewModel = viewModel()
    val activeSessionId by homeViewModel.activeSessionId.collectAsState()

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

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavScreens) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to home to avoid building up a large stack
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
                    },
                    onManageSessions = {
                        navController.navigate(Screen.SessionSelect.route)
                    },
                    viewModel = homeViewModel
                )
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.SessionSelect.route) {
                SessionSelectScreen(
                    onSessionSelected = { sessionId ->
                        homeViewModel.setActiveSession(sessionId)
                        navController.popBackStack()
                    },
                    onNewSession = { sessionId ->
                        homeViewModel.setActiveSession(sessionId)
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
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
                            homeViewModel.refresh()
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        },
                        initialSessionId = activeSessionId
                    )
                }
            }
        }
    }
}
