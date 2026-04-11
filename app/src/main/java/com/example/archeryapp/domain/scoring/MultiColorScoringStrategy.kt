package com.example.archeryapp.domain.scoring

object MultiColorScoringStrategy : ScoringStrategy {
    override fun normalize(rawRingScore: Int, isX: Boolean): Int = rawRingScore.coerceIn(0, 10)

    override fun isButtonVisible(ringValue: Int): Boolean = ringValue in 0..10
}
