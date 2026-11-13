package com.cobalto.core.database

import androidx.room.RoomDatabase

expect class ProjectDatabaseFactory {
    fun createDatabaseBuilder(fileName: String): RoomDatabase.Builder<AppDatabase>
    fun deleteDatabaseFile(fileName: String): Boolean
}
