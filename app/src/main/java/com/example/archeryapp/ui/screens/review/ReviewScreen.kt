package com.example.archeryapp.ui.screens.review

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.ArrowScore
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.data.model.TargetDetection
import com.example.archeryapp.detection.opencv.OpenCvArrowDetector
import com.example.archeryapp.detection.opencv.OpenCvTargetDetector
import com.example.archeryapp.domain.scoring.ScoreCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ReviewScreen"

@Composable
fun ReviewScreen(
    capturedImage: Bitmap,
    onConfirm: (ScoringResult) -> Unit,
    onRetake: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(true) }
    var targetDetection by remember { mutableStateOf<TargetDetection?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val arrowDetections = remember { mutableStateListOf<ArrowDetection>() }
    var selectedArrowIndex by remember { mutableIntStateOf(-1) }
    var imageWidth by remember { mutableFloatStateOf(0f) }
    var imageHeight by remember { mutableFloatStateOf(0f) }
    var displayWidth by remember { mutableFloatStateOf(0f) }
    var displayHeight by remember { mutableFloatStateOf(0f) }

    val scoreCalculator = remember { ScoreCalculator() }

    // Process image on launch
    LaunchedEffect(capturedImage) {
        try {
            val imgWidth = capturedImage.width.toFloat()
            val imgHeight = capturedImage.height.toFloat()
            imageWidth = imgWidth
            imageHeight = imgHeight

            Log.d(TAG, "Processing image: ${imgWidth}x${imgHeight}")

            // Try to detect target with OpenCV on IO thread
            val target = withContext(Dispatchers.IO) {
                try {
                    val targetDetector = OpenCvTargetDetector()
                    targetDetector.detect(capturedImage)
                } catch (e: Exception) {
                    Log.e(TAG, "Target detection failed", e)
                    null
                }
            }

            if (target != null) {
                Log.d(TAG, "Target detected: center=(${target.center.x}, ${target.center.y}), radius=${target.radius}")
                targetDetection = target

                // Try to detect arrows on IO thread
                val arrows = withContext(Dispatchers.IO) {
                    try {
                        val arrowDetector = OpenCvArrowDetector()
                        arrowDetector.detect(capturedImage, target)
                    } catch (e: Exception) {
                        Log.e(TAG, "Arrow detection failed", e)
                        emptyList()
                    }
                }

                arrowDetections.clear()
                arrowDetections.addAll(arrows)
                Log.d(TAG, "Detected ${arrows.size} arrows")
            } else {
                Log.w(TAG, "No target detected - creating default target at image center")
                // Create a default target at center of image for manual scoring
                val defaultTarget = TargetDetection(
                    center = PointF(imgWidth / 2f, imgHeight / 2f),
                    radius = minOf(imgWidth, imgHeight) / 2.5f,
                    confidence = 0f
                )
                targetDetection = defaultTarget
                errorMessage = "Target not auto-detected. Tap to add arrows manually."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image processing failed", e)
            // Create fallback target for manual scoring
            val defaultTarget = TargetDetection(
                center = PointF(imageWidth / 2f, imageHeight / 2f),
                radius = minOf(imageWidth, imageHeight) / 2.5f,
                confidence = 0f
            )
            targetDetection = defaultTarget
            errorMessage = "Auto-detection failed. Tap to add arrows manually."
        } finally {
            isProcessing = false
        }
    }

    // Calculate scores
    val scores = remember(arrowDetections.toList(), targetDetection) {
        targetDetection?.let { target ->
            scoreCalculator.calculate(arrowDetections.toList(), target)
        } ?: emptyList()
    }

    val totalScore = scores.sumOf { it.score }
    val xCount = scores.count { it.isX }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Image with overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        displayWidth = size.width.toFloat()
                        displayHeight = size.height.toFloat()
                    }
            ) {
                Image(
                    bitmap = capturedImage.asImageBitmap(),
                    contentDescription = "Captured target",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (isProcessing) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Analyzing image...")
                    }
                } else {
                    // Detection overlay with manual adjustment
                    DetectionOverlay(
                        target = targetDetection,
                        arrows = arrowDetections.toList(),
                        scores = scores,
                        selectedIndex = selectedArrowIndex,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        displayWidth = displayWidth,
                        displayHeight = displayHeight,
                        onArrowSelected = { index -> selectedArrowIndex = index },
                        onArrowMoved = { index, newPos ->
                            if (index in arrowDetections.indices) {
                                arrowDetections[index] = arrowDetections[index].copy(position = newPos)
                            }
                        },
                        onArrowAdded = { pos ->
                            arrowDetections.add(ArrowDetection(position = pos, confidence = 1.0f))
                        },
                        onArrowDeleted = { index ->
                            if (index in arrowDetections.indices) {
                                arrowDetections.removeAt(index)
                                selectedArrowIndex = -1
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message if any
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Score summary
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Score",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$totalScore",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (xCount > 0) {
                        Text(
                            text = "${xCount}X",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Arrows: ${arrowDetections.size}" + if (scores.isNotEmpty()) " | Scores: ${scores.map { it.score }.joinToString(", ")}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to add arrow, drag to move, long-press to delete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        targetDetection?.let { target ->
                            val result = ScoringResult(
                                originalImage = capturedImage,
                                processedImage = capturedImage,
                                target = target,
                                arrows = arrowDetections.toList(),
                                scores = scores,
                                totalScore = totalScore,
                                xCount = xCount
                            )
                            onConfirm(result)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = targetDetection != null
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
private fun DetectionOverlay(
    target: TargetDetection?,
    arrows: List<ArrowDetection>,
    scores: List<ArrowScore>,
    selectedIndex: Int,
    imageWidth: Float,
    imageHeight: Float,
    displayWidth: Float,
    displayHeight: Float,
    onArrowSelected: (Int) -> Unit,
    onArrowMoved: (Int, PointF) -> Unit,
    onArrowAdded: (PointF) -> Unit,
    onArrowDeleted: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    // Calculate scale and offset for proper coordinate mapping
    val imageAspect = if (imageHeight > 0) imageWidth / imageHeight else 1f
    val displayAspect = if (displayHeight > 0) displayWidth / displayHeight else 1f

    val scale: Float
    val offsetX: Float
    val offsetY: Float

    if (imageWidth > 0 && imageHeight > 0 && displayWidth > 0 && displayHeight > 0) {
        if (imageAspect > displayAspect) {
            scale = displayWidth / imageWidth
            offsetX = 0f
            offsetY = (displayHeight - imageHeight * scale) / 2f
        } else {
            scale = displayHeight / imageHeight
            offsetX = (displayWidth - imageWidth * scale) / 2f
            offsetY = 0f
        }
    } else {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    fun imageToDisplay(point: PointF): Offset {
        return Offset(point.x * scale + offsetX, point.y * scale + offsetY)
    }

    fun displayToImage(offset: Offset): PointF {
        return PointF(
            ((offset.x - offsetX) / scale).coerceIn(0f, imageWidth),
            ((offset.y - offsetY) / scale).coerceIn(0f, imageHeight)
        )
    }

    var draggedIndex by remember { mutableIntStateOf(-1) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(arrows.size) {
                detectTapGestures(
                    onTap = { offset ->
                        val imagePos = displayToImage(offset)
                        // Check if tapped on an arrow
                        val tappedIndex = arrows.indexOfFirst { arrow ->
                            val displayPos = imageToDisplay(arrow.position)
                            (offset - displayPos).getDistance() < 40f
                        }
                        if (tappedIndex >= 0) {
                            onArrowSelected(tappedIndex)
                        } else {
                            // Add new arrow at tap location
                            onArrowAdded(imagePos)
                        }
                    },
                    onLongPress = { offset ->
                        val tappedIndex = arrows.indexOfFirst { arrow ->
                            val displayPos = imageToDisplay(arrow.position)
                            (offset - displayPos).getDistance() < 40f
                        }
                        if (tappedIndex >= 0) {
                            onArrowDeleted(tappedIndex)
                        }
                    }
                )
            }
            .pointerInput(selectedIndex, arrows.size) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val tappedIndex = arrows.indexOfFirst { arrow ->
                            val displayPos = imageToDisplay(arrow.position)
                            (offset - displayPos).getDistance() < 40f
                        }
                        draggedIndex = tappedIndex
                    },
                    onDrag = { change, _ ->
                        if (draggedIndex >= 0) {
                            val newImagePos = displayToImage(change.position)
                            onArrowMoved(draggedIndex, newImagePos)
                        }
                    },
                    onDragEnd = {
                        draggedIndex = -1
                    }
                )
            }
    ) {
        // Draw target center if detected
        target?.let { t ->
            val centerDisplay = imageToDisplay(t.center)

            // Draw target outline
            drawCircle(
                color = Color.Green.copy(alpha = 0.7f),
                radius = t.radius * scale,
                center = centerDisplay,
                style = Stroke(width = 2f)
            )

            // Draw center crosshair
            drawCircle(
                color = Color.Green,
                radius = 8f,
                center = centerDisplay,
                style = Stroke(width = 2f)
            )
            drawLine(
                color = Color.Green,
                start = Offset(centerDisplay.x - 15f, centerDisplay.y),
                end = Offset(centerDisplay.x + 15f, centerDisplay.y),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Green,
                start = Offset(centerDisplay.x, centerDisplay.y - 15f),
                end = Offset(centerDisplay.x, centerDisplay.y + 15f),
                strokeWidth = 2f
            )
        }

        // Draw arrows
        arrows.forEachIndexed { index, arrow ->
            val pos = imageToDisplay(arrow.position)
            val isSelected = index == selectedIndex
            val score = scores.getOrNull(index)

            // Arrow marker circle
            drawCircle(
                color = if (isSelected) Color.Yellow else if (score?.isX == true) Color(0xFFFFD700) else Color.Red,
                radius = if (isSelected) 25f else 20f,
                center = pos
            )
            drawCircle(
                color = Color.White,
                radius = if (isSelected) 25f else 20f,
                center = pos,
                style = Stroke(width = 3f)
            )

            // Score label
            score?.let { s ->
                val label = if (s.isX) "X" else s.score.toString()
                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        pos.x - textLayoutResult.size.width / 2,
                        pos.y - textLayoutResult.size.height / 2
                    )
                )
            }
        }
    }
}
