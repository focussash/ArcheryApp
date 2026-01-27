package com.example.archeryapp.ui.screens.arrowedit

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.data.model.TargetDetection
import com.example.archeryapp.domain.scoring.ScoreCalculator
import kotlin.math.min

@Composable
fun ArrowEditScreen(
    target: TargetDetection,
    initialArrows: List<ArrowDetection>,
    capturedImage: Bitmap,
    onConfirm: (ScoringResult) -> Unit,
    onCancel: () -> Unit
) {
    val arrows = remember { mutableStateListOf<ArrowDetection>().apply { addAll(initialArrows) } }
    val scoreCalculator = remember { ScoreCalculator() }

    val scores = remember(arrows.toList()) {
        scoreCalculator.calculate(arrows.toList(), target)
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
            Text(
                text = "Edit Arrow Positions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Tap to add arrow, drag to move, long-press to delete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Large synthetic target for editing
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                EditableSyntheticTarget(
                    target = target,
                    arrows = arrows,
                    scores = scores,
                    onArrowAdded = { relativePos ->
                        // Convert relative position (0-1 range from center) to image coordinates
                        val imageX = target.center.x + relativePos.x * target.radius
                        val imageY = target.center.y + relativePos.y * target.radius
                        arrows.add(ArrowDetection(position = PointF(imageX, imageY), confidence = 1.0f))
                    },
                    onArrowMoved = { index, relativePos ->
                        if (index in arrows.indices) {
                            val imageX = target.center.x + relativePos.x * target.radius
                            val imageY = target.center.y + relativePos.y * target.radius
                            arrows[index] = arrows[index].copy(position = PointF(imageX, imageY))
                        }
                    },
                    onArrowDeleted = { index ->
                        if (index in arrows.indices) {
                            arrows.removeAt(index)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            text = "Arrows: ${arrows.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (scores.isNotEmpty()) {
                            Text(
                                text = "Scores: ${scores.map { if (it.isX) "X" else it.score.toString() }.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$totalScore",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (xCount > 0) {
                            Text(
                                text = " ${xCount}X",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val result = ScoringResult(
                            originalImage = capturedImage,
                            processedImage = capturedImage,
                            target = target,
                            arrows = arrows.toList(),
                            scores = scores,
                            totalScore = totalScore,
                            xCount = xCount
                        )
                        onConfirm(result)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
private fun EditableSyntheticTarget(
    target: TargetDetection,
    arrows: List<ArrowDetection>,
    scores: List<com.example.archeryapp.data.model.ArrowScore>,
    onArrowAdded: (PointF) -> Unit,
    onArrowMoved: (Int, PointF) -> Unit,
    onArrowDeleted: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var draggedIndex by remember { mutableIntStateOf(-1) }

    val goldColor = Color(0xFFFFD700)
    val redColor = Color(0xFFE31837)
    val blueColor = Color(0xFF00A2E8)
    val blackColor = Color(0xFF000000)
    val whiteColor = Color(0xFFFFFFFF)

    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(arrows.size) {
                    detectTapGestures(
                        onTap = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = min(size.width, size.height) / 2f * 0.95f

                            // Check if tapped on existing arrow
                            val tappedIndex = arrows.indexOfFirst { arrow ->
                                val relX = (arrow.position.x - target.center.x) / target.radius
                                val relY = (arrow.position.y - target.center.y) / target.radius
                                val arrowX = center.x + relX * maxRadius
                                val arrowY = center.y + relY * maxRadius
                                val distance = kotlin.math.sqrt(
                                    (offset.x - arrowX) * (offset.x - arrowX) +
                                    (offset.y - arrowY) * (offset.y - arrowY)
                                )
                                distance < 50f
                            }

                            if (tappedIndex < 0) {
                                // Add new arrow at tap location
                                val relX = (offset.x - center.x) / maxRadius
                                val relY = (offset.y - center.y) / maxRadius
                                onArrowAdded(PointF(relX, relY))
                            }
                        },
                        onLongPress = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = min(size.width, size.height) / 2f * 0.95f

                            val tappedIndex = arrows.indexOfFirst { arrow ->
                                val relX = (arrow.position.x - target.center.x) / target.radius
                                val relY = (arrow.position.y - target.center.y) / target.radius
                                val arrowX = center.x + relX * maxRadius
                                val arrowY = center.y + relY * maxRadius
                                val distance = kotlin.math.sqrt(
                                    (offset.x - arrowX) * (offset.x - arrowX) +
                                    (offset.y - arrowY) * (offset.y - arrowY)
                                )
                                distance < 50f
                            }

                            if (tappedIndex >= 0) {
                                onArrowDeleted(tappedIndex)
                            }
                        }
                    )
                }
                .pointerInput(arrows.size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = min(size.width, size.height) / 2f * 0.95f

                            draggedIndex = arrows.indexOfFirst { arrow ->
                                val relX = (arrow.position.x - target.center.x) / target.radius
                                val relY = (arrow.position.y - target.center.y) / target.radius
                                val arrowX = center.x + relX * maxRadius
                                val arrowY = center.y + relY * maxRadius
                                val distance = kotlin.math.sqrt(
                                    (offset.x - arrowX) * (offset.x - arrowX) +
                                    (offset.y - arrowY) * (offset.y - arrowY)
                                )
                                distance < 50f
                            }
                        },
                        onDrag = { change, _ ->
                            if (draggedIndex >= 0) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val maxRadius = min(size.width, size.height) / 2f * 0.95f
                                val relX = (change.position.x - center.x) / maxRadius
                                val relY = (change.position.y - center.y) / maxRadius
                                onArrowMoved(draggedIndex, PointF(relX, relY))
                            }
                        },
                        onDragEnd = {
                            draggedIndex = -1
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f * 0.95f
            val ringWidth = maxRadius / 10f

            // Draw rings
            drawCircle(color = whiteColor, radius = maxRadius, center = center)
            drawCircle(color = blackColor, radius = maxRadius, center = center, style = Stroke(width = 3f))
            drawCircle(color = blackColor, radius = maxRadius - ringWidth * 2, center = center)
            drawCircle(color = blueColor, radius = maxRadius - ringWidth * 4, center = center)
            drawCircle(color = redColor, radius = maxRadius - ringWidth * 6, center = center)
            drawCircle(color = goldColor, radius = maxRadius - ringWidth * 8, center = center)

            // Ring separators with numbers
            for (i in 1..10) {
                val ringRadius = maxRadius - ringWidth * (i - 1)
                val strokeColor = when {
                    i <= 2 -> blackColor
                    i <= 4 -> Color.White
                    else -> blackColor
                }
                drawCircle(color = strokeColor, radius = ringRadius, center = center, style = Stroke(width = 2f))
            }

            // X ring
            drawCircle(color = blackColor, radius = ringWidth * 0.5f, center = center, style = Stroke(width = 2f))

            // Draw arrows with larger markers for editing
            arrows.forEachIndexed { index, arrow ->
                val relX = (arrow.position.x - target.center.x) / target.radius
                val relY = (arrow.position.y - target.center.y) / target.radius
                val arrowX = center.x + relX * maxRadius
                val arrowY = center.y + relY * maxRadius
                val arrowPos = Offset(arrowX, arrowY)
                val score = scores.getOrNull(index)
                val isBeingDragged = index == draggedIndex

                // Larger markers for easier touch
                val markerRadius = if (isBeingDragged) 32f else 26f

                drawCircle(
                    color = if (isBeingDragged) Color.Yellow else Color.Red,
                    radius = markerRadius,
                    center = arrowPos
                )
                drawCircle(
                    color = Color.White,
                    radius = markerRadius,
                    center = arrowPos,
                    style = Stroke(width = 4f)
                )

                // Score label - larger font
                score?.let { s ->
                    val label = if (s.isX) "X" else s.score.toString()
                    val textLayoutResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
