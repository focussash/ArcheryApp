package com.example.archeryapp.ui.screens.review

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import kotlin.math.min

private const val TAG = "ReviewScreen"

@Composable
fun ReviewScreen(
    capturedImage: Bitmap,
    onConfirm: (ScoringResult) -> Unit,
    onEditArrows: (TargetDetection, List<ArrowDetection>) -> Unit,
    onRetake: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(true) }
    var targetDetection by remember { mutableStateOf<TargetDetection?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val arrowDetections = remember { mutableStateListOf<ArrowDetection>() }
    var imageWidth by remember { mutableFloatStateOf(0f) }
    var imageHeight by remember { mutableFloatStateOf(0f) }

    val scoreCalculator = remember { ScoreCalculator() }

    // Process image on launch
    LaunchedEffect(capturedImage) {
        try {
            val imgWidth = capturedImage.width.toFloat()
            val imgHeight = capturedImage.height.toFloat()
            imageWidth = imgWidth
            imageHeight = imgHeight

            Log.d(TAG, "Processing image: ${imgWidth}x${imgHeight}")

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
                val defaultTarget = TargetDetection(
                    center = PointF(imgWidth / 2f, imgHeight / 2f),
                    radius = minOf(imgWidth, imgHeight) / 2.5f,
                    confidence = 0f
                )
                targetDetection = defaultTarget
                errorMessage = "Target not auto-detected. Use Edit Arrows to add manually."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image processing failed", e)
            val defaultTarget = TargetDetection(
                center = PointF(imageWidth / 2f, imageHeight / 2f),
                radius = minOf(imageWidth, imageHeight) / 2.5f,
                confidence = 0f
            )
            targetDetection = defaultTarget
            errorMessage = "Auto-detection failed. Use Edit Arrows to add manually."
        } finally {
            isProcessing = false
        }
    }

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
        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing image...")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Side-by-side views
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left: Original image with overlays
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        ImageWithOverlay(
                            bitmap = capturedImage,
                            target = targetDetection,
                            arrows = arrowDetections.toList(),
                            scores = scores,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Right: Synthetic target
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        SyntheticTargetPreview(
                            target = targetDetection,
                            arrows = arrowDetections.toList(),
                            scores = scores,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error message
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Score summary
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Score",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Arrows: ${arrowDetections.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalScore",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (xCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${xCount}X",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retake")
                    }

                    OutlinedButton(
                        onClick = {
                            targetDetection?.let { target ->
                                onEditArrows(target, arrowDetections.toList())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = targetDetection != null
                    ) {
                        Text("Edit Arrows")
                    }

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
}

@Composable
private fun ImageWithOverlay(
    bitmap: Bitmap,
    target: TargetDetection?,
    arrows: List<ArrowDetection>,
    scores: List<ArrowScore>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var displayWidth by remember { mutableFloatStateOf(0f) }
    var displayHeight by remember { mutableFloatStateOf(0f) }

    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()

    Box(
        modifier = modifier.onSizeChanged { size ->
            displayWidth = size.width.toFloat()
            displayHeight = size.height.toFloat()
        }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured target",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (displayWidth > 0 && displayHeight > 0) {
            val imageAspect = imageWidth / imageHeight
            val displayAspect = displayWidth / displayHeight

            val scale: Float
            val offsetX: Float
            val offsetY: Float

            if (imageAspect > displayAspect) {
                scale = displayWidth / imageWidth
                offsetX = 0f
                offsetY = (displayHeight - imageHeight * scale) / 2f
            } else {
                scale = displayHeight / imageHeight
                offsetX = (displayWidth - imageWidth * scale) / 2f
                offsetY = 0f
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw target outline
                target?.let { t ->
                    val centerX = t.center.x * scale + offsetX
                    val centerY = t.center.y * scale + offsetY
                    val centerDisplay = Offset(centerX, centerY)

                    drawCircle(
                        color = Color.Green.copy(alpha = 0.7f),
                        radius = t.radius * scale,
                        center = centerDisplay,
                        style = Stroke(width = 2f)
                    )

                    drawCircle(
                        color = Color.Green,
                        radius = 6f,
                        center = centerDisplay,
                        style = Stroke(width = 2f)
                    )
                }

                // Draw arrows
                arrows.forEachIndexed { index, arrow ->
                    val posX = arrow.position.x * scale + offsetX
                    val posY = arrow.position.y * scale + offsetY
                    val pos = Offset(posX, posY)
                    val score = scores.getOrNull(index)

                    drawCircle(
                        color = if (score?.isX == true) Color(0xFFFFD700) else Color.Red,
                        radius = 16f,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = pos,
                        style = Stroke(width = 2f)
                    )

                    score?.let { s ->
                        val label = if (s.isX) "X" else s.score.toString()
                        val textLayoutResult = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                fontSize = 12.sp,
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
    }
}

@Composable
private fun SyntheticTargetPreview(
    target: TargetDetection?,
    arrows: List<ArrowDetection>,
    scores: List<ArrowScore>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val goldColor = Color(0xFFFFD700)
    val redColor = Color(0xFFE31837)
    val blueColor = Color(0xFF00A2E8)
    val blackColor = Color(0xFF000000)
    val whiteColor = Color(0xFFFFFFFF)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f * 0.95f
            val ringWidth = maxRadius / 10f

            // Draw rings
            drawCircle(color = whiteColor, radius = maxRadius, center = center)
            drawCircle(color = blackColor, radius = maxRadius, center = center, style = Stroke(width = 2f))
            drawCircle(color = blackColor, radius = maxRadius - ringWidth * 2, center = center)
            drawCircle(color = blueColor, radius = maxRadius - ringWidth * 4, center = center)
            drawCircle(color = redColor, radius = maxRadius - ringWidth * 6, center = center)
            drawCircle(color = goldColor, radius = maxRadius - ringWidth * 8, center = center)

            // Ring separators
            for (i in 1..10) {
                val ringRadius = maxRadius - ringWidth * (i - 1)
                val strokeColor = when {
                    i <= 2 -> blackColor
                    i <= 4 -> Color.White
                    else -> blackColor
                }
                drawCircle(color = strokeColor, radius = ringRadius, center = center, style = Stroke(width = 1f))
            }

            drawCircle(color = blackColor, radius = ringWidth * 0.5f, center = center, style = Stroke(width = 1f))

            // Draw arrows
            target?.let { t ->
                arrows.forEachIndexed { index, arrow ->
                    val relX = (arrow.position.x - t.center.x) / t.radius
                    val relY = (arrow.position.y - t.center.y) / t.radius
                    val arrowX = center.x + relX * maxRadius
                    val arrowY = center.y + relY * maxRadius
                    val arrowPos = Offset(arrowX, arrowY)
                    val score = scores.getOrNull(index)

                    drawCircle(color = Color.Red, radius = 14f, center = arrowPos)
                    drawCircle(color = Color.White, radius = 14f, center = arrowPos, style = Stroke(width = 2f))

                    score?.let { s ->
                        val label = if (s.isX) "X" else s.score.toString()
                        val textLayoutResult = textMeasurer.measure(
                            text = label,
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                arrowPos.x - textLayoutResult.size.width / 2,
                                arrowPos.y - textLayoutResult.size.height / 2
                            )
                        )
                    }
                }
            }
        }
    }
}
