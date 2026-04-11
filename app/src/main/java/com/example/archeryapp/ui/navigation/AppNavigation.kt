package com.example.archeryapp.ui.navigation

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.PointF
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
import com.example.archeryapp.ui.screens.calendar.CalendarScreen
import com.example.archeryapp.ui.screens.calendar.DayDetailScreen
import com.example.archeryapp.ui.screens.camera.CameraScreen
import com.example.archeryapp.ui.screens.history.HistoryScreen
import com.example.archeryapp.ui.screens.home.HomeScreen
import com.example.archeryapp.ui.screens.home.HomeViewModel
import com.example.archeryapp.ui.screens.results.ResultsScreen
import com.example.archeryapp.ui.screens.review.ReviewScreen
import com.example.archeryapp.ui.screens.scoreinput.NumericScoreInputScreen
import com.example.archeryapp.ui.screens.scoreinput.ScoreInputMethodScreen
import com.example.archeryapp.ui.screens.session.SessionSelectScreen
import com.example.archeryapp.ui.screens.settings.SettingsScreen
import com.example.archeryapp.ui.screens.statistics.StatisticsScreen
import java.time.LocalDate

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")
    object SessionSelect : Screen("session_select")
    object Camera : Screen("camera")
    object Review : Screen("review")
    object ArrowEdit : Screen("arrow_edit")
    object Results : Screen("results")
    object Statistics : Screen("statistics")
    object Calendar : Screen("calendar")
    object DayDetail : Screen("day_detail/{date}") {
        fun createRoute(date: LocalDate) = "day_detail/${date}"
    }
    object ScoreInputMethod : Screen("score_input_method")
    object NumericScoreInput : Screen("numeric_score_input")
}

// Screens that show the bottom navigation bar
private val bottomNavScreens = setOf(
    Screen.Home.route,
    Screen.History.route,
    Screen.Statistics.route,
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
    val homeUiState by homeViewModel.uiState.collectAsState()

    // Shared state for passing data between screens
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scoringResult by remember { mutableStateOf<ScoringResult?>(null) }
    var detectedTarget by remember { mutableStateOf<TargetDetection?>(null) }
    var detectedArrows by remember { mutableStateOf<List<ArrowDetection>>(emptyList()) }

    // Helper function to create default target based on image dimensions
    fun createDefaultTarget(bitmap: Bitmap): TargetDetection {
        val centerX = bitmap.width / 2f
        val centerY = bitmap.height / 2f
        val radius = minOf(bitmap.width, bitmap.height) / 2f * 0.9f
        return TargetDetection(
            center = PointF(centerX, centerY),
            radius = radius,
            confidence = 1.0f
        )
    }

    // Helper to navigate after camera capture - always go to auto-detect review
    fun navigateAfterCapture(bitmap: Bitmap) {
        capturedBitmap = bitmap
        navController.navigate(Screen.Review.route)
    }

    // Helper to set up for manual arrow placement
    fun setupManualArrowEdit(bitmap: Bitmap) {
        capturedBitmap = bitmap
        detectedTarget = createDefaultTarget(bitmap)
        detectedArrows = emptyList()
        navController.navigate(Screen.ArrowEdit.route)
    }

    // Photo picker launcher for calendar/day detail - uses auto-detect
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
                navigateAfterCapture(bitmap)
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
                                saveState = false
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
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
                    onTakePicture = {
                        navController.navigate(Screen.Camera.route)
                    },
                    onInputScore = {
                        navController.navigate(Screen.ScoreInputMethod.route)
                    },
                    onManageSessions = {
                        navController.navigate(Screen.SessionSelect.route)
                    },
                    viewModel = homeViewModel
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route)
                    }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onDaySelected = { date ->
                        navController.navigate(Screen.DayDetail.createRoute(date))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.DayDetail.route) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date")
                val date = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()
                DayDetailScreen(
                    date = date,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddScore = {
                        // Navigate to score input method screen
                        navController.navigate(Screen.ScoreInputMethod.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onDataWiped = {
                        homeViewModel.clearActiveSession()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen()
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
                        navigateAfterCapture(bitmap)
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
                                // Pop back to home whether coming from Review or direct entry
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        onCancel = {
                            // Navigate back to home, clearing any intermediate screens
                            navController.popBackStack(Screen.Home.route, inclusive = false)
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
                        },
                        onEditEnd = {
                            // Clear shared state for fresh entry
                            scoringResult = null
                            capturedBitmap = null
                            // Navigate to ScoreInputMethod, keeping Home in the stack
                            navController.navigate(Screen.ScoreInputMethod.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        initialSessionId = activeSessionId
                    )
                }
            }

            composable(Screen.ScoreInputMethod.route) {
                ScoreInputMethodScreen(
                    onPlaceOnTarget = {
                        // Create a dummy bitmap for manual placement
                        val dummyBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
                        setupManualArrowEdit(dummyBitmap)
                    },
                    onEnterNumbers = {
                        navController.navigate(Screen.NumericScoreInput.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.NumericScoreInput.route) {
                NumericScoreInputScreen(
                    onConfirm = { result ->
                        scoringResult = result
                        navController.navigate(Screen.Results.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onCancel = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }
        }
    }
}
