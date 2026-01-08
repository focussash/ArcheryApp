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
        // Gold/Yellow (10, 9 rings)
        private val GOLD_LOW = Scalar(15.0, 100.0, 100.0)
        private val GOLD_HIGH = Scalar(35.0, 255.0, 255.0)

        // Red (8, 7 rings)
        private val RED_LOW_1 = Scalar(0.0, 100.0, 100.0)
        private val RED_HIGH_1 = Scalar(10.0, 255.0, 255.0)
        private val RED_LOW_2 = Scalar(160.0, 100.0, 100.0)
        private val RED_HIGH_2 = Scalar(180.0, 255.0, 255.0)

        // Blue (6, 5 rings)
        private val BLUE_LOW = Scalar(100.0, 100.0, 50.0)
        private val BLUE_HIGH = Scalar(130.0, 255.0, 255.0)

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

        // Apply Gaussian blur
        Imgproc.GaussianBlur(gray, gray, Size(9.0, 9.0), 2.0)

        // Detect circles using Hough Transform
        val circles = Mat()
        Imgproc.HoughCircles(
            gray,
            circles,
            Imgproc.HOUGH_GRADIENT,
            1.0,                            // dp - inverse ratio of resolution
            gray.rows() / 4.0,              // minDist - minimum distance between circles
            100.0,                          // param1 - higher threshold for Canny edge detector
            50.0,                           // param2 - accumulator threshold
            gray.rows() / 8,                // minRadius
            gray.rows() / 2                 // maxRadius
        )

        Log.d(TAG, "Found ${circles.cols()} potential circles")

        var bestCircle: Triple<Point, Double, Double>? = null
        var bestScore = 0.0

        // Evaluate each detected circle
        for (i in 0 until circles.cols()) {
            val circle = circles.get(0, i)
            if (circle == null || circle.size < 3) continue

            val center = Point(circle[0], circle[1])
            val radius = circle[2]

            // Validate circle by checking for target colors
            val colorScore = validateTargetColors(mat, center, radius)

            Log.d(TAG, "Circle $i: center=(${center.x}, ${center.y}), radius=$radius, colorScore=$colorScore")

            if (colorScore > bestScore) {
                bestScore = colorScore
                bestCircle = Triple(center, radius, colorScore)
            }
        }

        // Cleanup
        mat.release()
        gray.release()
        circles.release()

        return bestCircle?.let { (center, radius, confidence) ->
            Log.d(TAG, "Best circle: center=(${center.x}, ${center.y}), radius=$radius")
            TargetDetection(
                center = PointF(center.x.toFloat(), center.y.toFloat()),
                radius = radius.toFloat(),
                confidence = (confidence / 3.0).toFloat().coerceIn(0f, 1f)
            )
        }
    }

    private fun validateTargetColors(mat: Mat, center: Point, radius: Double): Double {
        return try {
            val hsv = Mat()
            Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_RGBA2RGB)
            Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_RGB2HSV)

            var score = 0.0

            // Check for gold/yellow near center (high scoring area)
            val goldMask = Mat()
            Core.inRange(hsv, GOLD_LOW, GOLD_HIGH, goldMask)
            val goldRatio = countPixelsInRing(goldMask, center, 0.0, radius * 0.2)
            if (goldRatio > 0.3) score += 1.0

            // Check for red in middle rings
            val redMask1 = Mat()
            val redMask2 = Mat()
            val redMask = Mat()
            Core.inRange(hsv, RED_LOW_1, RED_HIGH_1, redMask1)
            Core.inRange(hsv, RED_LOW_2, RED_HIGH_2, redMask2)
            Core.bitwise_or(redMask1, redMask2, redMask)
            val redRatio = countPixelsInRing(redMask, center, radius * 0.2, radius * 0.4)
            if (redRatio > 0.2) score += 1.0

            // Check for blue in outer rings
            val blueMask = Mat()
            Core.inRange(hsv, BLUE_LOW, BLUE_HIGH, blueMask)
            val blueRatio = countPixelsInRing(blueMask, center, radius * 0.4, radius * 0.6)
            if (blueRatio > 0.2) score += 1.0

            // Cleanup
            hsv.release()
            goldMask.release()
            redMask1.release()
            redMask2.release()
            redMask.release()
            blueMask.release()

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
