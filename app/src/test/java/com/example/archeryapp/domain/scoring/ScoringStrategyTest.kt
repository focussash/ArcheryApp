package com.example.archeryapp.domain.scoring

import com.example.archeryapp.domain.model.TargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringStrategyTest {

    @Test
    fun `multicolor passes through all valid ring values`() {
        val s = MultiColorScoringStrategy
        assertEquals(10, s.normalize(10, isX = false))
        assertEquals(10, s.normalize(10, isX = true))
        assertEquals(8, s.normalize(8, isX = false))
        assertEquals(5, s.normalize(5, isX = false))
        assertEquals(1, s.normalize(1, isX = false))
        assertEquals(0, s.normalize(0, isX = false))
    }

    @Test
    fun `multicolor shows every button including miss`() {
        val s = MultiColorScoringStrategy
        for (v in 0..10) {
            assertTrue("ring $v should be visible", s.isButtonVisible(v))
        }
    }

    @Test
    fun `blue face demotes rings 1 through 5 to miss`() {
        val s = BlueFaceScoringStrategy
        assertEquals(0, s.normalize(1, isX = false))
        assertEquals(0, s.normalize(2, isX = false))
        assertEquals(0, s.normalize(3, isX = false))
        assertEquals(0, s.normalize(4, isX = false))
        assertEquals(0, s.normalize(5, isX = false))
    }

    @Test
    fun `blue face keeps rings 6 through X`() {
        val s = BlueFaceScoringStrategy
        assertEquals(6, s.normalize(6, isX = false))
        assertEquals(7, s.normalize(7, isX = false))
        assertEquals(8, s.normalize(8, isX = false))
        assertEquals(9, s.normalize(9, isX = false))
        assertEquals(10, s.normalize(10, isX = false))
        assertEquals(10, s.normalize(10, isX = true))
    }

    @Test
    fun `blue face keeps explicit miss as miss`() {
        assertEquals(0, BlueFaceScoringStrategy.normalize(0, isX = false))
    }

    @Test
    fun `blue face hides buttons 1 through 5`() {
        val s = BlueFaceScoringStrategy
        for (v in 1..5) {
            assertFalse("ring $v should NOT be visible on BF", s.isButtonVisible(v))
        }
    }

    @Test
    fun `blue face shows miss and 6 through 10`() {
        val s = BlueFaceScoringStrategy
        assertTrue(s.isButtonVisible(0))
        for (v in 6..10) {
            assertTrue("ring $v should be visible on BF", s.isButtonVisible(v))
        }
    }

    @Test
    fun `factory returns multicolor for MC, MINI_MC, TRIPLE`() {
        assertTrue(ScoringStrategy.forTarget(TargetType.MC) is MultiColorScoringStrategy)
        assertTrue(ScoringStrategy.forTarget(TargetType.MINI_MC) is MultiColorScoringStrategy)
        assertTrue(ScoringStrategy.forTarget(TargetType.TRIPLE) is MultiColorScoringStrategy)
    }

    @Test
    fun `factory returns blue face for BF`() {
        assertTrue(ScoringStrategy.forTarget(TargetType.BF) is BlueFaceScoringStrategy)
    }

    @Test
    fun `target type from null code returns MINI_MC as legacy default`() {
        assertEquals(TargetType.MINI_MC, TargetType.fromCode(null))
    }

    @Test
    fun `target type from unknown code returns MINI_MC as legacy default`() {
        assertEquals(TargetType.MINI_MC, TargetType.fromCode("NONSENSE"))
    }

    @Test
    fun `target type from known code returns matching enum`() {
        assertEquals(TargetType.MC, TargetType.fromCode("MC"))
        assertEquals(TargetType.BF, TargetType.fromCode("BF"))
        assertEquals(TargetType.MINI_MC, TargetType.fromCode("MINI_MC"))
        assertEquals(TargetType.TRIPLE, TargetType.fromCode("TRIPLE"))
    }
}
