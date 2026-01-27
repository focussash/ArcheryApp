package com.example.archeryapp.domain.model

import java.time.LocalDateTime

data class End(
    val id: Long = 0,
    val endNumber: Int,
    val timestamp: LocalDateTime,
    val arrows: List<Arrow> = emptyList(),
    val notes: String? = null
) {
    val totalScore: Int get() = arrows.sumOf { it.score }
    val xCount: Int get() = arrows.count { it.isX }
}
