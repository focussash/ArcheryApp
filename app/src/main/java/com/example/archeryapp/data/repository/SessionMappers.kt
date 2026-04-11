package com.example.archeryapp.data.repository

import com.example.archeryapp.data.local.entity.SessionEntity
import com.example.archeryapp.domain.model.Session
import com.example.archeryapp.domain.model.TargetType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    date = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    distance = distance,
    bowType = bowType,
    location = location,
    notes = notes,
    targetType = targetType.code
)

internal fun SessionEntity.toDomain(): Session = Session(
    id = id,
    date = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()),
    distance = distance,
    bowType = bowType,
    location = location,
    notes = notes,
    targetType = TargetType.fromCode(targetType),
    ends = emptyList()
)
