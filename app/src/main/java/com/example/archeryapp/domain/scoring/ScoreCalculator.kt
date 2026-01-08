package com.example.archeryapp.domain.scoring

import android.graphics.PointF
import com.example.archeryapp.data.model.ArrowDetection
import com.example.archeryapp.data.model.ArrowScore
import com.example.archeryapp.data.model.TargetDetection
import kotlin.math.sqrt

class ScoreCalculator {

    fun calculate(arrows: List<ArrowDetection>, target: TargetDetection): List<ArrowScore> {
        return arrows.map { arrow ->
            calculateArrowScore(arrow, target)
        }
    }

    private fun calculateArrowScore(arrow: ArrowDetection, target: TargetDetection): ArrowScore {
        val distanceFromCenter = calculateDistance(arrow.position, target.center)
        val normalizedDistance = distanceFromCenter / target.radius

        val ring = UsaArcheryTarget.RINGS.find { ring ->
            normalizedDistance >= ring.innerRadiusPercent && normalizedDistance < ring.outerRadiusPercent
        }

        return ArrowScore(
            arrow = arrow,
            score = ring?.score ?: 0,
            isX = ring?.isX ?: false
        )
    }

    private fun calculateDistance(p1: PointF, p2: PointF): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }
}
