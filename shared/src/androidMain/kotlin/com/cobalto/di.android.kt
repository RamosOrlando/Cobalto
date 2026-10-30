package com.cobalto

import androidx.room.Room
import androidx.room.RoomDatabase
import com.cobalto.core.database.AppDatabase
import com.cobalto.core.common.util.pdf.AndroidPdfGenerator
import com.cobalto.core.common.util.pdf.PdfGenerator
import com.cobalto.core.common.util.share.AndroidShareManager
import com.cobalto.core.common.util.share.ShareManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("materials.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        ).fallbackToDestructiveMigration(true)
    }
    
    single<PdfGenerator> { AndroidPdfGenerator(androidContext()) }
    single<ShareManager> { AndroidShareManager(androidContext()) }
}
