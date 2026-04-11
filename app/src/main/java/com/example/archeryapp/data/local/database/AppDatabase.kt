package com.example.archeryapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.archeryapp.data.local.dao.ArrowScoreDao
import com.example.archeryapp.data.local.dao.EndDao
import com.example.archeryapp.data.local.dao.SessionDao
import com.example.archeryapp.data.local.entity.ArrowScoreEntity
import com.example.archeryapp.data.local.entity.EndEntity
import com.example.archeryapp.data.local.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        EndEntity::class,
        ArrowScoreEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun endDao(): EndDao
    abstract fun arrowScoreDao(): ArrowScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "archery_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
