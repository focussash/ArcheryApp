package com.example.archeryapp.ui.screens.scoreinput

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.ArrowScore
import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.data.model.TargetDetection

data class NumericArrowEntry(
    val score: Int,
    val isX: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NumericScoreInputScreen(
    onConfirm: (ScoringResult) -> Unit,
    onCancel: () -> Unit
) {
    val arrows = remember { mutableStateListOf<NumericArrowEntry>() }

    val totalScore = arrows.sumOf { it.score }
    val xCount = arrows.count { it.isX }

    // Score buttons: X, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, M (0)
    val scoreOptions = listOf(
        "X" to Pair(10, true),
        "10" to Pair(10, false),
        "9" to Pair(9, false),
        "8" to Pair(8, false),
        "7" to Pair(7, false),
        "6" to Pair(6, false),
        "5" to Pair(5, false),
        "4" to Pair(4, false),
        "3" to Pair(3, false),
        "2" to Pair(2, false),
        "1" to Pair(1, false),
        "M" to Pair(0, false)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Enter Scores") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Current arrows display
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Arrows (${arrows.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$totalScore",
                                    style = MaterialTheme.typography.headlineMedium,
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

                        Spacer(modifier = Modifier.height(12.dp))

                        if (arrows.isEmpty()) {
                            Text(
                                text = "Tap buttons below to add arrows",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(arrows) { index, arrow ->
                                    ArrowChip(
                                        index = index + 1,
                                        arrow = arrow,
                                        onRemove = { arrows.removeAt(index) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Score buttons grid
                Text(
                    text = "Tap to add score",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    scoreOptions.forEach { (label, scoreData) ->
                        ScoreButton(
                            label = label,
                            score = scoreData.first,
                            isX = scoreData.second,
                            onClick = {
                                arrows.add(NumericArrowEntry(scoreData.first, scoreData.second))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { arrows.clear() },
                        modifier = Modifier.weight(1f),
                        enabled = arrows.isNotEmpty()
                    ) {
                        Text("Clear All")
                    }

                    Button(
                        onClick = {
                            // Create a dummy target and scoring result
                            val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                            val dummyTarget = TargetDetection(
                                center = PointF(50f, 50f),
                                radius = 45f,
                                confidence = 1.0f
                            )

                            // Convert to arrow detections and scores
                            val arrowDetections = arrows.mapIndexed { index, entry ->
                                // Place arrows in a line for visual representation
                                val x = 50f + (index - arrows.size / 2f) * 5f
                                val y = 50f
                                ArrowDetection(
                                    position = PointF(x, y),
                                    confidence = 1.0f
                                )
                            }

                            val arrowScores = arrowDetections.mapIndexed { index, detection ->
                                val entry = arrows[index]
                                ArrowScore(
                                    arrow = detection,
                                    score = entry.score,
                                    isX = entry.isX
                                )
                            }

                            val result = ScoringResult(
                                originalImage = dummyBitmap,
                                processedImage = dummyBitmap,
                                target = dummyTarget,
                                arrows = arrowDetections,
                                scores = arrowScores,
                                totalScore = totalScore,
                                xCount = xCount
                            )
                            onConfirm(result)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = arrows.isNotEmpty()
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun ArrowChip(
    index: Int,
    arrow: NumericArrowEntry,
    onRemove: () -> Unit
) {
    val displayText = if (arrow.isX) "X" else if (arrow.score == 0) "M" else arrow.score.toString()
    val backgroundColor = when {
        arrow.isX -> MaterialTheme.colorScheme.primary
        arrow.score >= 9 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        arrow.score >= 7 -> MaterialTheme.colorScheme.secondary
        arrow.score >= 5 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onRemove)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$index: $displayText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (arrow.score >= 5 || arrow.isX)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = if (arrow.score >= 5 || arrow.isX)
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ScoreButton(
    label: String,
    score: Int,
    isX: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isX -> MaterialTheme.colorScheme.primary
        score == 10 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        score >= 9 -> Color(0xFFFFD700) // Gold
        score >= 7 -> Color(0xFFE31837) // Red
        score >= 5 -> Color(0xFF00A2E8) // Blue
        score >= 3 -> Color.Black
        score >= 1 -> Color.White
        else -> MaterialTheme.colorScheme.surfaceVariant // Miss
    }

    val textColor = when {
        isX || score == 10 -> Color.White
        score >= 9 -> Color.Black
        score >= 7 -> Color.White
        score >= 5 -> Color.White
        score >= 3 -> Color.White
        score >= 1 -> Color.Black
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        )
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
