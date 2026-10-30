package com.cobalto

import androidx.room.Room
import androidx.room.RoomDatabase
import com.cobalto.core.database.AppDatabase
import com.cobalto.core.common.util.pdf.JvmPdfGenerator
import com.cobalto.core.common.util.pdf.PdfGenerator
import com.cobalto.core.common.util.share.JvmShareManager
import com.cobalto.core.common.util.share.ShareManager
import java.io.File
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = File(System.getProperty("user.home"), "materials.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        ).fallbackToDestructiveMigration(true)
    }

    single<PdfGenerator> { JvmPdfGenerator() }
    single<ShareManager> { JvmShareManager() }
}
