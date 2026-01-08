package com.example.archeryapp.domain.scoring

data class TargetRing(
    val score: Int,
    val innerRadiusPercent: Float,
    val outerRadiusPercent: Float,
    val isX: Boolean = false
)

object UsaArcheryTarget {
    // Ring definitions as percentage of total target radius (0.0 to 1.0)
    // Based on USA Archery indoor single-spot target
    val RINGS = listOf(
        TargetRing(score = 10, innerRadiusPercent = 0.00f, outerRadiusPercent = 0.04f, isX = true),  // X ring
        TargetRing(score = 10, innerRadiusPercent = 0.04f, outerRadiusPercent = 0.08f),              // 10 ring
        TargetRing(score = 9, innerRadiusPercent = 0.08f, outerRadiusPercent = 0.16f),
        TargetRing(score = 8, innerRadiusPercent = 0.16f, outerRadiusPercent = 0.24f),
        TargetRing(score = 7, innerRadiusPercent = 0.24f, outerRadiusPercent = 0.32f),
        TargetRing(score = 6, innerRadiusPercent = 0.32f, outerRadiusPercent = 0.40f),
        TargetRing(score = 5, innerRadiusPercent = 0.40f, outerRadiusPercent = 0.48f),
        TargetRing(score = 4, innerRadiusPercent = 0.48f, outerRadiusPercent = 0.56f),
        TargetRing(score = 3, innerRadiusPercent = 0.56f, outerRadiusPercent = 0.64f),
        TargetRing(score = 2, innerRadiusPercent = 0.64f, outerRadiusPercent = 0.72f),
        TargetRing(score = 1, innerRadiusPercent = 0.72f, outerRadiusPercent = 0.80f),
    )

    // Anything beyond 80% of radius is a miss
    const val MISS_THRESHOLD = 0.80f
}
