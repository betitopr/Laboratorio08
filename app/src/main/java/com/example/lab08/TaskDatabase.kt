package com.example.lab08
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar la nueva columna createdAt con un valor por defecto
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN created_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                )
            }
        }
    }
}

