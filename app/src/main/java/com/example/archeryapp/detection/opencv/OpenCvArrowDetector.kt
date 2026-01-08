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
        private const val MIN_LINE_LENGTH = 30.0
        private const val MAX_LINE_GAP = 10.0
        private const val ARROW_GROUP_DISTANCE = 50.0
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

        // Apply Gaussian blur
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 1.5)

        // Edge detection
        val edges = Mat()
        Imgproc.Canny(gray, edges, 50.0, 150.0)

        // Detect lines using Hough Transform
        val lines = Mat()
        Imgproc.HoughLinesP(
            edges,
            lines,
            1.0,                    // rho - distance resolution
            Math.PI / 180,          // theta - angle resolution
            50,                     // threshold
            MIN_LINE_LENGTH,        // minLineLength
            MAX_LINE_GAP            // maxLineGap
        )

        Log.d(TAG, "Found ${lines.rows()} lines")

        val targetCenter = Point(target.center.x.toDouble(), target.center.y.toDouble())
        val targetRadius = target.radius.toDouble()

        // Filter and process lines
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

            // Check if line is within or near target
            val dist1 = distance(p1, targetCenter)
            val dist2 = distance(p2, targetCenter)

            if (dist1 <= targetRadius * 1.5 || dist2 <= targetRadius * 1.5) {
                // Find the end closest to center (arrow tip)
                val tip = if (dist1 < dist2) p1 else p2
                val tail = if (dist1 < dist2) p2 else p1

                // Check if line points roughly toward center (radial orientation)
                val lineAngle = atan2(tip.y - tail.y, tip.x - tail.x)
                val centerAngle = atan2(targetCenter.y - tail.y, targetCenter.x - tail.x)
                val angleDiff = abs(normalizeAngle(lineAngle - centerAngle))

                // Allow lines that are roughly radial (within 60 degrees)
                if (angleDiff < Math.PI / 3) {
                    val lineLength = distance(p1, p2)
                    arrowCandidates.add(ArrowCandidate(tip, lineLength, angleDiff))
                }
            }
        }

        Log.d(TAG, "Found ${arrowCandidates.size} arrow candidates")

        // Group nearby arrow candidates
        val groupedArrows = groupArrowCandidates(arrowCandidates, targetCenter, targetRadius)

        Log.d(TAG, "Grouped into ${groupedArrows.size} arrows")

        // Cleanup
        mat.release()
        gray.release()
        edges.release()
        lines.release()

        return groupedArrows.map { candidate ->
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

        for (candidate in candidates) {
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

        // Take the best candidate from each group (longest line, best angle)
        return grouped.mapNotNull { group ->
            group.maxByOrNull { it.length / (it.angleDiff + 0.1) }
        }.filter { candidate ->
            // Only keep arrows within the target
            distance(candidate.tip, targetCenter) <= targetRadius
        }
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
