package com.cyberqbit.ceptekabin.di

import android.content.Context
import com.cyberqbit.ceptekabin.data.local.database.CepteKabinDatabase
import com.cyberqbit.ceptekabin.data.local.database.dao.*
import com.cyberqbit.ceptekabin.data.local.repository.*
import com.cyberqbit.ceptekabin.data.remote.api.HavaDurumuApiService
import com.cyberqbit.ceptekabin.data.repository.*
import com.cyberqbit.ceptekabin.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CepteKabinDatabase =
        CepteKabinDatabase.getDatabase(context)

    @Provides @Singleton fun provideKiyaketDao(db: CepteKabinDatabase)      = db.kiyaketDao()
    @Provides @Singleton fun provideKombinDao(db: CepteKabinDatabase)       = db.kombinDao()
    @Provides @Singleton fun provideKategoriDao(db: CepteKabinDatabase)     = db.kategoriDao()
    @Provides @Singleton fun provideBarkodOnbellekDao(db: CepteKabinDatabase) = db.barkodOnbellekDao()
    @Provides @Singleton fun provideSezonluUrunDao(db: CepteKabinDatabase)  = db.sezonluUrunDao()
    @Provides @Singleton fun provideWeatherCacheDao(db: CepteKabinDatabase) = db.weatherCacheDao()
    @Provides @Singleton fun provideKombinKullanimDao(db: CepteKabinDatabase) = db.kombinKullanimDao()
    @Provides @Singleton fun provideTakvimGirisiDao(db: CepteKabinDatabase) = db.takvimGirisiDao()

    @Provides @Singleton
    fun provideKiyaketRepository(dao: KiyaketDao): KiyaketRepository =
        KiyaketRepositoryImpl(dao)

    @Provides @Singleton
    fun provideKombinRepository(kombinDao: KombinDao, kiyaketDao: KiyaketDao): KombinRepository =
        KombinRepositoryImpl(kombinDao, kiyaketDao)

    @Provides @Singleton
    fun provideBarkodRepository(
        barkodOnbellekDao: BarkodOnbellekDao,
        sezonluUrunDao: SezonluUrunDao,
        okHttpClient: OkHttpClient
    ): BarkodRepository = BarkodRepositoryImpl(barkodOnbellekDao, sezonluUrunDao, okHttpClient)

    @Provides @Singleton
    fun provideHavaDurumuApiService(okHttpClient: OkHttpClient) =
        HavaDurumuApiService(okHttpClient)

    @Provides @Singleton
    fun provideHavaDurumuRepository(apiService: HavaDurumuApiService): HavaDurumuRepository =
        HavaDurumuRepositoryImpl(apiService)
}
