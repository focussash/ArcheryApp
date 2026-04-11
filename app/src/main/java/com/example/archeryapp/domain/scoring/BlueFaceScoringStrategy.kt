package com.example.archeryapp.domain.scoring

object BlueFaceScoringStrategy : ScoringStrategy {
    override fun normalize(rawRingScore: Int, isX: Boolean): Int {
        val clamped = rawRingScore.coerceIn(0, 10)
        // Rings 1-5 count as miss on a Blue Face target.
        return if (clamped in 1..5) 0 else clamped
    }

    override fun isButtonVisible(ringValue: Int): Boolean =
        ringValue == 0 || ringValue in 6..10
}
