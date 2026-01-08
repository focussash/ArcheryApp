package com.example.archeryapp.detection.opencv

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.TargetDetection
import com.example.archeryapp.domain.detection.ArrowDetector
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class OpenCvArrowDetector : ArrowDetector {

    companion object {
        private const val TAG = "OpenCvArrowDetector"
        private const val MIN_LINE_LENGTH = 50.0  // Relaxed from 80
        private const val MAX_LINE_GAP = 15.0
        private const val ARROW_GROUP_DISTANCE = 60.0  // Grouping radius
        private const val MAX_ARROWS = 12  // Realistic max arrows on a target
        private const val MIN_GROUP_SIZE = 1  // Allow single line segments
        private var isOpenCvInitialized = false

        init {
            try {
                isOpenCvInitialized = OpenCVLoader.initLocal()
            } catch (e: Exception) {
                Log.e(TAG, "OpenCV initialization failed", e)
                isOpenCvInitialized = false
            }
        }
    }

    override fun detect(bitmap: Bitmap, target: TargetDetection): List<ArrowDetection> {
        if (!isOpenCvInitialized) {
            Log.e(TAG, "OpenCV not initialized, cannot detect arrows")
            return emptyList()
        }

        return try {
            detectArrows(bitmap, target)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting arrows", e)
            emptyList()
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "OpenCV native library not found", e)
            emptyList()
        }
    }

    private fun detectArrows(bitmap: Bitmap, target: TargetDetection): List<ArrowDetection> {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)

        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(gray, gray, Size(7.0, 7.0), 2.0)

        // Edge detection with higher thresholds to reduce noise
        val edges = Mat()
        Imgproc.Canny(gray, edges, 80.0, 200.0)

        // Detect lines using Hough Transform
        val lines = Mat()
        Imgproc.HoughLinesP(
            edges,
            lines,
            1.0,                    // rho - distance resolution
            Math.PI / 180,          // theta - angle resolution
            70,                     // threshold - balanced
            MIN_LINE_LENGTH,        // minLineLength
            MAX_LINE_GAP            // maxLineGap
        )

        Log.d(TAG, "Found ${lines.rows()} lines")

        val targetCenter = Point(target.center.x.toDouble(), target.center.y.toDouble())
        val targetRadius = target.radius.toDouble()

        // Filter and process lines with strict criteria
        val arrowCandidates = mutableListOf<ArrowCandidate>()

        for (i in 0 until lines.rows()) {
            val line = lines.get(i, 0) ?: continue
            if (line.size < 4) continue

            val x1 = line[0]
            val y1 = line[1]
            val x2 = line[2]
            val y2 = line[3]

            val p1 = Point(x1, y1)
            val p2 = Point(x2, y2)

            val lineLength = distance(p1, p2)

            // Skip short lines
            if (lineLength < MIN_LINE_LENGTH) continue

            // Check distances from center
            val dist1 = distance(p1, targetCenter)
            val dist2 = distance(p2, targetCenter)

            // Line should have one end inside/near target
            val minDist = minOf(dist1, dist2)
            val maxDist = maxOf(dist1, dist2)

            if (minDist > targetRadius * 1.2) continue  // Both ends too far from center

            // Find the end closest to center (arrow tip)
            val tip = if (dist1 < dist2) p1 else p2
            val tail = if (dist1 < dist2) p2 else p1
            val tipDist = minOf(dist1, dist2)
            val tailDist = maxOf(dist1, dist2)

            // Arrow should span some distance (not just on edge of target)
            val radialSpan = tailDist - tipDist
            if (radialSpan < lineLength * 0.3) continue  // Line is more tangent than radial

            // Check if line points toward center (radial orientation)
            val lineAngle = atan2(tip.y - tail.y, tip.x - tail.x)
            val centerAngle = atan2(targetCenter.y - tail.y, targetCenter.x - tail.x)
            val angleDiff = abs(normalizeAngle(lineAngle - centerAngle))

            // Angle check - within 45 degrees of radial
            if (angleDiff < Math.PI / 4) {
                arrowCandidates.add(ArrowCandidate(tip, lineLength, angleDiff))
            }
        }

        Log.d(TAG, "Found ${arrowCandidates.size} arrow candidates after filtering")

        // Group nearby arrow candidates
        val groupedArrows = groupArrowCandidates(arrowCandidates, targetCenter, targetRadius)

        Log.d(TAG, "Grouped into ${groupedArrows.size} arrows")

        // Cleanup
        mat.release()
        gray.release()
        edges.release()
        lines.release()

        return groupedArrows
            .take(MAX_ARROWS)  // Limit to realistic number
            .map { candidate ->
                ArrowDetection(
                    position = PointF(candidate.tip.x.toFloat(), candidate.tip.y.toFloat()),
                    confidence = calculateConfidence(candidate)
                )
            }
    }

    private fun groupArrowCandidates(
        candidates: List<ArrowCandidate>,
        targetCenter: Point,
        targetRadius: Double
    ): List<ArrowCandidate> {
        if (candidates.isEmpty()) return emptyList()

        val grouped = mutableListOf<MutableList<ArrowCandidate>>()

        // Sort by line length (longer lines are more likely to be arrows)
        val sortedCandidates = candidates.sortedByDescending { it.length }

        for (candidate in sortedCandidates) {
            var addedToGroup = false

            for (group in grouped) {
                val avgTip = Point(
                    group.map { it.tip.x }.average(),
                    group.map { it.tip.y }.average()
                )
                if (distance(candidate.tip, avgTip) < ARROW_GROUP_DISTANCE) {
                    group.add(candidate)
                    addedToGroup = true
                    break
                }
            }

            if (!addedToGroup) {
                grouped.add(mutableListOf(candidate))
            }
        }

        // Only keep groups with enough supporting line segments
        // Take the best candidate from each qualifying group
        return grouped
            .filter { group -> group.size >= MIN_GROUP_SIZE }
            .mapNotNull { group ->
                group.maxByOrNull { it.length / (it.angleDiff + 0.1) }
            }
            .filter { candidate ->
                // Only keep arrows within the target
                distance(candidate.tip, targetCenter) <= targetRadius
            }
            .sortedByDescending { it.length }  // Return longest/best arrows first
    }

    private fun calculateConfidence(candidate: ArrowCandidate): Float {
        // Higher confidence for longer lines with better angle alignment
        val lengthScore = (candidate.length / 100.0).coerceIn(0.0, 1.0)
        val angleScore = 1.0 - (candidate.angleDiff / (Math.PI / 3))
        return ((lengthScore + angleScore) / 2.0).toFloat()
    }

    private fun distance(p1: Point, p2: Point): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun normalizeAngle(angle: Double): Double {
        var a = angle
        while (a > Math.PI) a -= 2 * Math.PI
        while (a < -Math.PI) a += 2 * Math.PI
        return a
    }

    private data class ArrowCandidate(
        val tip: Point,
        val length: Double,
        val angleDiff: Double
    )
}
