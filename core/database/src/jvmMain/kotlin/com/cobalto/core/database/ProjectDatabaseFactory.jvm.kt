package com.cobalto.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class ProjectDatabaseFactory {
    actual fun createDatabaseBuilder(fileName: String): RoomDatabase.Builder<AppDatabase> {
        val userHome = System.getProperty("user.home") ?: "."
        val baseDir = File(userHome, ".cobalto")
        baseDir.mkdirs()
        val file = if (fileName.startsWith("/")) File(fileName) else File(baseDir, fileName)
        file.parentFile?.mkdirs()
        return Room.databaseBuilder<AppDatabase>(
            name = file.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).fallbackToDestructiveMigration(true)
    }

    actual fun deleteDatabaseFile(fileName: String): Boolean {
        val userHome = System.getProperty("user.home") ?: "."
        val baseDir = File(userHome, ".cobalto")
        val file = if (fileName.startsWith("/")) File(fileName) else File(baseDir, fileName)
        val deleted = file.delete()
        File("${file.absolutePath}-wal").delete()
        File("${file.absolutePath}-shm").delete()
        return deleted
    }
}
