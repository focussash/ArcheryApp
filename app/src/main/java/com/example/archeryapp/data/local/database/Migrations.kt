package com.example.archeryapp.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add nullable targetType column. No DEFAULT clause: Room's expected schema
        // has no default either, so the post-migration schema hash matches.
        db.execSQL("ALTER TABLE sessions ADD COLUMN targetType TEXT")
        // Backfill every pre-existing row as MINI_MC per user decision.
        db.execSQL("UPDATE sessions SET targetType = 'MINI_MC' WHERE targetType IS NULL")
    }
}
