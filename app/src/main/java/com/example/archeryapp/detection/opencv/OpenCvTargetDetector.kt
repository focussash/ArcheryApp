package com.example.archeryapp.detection.opencv

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import com.example.archeryapp.data.model.TargetDetection
import com.example.archeryapp.domain.detection.TargetDetector
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class OpenCvTargetDetector : TargetDetector {

    companion object {
        private const val TAG = "OpenCvTargetDetector"
        private var isOpenCvInitialized = false

        // HSV ranges for target colors (USA archery target)
        // Expanded ranges and lowered saturation minimums for lighting tolerance

        // Gold/Yellow (10, 9 rings) - expanded hue range, lower saturation minimum
        private val GOLD_LOW = Scalar(12.0, 60.0, 80.0)
        private val GOLD_HIGH = Scalar(40.0, 255.0, 255.0)

        // Red (8, 7 rings) - lower saturation for washed-out reds
        private val RED_LOW_1 = Scalar(0.0, 50.0, 60.0)
        private val RED_HIGH_1 = Scalar(12.0, 255.0, 255.0)
        private val RED_LOW_2 = Scalar(155.0, 50.0, 60.0)
        private val RED_HIGH_2 = Scalar(180.0, 255.0, 255.0)

        // Blue (6, 5 rings) - expanded range for cyan-ish blues
        private val BLUE_LOW = Scalar(90.0, 50.0, 40.0)
        private val BLUE_HIGH = Scalar(135.0, 255.0, 255.0)

        init {
            try {
                isOpenCvInitialized = OpenCVLoader.initLocal()
                Log.d(TAG, "OpenCV initialization: $isOpenCvInitialized")
            } catch (e: Exception) {
                Log.e(TAG, "OpenCV initialization failed", e)
                isOpenCvInitialized = false
            }
        }
    }

    override fun detect(bitmap: Bitmap): TargetDetection? {
        if (!isOpenCvInitialized) {
            Log.e(TAG, "OpenCV not initialized, cannot detect target")
            return null
        }

        return try {
            detectTarget(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting target", e)
            null
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "OpenCV native library not found", e)
            null
        }
    }

    private fun detectTarget(bitmap: Bitmap): TargetDetection? {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)

        // Normalize brightness/contrast using CLAHE (Contrast Limited Adaptive Histogram Equalization)
        val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
        clahe.apply(gray, gray)

        // Apply Gaussian blur
        Imgproc.GaussianBlur(gray, gray, Size(9.0, 9.0), 2.0)

        // Try multiple parameter sweeps to catch targets of different sizes/conditions
        val allCircles = mutableListOf<Triple<Point, Double, Mat>>()
        val imageSize = minOf(gray.rows(), gray.cols())

        // Parameter sets: (param1, param2, minRadiusFactor, maxRadiusFactor)
        val paramSets = listOf(
            // Original parameters
            Triple(100.0, 50.0, Pair(1.0/8, 1.0/2)),
            // More lenient for faint edges
            Triple(80.0, 40.0, Pair(1.0/10, 2.0/3)),
            // Stricter for high-contrast images
            Triple(120.0, 60.0, Pair(1.0/6, 1.0/2)),
            // Small targets (far away)
            Triple(80.0, 35.0, Pair(1.0/16, 1.0/4)),
            // Large targets (close up)
            Triple(100.0, 45.0, Pair(1.0/4, 3.0/4))
        )

        for ((param1, param2, radiusFactors) in paramSets) {
            val circles = Mat()
            val minRadius = (imageSize * radiusFactors.first).toInt()
            val maxRadius = (imageSize * radiusFactors.second).toInt()

            Imgproc.HoughCircles(
                gray,
                circles,
                Imgproc.HOUGH_GRADIENT,
                1.0,                            // dp - inverse ratio of resolution
                gray.rows() / 8.0,              // minDist - allow closer circles for concentric detection
                param1,                         // param1 - Canny edge threshold
                param2,                         // param2 - accumulator threshold
                minRadius,
                maxRadius
            )

            if (circles.cols() > 0) {
                for (i in 0 until circles.cols()) {
                    val circle = circles.get(0, i) ?: continue
                    if (circle.size >= 3) {
                        allCircles.add(Triple(Point(circle[0], circle[1]), circle[2], mat))
                    }
                }
            }
            circles.release()
        }

        Log.d(TAG, "Found ${allCircles.size} potential circles across all parameter sweeps")

        var bestCircle: Triple<Point, Double, Double>? = null
        var bestScore = 0.0

        // Deduplicate circles that are very similar (same center within tolerance)
        val uniqueCircles = deduplicateCircles(allCircles.map { Pair(it.first, it.second) })

        Log.d(TAG, "After deduplication: ${uniqueCircles.size} unique circles")

        // Evaluate each detected circle
        for ((index, circlePair) in uniqueCircles.withIndex()) {
            val center = circlePair.first
            val radius = circlePair.second

            // Validate circle by checking for target colors
            val colorScore = validateTargetColors(mat, center, radius)

            // Prefer larger circles - add bonus for size (normalized to image size)
            val sizeBonus = (radius / gray.rows()) * 2.0
            val totalScore = colorScore + sizeBonus

            Log.d(TAG, "Circle $index: center=(${center.x}, ${center.y}), radius=$radius, colorScore=$colorScore, sizeBonus=$sizeBonus, total=$totalScore")

            if (totalScore > bestScore) {
                bestScore = totalScore
                bestCircle = Triple(center, radius, colorScore)
            }
        }

        // Cleanup
        mat.release()
        gray.release()

        return bestCircle?.let { (center, radius, confidence) ->
            Log.d(TAG, "Best circle: center=(${center.x}, ${center.y}), radius=$radius, score=$confidence")
            TargetDetection(
                center = PointF(center.x.toFloat(), center.y.toFloat()),
                radius = radius.toFloat(),
                confidence = (confidence / 5.0).toFloat().coerceIn(0f, 1f)  // Max score ~5 with graduated scoring
            )
        }
    }

    private fun deduplicateCircles(circles: List<Pair<Point, Double>>): List<Pair<Point, Double>> {
        if (circles.isEmpty()) return emptyList()

        val result = mutableListOf<Pair<Point, Double>>()
        val used = BooleanArray(circles.size)

        // Sort by radius descending - prefer larger circles
        val sorted = circles.sortedByDescending { it.second }

        for (i in sorted.indices) {
            if (used[i]) continue

            val (center1, radius1) = sorted[i]
            var bestRadius = radius1
            var bestCenter = center1

            // Find all similar circles and keep the largest
            for (j in i + 1 until sorted.size) {
                if (used[j]) continue

                val (center2, radius2) = sorted[j]
                val centerDist = kotlin.math.sqrt(
                    (center1.x - center2.x) * (center1.x - center2.x) +
                    (center1.y - center2.y) * (center1.y - center2.y)
                )

                // If centers are close (within 20% of the average radius), consider them duplicates
                val avgRadius = (radius1 + radius2) / 2
                if (centerDist < avgRadius * 0.2) {
                    used[j] = true
                    if (radius2 > bestRadius) {
                        bestRadius = radius2
                        bestCenter = center2
                    }
                }
            }

            result.add(Pair(bestCenter, bestRadius))
            used[i] = true
        }

        return result
    }

    private fun validateTargetColors(mat: Mat, center: Point, radius: Double): Double {
        return try {
            val hsv = Mat()
            Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_RGBA2RGB)
            Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_RGB2HSV)

            var score = 0.0

            // Check for gold/yellow near center (high scoring area)
            // Use graduated scoring instead of binary pass/fail
            val goldMask = Mat()
            Core.inRange(hsv, GOLD_LOW, GOLD_HIGH, goldMask)
            val goldRatio = countPixelsInRing(goldMask, center, 0.0, radius * 0.25)
            score += when {
                goldRatio > 0.25 -> 1.0
                goldRatio > 0.15 -> 0.7
                goldRatio > 0.08 -> 0.4
                else -> 0.0
            }

            // Check for red in middle rings
            val redMask1 = Mat()
            val redMask2 = Mat()
            val redMask = Mat()
            Core.inRange(hsv, RED_LOW_1, RED_HIGH_1, redMask1)
            Core.inRange(hsv, RED_LOW_2, RED_HIGH_2, redMask2)
            Core.bitwise_or(redMask1, redMask2, redMask)
            val redRatio = countPixelsInRing(redMask, center, radius * 0.2, radius * 0.45)
            score += when {
                redRatio > 0.15 -> 1.0
                redRatio > 0.08 -> 0.6
                redRatio > 0.03 -> 0.3
                else -> 0.0
            }

            // Check for blue in middle-outer rings
            val blueMask = Mat()
            Core.inRange(hsv, BLUE_LOW, BLUE_HIGH, blueMask)
            val blueRatio = countPixelsInRing(blueMask, center, radius * 0.35, radius * 0.65)
            score += when {
                blueRatio > 0.15 -> 1.0
                blueRatio > 0.08 -> 0.6
                blueRatio > 0.03 -> 0.3
                else -> 0.0
            }

            // Check for black rings (scoring lines) - common on all targets
            val blackMask = Mat()
            Core.inRange(hsv, Scalar(0.0, 0.0, 0.0), Scalar(180.0, 255.0, 50.0), blackMask)
            val blackRatio = countPixelsInRing(blackMask, center, radius * 0.1, radius * 0.9)
            // Some black pixels indicate scoring rings - but not too many (that would be a dark blob)
            if (blackRatio > 0.02 && blackRatio < 0.25) score += 0.5

            // Check for white/light areas (outer rings or between colored rings)
            val whiteMask = Mat()
            Core.inRange(hsv, Scalar(0.0, 0.0, 160.0), Scalar(180.0, 60.0, 255.0), whiteMask)
            val whiteRatio = countPixelsInRing(whiteMask, center, radius * 0.7, radius * 1.0)
            score += when {
                whiteRatio > 0.12 -> 0.5
                whiteRatio > 0.05 -> 0.3
                else -> 0.0
            }

            blackMask.release()

            // Cleanup
            hsv.release()
            goldMask.release()
            redMask1.release()
            redMask2.release()
            redMask.release()
            blueMask.release()
            whiteMask.release()

            score
        } catch (e: Exception) {
            Log.e(TAG, "Error validating target colors", e)
            0.0
        }
    }

    private fun countPixelsInRing(mask: Mat, center: Point, innerRadius: Double, outerRadius: Double): Double {
        var count = 0
        var total = 0

        val startY = (center.y - outerRadius).toInt().coerceIn(0, mask.rows() - 1)
        val endY = (center.y + outerRadius).toInt().coerceIn(0, mask.rows() - 1)
        val startX = (center.x - outerRadius).toInt().coerceIn(0, mask.cols() - 1)
        val endX = (center.x + outerRadius).toInt().coerceIn(0, mask.cols() - 1)

        for (y in startY until endY step 4) {
            for (x in startX until endX step 4) {
                val dx = x - center.x
                val dy = y - center.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                if (dist >= innerRadius && dist < outerRadius) {
                    total++
                    val pixel = mask.get(y, x)
                    if (pixel != null && pixel.isNotEmpty() && pixel[0] > 0) {
                        count++
                    }
                }
            }
        }

        return if (total > 0) count.toDouble() / total else 0.0
    }
}
