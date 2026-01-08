package com.example.archeryapp.ui.screens.results

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.archeryapp.data.model.ArrowScore
import com.example.archeryapp.data.model.ScoringResult
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ResultsScreen(
    scoringResult: ScoringResult,
    onNewScan: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Score Results",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Synthetic USA target with arrow positions
            SyntheticTargetView(
                scoringResult = scoringResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total score card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${scoringResult.totalScore}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (scoringResult.xCount > 0) {
                        Text(
                            text = "${scoringResult.xCount}X",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Individual arrow scores
            Text(
                text = "Arrow Scores",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (scoringResult.scores.isEmpty()) {
                Text(
                    text = "No arrows detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(scoringResult.scores) { index, arrowScore ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (arrowScore.isX)
                                    MaterialTheme.colorScheme.tertiaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (arrowScore.isX) "X" else "${arrowScore.score}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        label = "Arrows",
                        value = "${scoringResult.arrows.size}"
                    )
                    StatItem(
                        label = "Average",
                        value = if (scoringResult.scores.isNotEmpty())
                            "%.1f".format(scoringResult.scores.map { it.score }.average())
                        else "N/A"
                    )
                    StatItem(
                        label = "Max",
                        value = if (scoringResult.scores.isNotEmpty())
                            "${scoringResult.scores.maxOf { it.score }}"
                        else "N/A"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // New scan button
            Button(
                onClick = onNewScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "New Scan",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SyntheticTargetView(
    scoringResult: ScoringResult,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // USA Archery target colors
    val goldColor = Color(0xFFFFD700)      // X, 10, 9
    val redColor = Color(0xFFE31837)       // 8, 7
    val blueColor = Color(0xFF00A2E8)      // 6, 5
    val blackColor = Color(0xFF000000)     // 4, 3
    val whiteColor = Color(0xFFFFFFFF)     // 2, 1

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f * 0.95f
            val ringWidth = maxRadius / 10f  // 10 scoring rings

            // Draw rings from outside to inside
            // White (1, 2)
            drawCircle(color = whiteColor, radius = maxRadius, center = center)
            drawCircle(color = blackColor, radius = maxRadius, center = center, style = Stroke(width = 2f))

            // Black (3, 4)
            drawCircle(color = blackColor, radius = maxRadius - ringWidth * 2, center = center)

            // Blue (5, 6)
            drawCircle(color = blueColor, radius = maxRadius - ringWidth * 4, center = center)

            // Red (7, 8)
            drawCircle(color = redColor, radius = maxRadius - ringWidth * 6, center = center)

            // Gold (9, 10, X)
            drawCircle(color = goldColor, radius = maxRadius - ringWidth * 8, center = center)

            // Draw ring separators
            for (i in 1..10) {
                val ringRadius = maxRadius - ringWidth * (i - 1)
                val strokeColor = when {
                    i <= 2 -> blackColor
                    i <= 4 -> Color.White
                    i <= 8 -> blackColor
                    else -> blackColor
                }
                drawCircle(
                    color = strokeColor,
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Draw X ring (innermost)
            drawCircle(
                color = blackColor,
                radius = ringWidth * 0.5f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Draw arrows based on their relative position to the detected target
            val targetRadius = scoringResult.target.radius
            val targetCenterX = scoringResult.target.center.x
            val targetCenterY = scoringResult.target.center.y

            scoringResult.arrows.forEachIndexed { index, arrow ->
                // Calculate relative position from target center (normalized to -1 to 1)
                val relX = (arrow.position.x - targetCenterX) / targetRadius
                val relY = (arrow.position.y - targetCenterY) / targetRadius

                // Map to synthetic target coordinates
                val arrowX = center.x + relX * maxRadius
                val arrowY = center.y + relY * maxRadius

                val arrowPos = Offset(arrowX, arrowY)
                val score = scoringResult.scores.getOrNull(index)

                // Draw arrow marker - larger size
                drawCircle(
                    color = Color.Red,
                    radius = 20f,
                    center = arrowPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 20f,
                    center = arrowPos,
                    style = Stroke(width = 3f)
                )

                // Draw score label - larger font
                score?.let { s ->
                    val label = if (s.isX) "X" else s.score.toString()
                    val textLayoutResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            fontSize = 16.sp,
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
