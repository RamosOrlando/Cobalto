package com.cobalto

import androidx.room.Room
import androidx.room.RoomDatabase
import com.cobalto.core.database.AppDatabase
import com.cobalto.core.common.util.pdf.IosPdfGenerator
import com.cobalto.core.common.util.pdf.PdfGenerator
import com.cobalto.core.common.util.share.IosShareManager
import com.cobalto.core.common.util.share.ShareManager
import platform.Foundation.NSHomeDirectory
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = NSHomeDirectory() + "/materials.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFile
        ).fallbackToDestructiveMigration(true)
    }

    single<PdfGenerator> { IosPdfGenerator() }
    single<ShareManager> { IosShareManager() }
}
