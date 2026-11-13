package com.cobalto.core.database

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProjectDatabaseManager(
    private val factory: ProjectDatabaseFactory
) {
    private val instances = mutableMapOf<String, AppDatabase>()
    private val mutex = Mutex()

    suspend fun getDatabaseForFile(filePath: String): AppDatabase {
        return mutex.withLock {
            instances.getOrPut(filePath) {
                val builder = factory.createDatabaseBuilder(filePath)
                getRoomDatabase(builder)
            }
        }
    }

    suspend fun closeDatabase(filePath: String) {
        mutex.withLock {
            instances.remove(filePath)?.close()
        }
    }

    suspend fun deleteProjectDatabaseFile(filePath: String): Boolean {
        closeDatabase(filePath)
        return factory.deleteDatabaseFile(filePath)
    }
}
