package com.example.archeryapp.domain.scoring

import com.example.archeryapp.domain.model.TargetType

interface ScoringStrategy {
    fun normalize(rawRingScore: Int, isX: Boolean): Int
    fun isButtonVisible(ringValue: Int): Boolean

    companion object {
        fun forTarget(type: TargetType): ScoringStrategy = when (type) {
            TargetType.MC, TargetType.MINI_MC, TargetType.TRIPLE -> MultiColorScoringStrategy
            TargetType.BF -> BlueFaceScoringStrategy
        }
    }
}
