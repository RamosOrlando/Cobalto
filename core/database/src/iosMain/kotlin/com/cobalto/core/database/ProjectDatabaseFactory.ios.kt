package com.cobalto.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSFileManager
import kotlinx.cinterop.ExperimentalForeignApi

actual class ProjectDatabaseFactory {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDatabaseBuilder(fileName: String): RoomDatabase.Builder<AppDatabase> {
        val dbPath = if (fileName.startsWith("/")) fileName else NSHomeDirectory() + "/" + fileName
        val fileManager = NSFileManager.defaultManager
        val parentDir = dbPath.substringBeforeLast("/", "")
        if (parentDir.isNotEmpty()) {
            fileManager.createDirectoryAtPath(parentDir, true, null, null)
        }
        return Room.databaseBuilder<AppDatabase>(
            name = dbPath,
            factory = { AppDatabaseConstructor.initialize() }
        ).fallbackToDestructiveMigration(true)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteDatabaseFile(fileName: String): Boolean {
        val dbPath = if (fileName.startsWith("/")) fileName else NSHomeDirectory() + "/" + fileName
        val fileManager = NSFileManager.defaultManager
        val deleted = fileManager.removeItemAtPath(dbPath, null)
        fileManager.removeItemAtPath("$dbPath-wal", null)
        fileManager.removeItemAtPath("$dbPath-shm", null)
        return deleted
    }
}
