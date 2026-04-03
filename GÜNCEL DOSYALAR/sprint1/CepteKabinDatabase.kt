package com.cyberqbit.ceptekabin.data.local.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cyberqbit.ceptekabin.data.local.database.dao.*
import com.cyberqbit.ceptekabin.data.local.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        KiyaketEntity::class,
        KombinEntity::class,
        KategoriEntity::class,
        BarkodOnbellekEntity::class,
        SezonluUrunEntity::class,
        WeatherCacheEntity::class,       // Sprint 1 — skeleton loading
        KombinKullanimEntity::class      // Sprint 2 — OOTD takvim
    ],
    version = 4,
    exportSchema = false
)
abstract class CepteKabinDatabase : RoomDatabase() {
    abstract fun kiyaketDao(): KiyaketDao
    abstract fun kombinDao(): KombinDao
    abstract fun kategoriDao(): KategoriDao
    abstract fun barkodOnbellekDao(): BarkodOnbellekDao
    abstract fun sezonluUrunDao(): SezonluUrunDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun kombinKullanimDao(): KombinKullanimDao

    companion object {
        @Volatile private var INSTANCE: CepteKabinDatabase? = null

        fun getDatabase(context: Context): CepteKabinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CepteKabinDatabase::class.java,
                    "ceptekabin_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.kategoriDao())
                }
            }
        }

        suspend fun populateDatabase(kategoriDao: KategoriDao) {
            kategoriDao.insertAll(listOf(
                KategoriEntity(1, "Üst Giyim",  "shirt",     1),
                KategoriEntity(2, "Alt Giyim",  "pants",     2),
                KategoriEntity(3, "Dış Giyim",  "jacket",    3),
                KategoriEntity(4, "Ayakkabı",   "shoe",      4),
                KategoriEntity(5, "Aksesuar",   "accessory", 5)
            ))
        }
    }
}
