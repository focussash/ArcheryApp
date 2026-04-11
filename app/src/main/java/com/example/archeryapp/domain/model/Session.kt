package com.example.archeryapp.domain.model

import java.time.LocalDateTime

data class Session(
    val id: Long = 0,
    val date: LocalDateTime,
    val distance: String? = null,
    val bowType: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val targetType: TargetType = TargetType.MINI_MC,
    val ends: List<End> = emptyList()
) {
    val totalScore: Int get() = ends.sumOf { it.totalScore }
    val totalArrows: Int get() = ends.sumOf { it.arrows.size }
    val totalXCount: Int get() = ends.sumOf { it.xCount }
    val averagePerArrow: Float get() = if (totalArrows > 0) totalScore.toFloat() / totalArrows else 0f
}
