package com.cobalto.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.cobalto.core.database.category.CategoryDao
import com.cobalto.core.database.category.CategoryEntity
import com.cobalto.core.database.db.DbDao
import com.cobalto.core.database.db.DbEntity
import com.cobalto.core.database.db_item.DbItemDao
import com.cobalto.core.database.db_item.DbItemEntity
import com.cobalto.core.database.db_item_resource.DbItemResourceDao
import com.cobalto.core.database.db_item_resource.DbItemResourceEntity
import com.cobalto.core.database.section.SectionDao
import com.cobalto.core.database.section.SectionEntity
import com.cobalto.core.database.maker.MakerDao
import com.cobalto.core.database.maker.MakerEntity
import com.cobalto.core.database.resource.ResourceDao
import com.cobalto.core.database.resource.ResourceEntity
import com.cobalto.core.database.price_history.PriceHistoryDao
import com.cobalto.core.database.price_history.PriceHistoryEntity
import com.cobalto.core.database.provider.ProviderDao
import com.cobalto.core.database.provider.ProviderEntity
import com.cobalto.core.database.auth.ProfileDao
import com.cobalto.core.database.auth.ProfileEntity
import com.cobalto.core.database.user.*

@Database(entities = [CategoryEntity::class, SectionEntity::class,
    MakerEntity::class, ResourceEntity::class, ProviderEntity::class,
    PriceHistoryEntity::class, ProfileEntity::class, DbEntity::class,
    DbItemEntity::class, DbItemResourceEntity::class,
    UserEntity::class, UserRoleEntity::class, UserProfessionEntity::class,
    UserPlanEntity::class, SubscriptionHistoryEntity::class], version = 25)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun sectionDao(): SectionDao
    abstract fun makerDao(): MakerDao
    abstract fun resourceDao(): ResourceDao
    abstract fun providerDao(): ProviderDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun profileDao(): ProfileDao
    abstract fun userDao(): UserDao
    abstract fun dbDao(): DbDao
    abstract fun dbItemDao(): DbItemDao
    abstract fun dbItemResourceDao(): DbItemResourceDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : androidx.room.RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
