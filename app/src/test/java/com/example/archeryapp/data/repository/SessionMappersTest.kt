package com.example.archeryapp.data.repository

import com.example.archeryapp.data.local.entity.SessionEntity
import com.example.archeryapp.domain.model.Session
import com.example.archeryapp.domain.model.TargetType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class SessionMappersTest {

    private val fixedDateTime = LocalDateTime.of(2026, 4, 10, 14, 30)
    private val fixedMillis =
        fixedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Test
    fun `domain to entity stores BF as code string`() {
        val session = Session(
            id = 1,
            date = fixedDateTime,
            targetType = TargetType.BF
        )
        assertEquals("BF", session.toEntity().targetType)
    }

    @Test
    fun `domain to entity stores code for every target type`() {
        TargetType.entries.forEach { type ->
            val session = Session(date = fixedDateTime, targetType = type)
            assertEquals(type.code, session.toEntity().targetType)
        }
    }

    @Test
    fun `entity with BF code maps back to BF enum`() {
        val entity = SessionEntity(
            id = 1,
            date = fixedMillis,
            distance = "18m",
            bowType = "recurve",
            location = "range",
            notes = null,
            targetType = "BF"
        )
        assertEquals(TargetType.BF, entity.toDomain().targetType)
    }

    @Test
    fun `entity with null targetType is legacy-backfilled to MINI_MC`() {
        val entity = SessionEntity(
            id = 1,
            date = fixedMillis,
            distance = null,
            bowType = null,
            location = null,
            notes = null,
            targetType = null
        )
        assertEquals(TargetType.MINI_MC, entity.toDomain().targetType)
    }

    @Test
    fun `round-trip preserves BF`() {
        val original = Session(
            id = 1,
            date = fixedDateTime,
            distance = "18m",
            bowType = "recurve",
            location = "range",
            notes = "notes",
            targetType = TargetType.BF
        )
        assertEquals(TargetType.BF, original.toEntity().toDomain().targetType)
    }

    @Test
    fun `round-trip preserves every target type`() {
        TargetType.entries.forEach { type ->
            val original = Session(date = fixedDateTime, targetType = type)
            assertEquals(type, original.toEntity().toDomain().targetType)
        }
    }

    @Test
    fun `round-trip preserves all non-target fields alongside targetType`() {
        val original = Session(
            id = 42,
            date = fixedDateTime,
            distance = "70m",
            bowType = "compound",
            location = "outdoor range",
            notes = "PR attempt",
            targetType = TargetType.TRIPLE
        )
        val result = original.toEntity().toDomain()
        assertEquals(42L, result.id)
        assertEquals("70m", result.distance)
        assertEquals("compound", result.bowType)
        assertEquals("outdoor range", result.location)
        assertEquals("PR attempt", result.notes)
        assertEquals(TargetType.TRIPLE, result.targetType)
    }
}
