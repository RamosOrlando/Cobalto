package com.cobalto.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class ProjectDatabaseFactory(private val context: Context) {
    actual fun createDatabaseBuilder(fileName: String): RoomDatabase.Builder<AppDatabase> {
        val file = if (fileName.startsWith("/")) {
            File(fileName)
        } else {
            val baseDbDir = context.getDatabasePath("materials.db").parentFile ?: context.filesDir
            baseDbDir.mkdirs()
            File(baseDbDir, fileName)
        }
        file.parentFile?.mkdirs()
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = file.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).fallbackToDestructiveMigration(true)
    }

    actual fun deleteDatabaseFile(fileName: String): Boolean {
        val file = if (fileName.startsWith("/")) {
            File(fileName)
        } else {
            val baseDbDir = context.getDatabasePath("materials.db").parentFile ?: context.filesDir
            File(baseDbDir, fileName)
        }
        val deleted = file.delete()
        File("${file.absolutePath}-wal").delete()
        File("${file.absolutePath}-shm").delete()
        return deleted
    }
}
