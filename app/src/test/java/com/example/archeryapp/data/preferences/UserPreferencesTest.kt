package com.example.archeryapp.data.preferences

import com.example.archeryapp.domain.model.TargetType
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {

    private class FakeStore : TargetTypeStore {
        private var current: String? = null
        override fun read(): String? = current
        override fun write(code: String) {
            current = code
        }
    }

    @Test
    fun `unset returns MINI_MC as legacy default`() {
        val prefs = UserPreferences(FakeStore())
        assertEquals(TargetType.MINI_MC, prefs.getLastTargetType())
    }

    @Test
    fun `set BF then get returns BF`() {
        val prefs = UserPreferences(FakeStore())
        prefs.setLastTargetType(TargetType.BF)
        assertEquals(TargetType.BF, prefs.getLastTargetType())
    }

    @Test
    fun `round-trip preserves every target type`() {
        TargetType.entries.forEach { type ->
            val prefs = UserPreferences(FakeStore())
            prefs.setLastTargetType(type)
            assertEquals(type, prefs.getLastTargetType())
        }
    }

    @Test
    fun `later write overwrites earlier write`() {
        val prefs = UserPreferences(FakeStore())
        prefs.setLastTargetType(TargetType.BF)
        prefs.setLastTargetType(TargetType.TRIPLE)
        assertEquals(TargetType.TRIPLE, prefs.getLastTargetType())
    }

    @Test
    fun `unknown code on read falls back to MINI_MC via TargetType fromCode`() {
        val corruptedStore = object : TargetTypeStore {
            override fun read(): String? = "GARBAGE_VALUE"
            override fun write(code: String) {}
        }
        val prefs = UserPreferences(corruptedStore)
        assertEquals(TargetType.MINI_MC, prefs.getLastTargetType())
    }
}
