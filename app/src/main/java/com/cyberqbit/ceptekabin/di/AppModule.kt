package com.cyberqbit.ceptekabin.di

import android.content.Context
import com.cyberqbit.ceptekabin.data.local.database.CepteKabinDatabase
import com.cyberqbit.ceptekabin.data.local.database.dao.*
import com.cyberqbit.ceptekabin.data.local.repository.KiyaketRepositoryImpl
import com.cyberqbit.ceptekabin.data.local.repository.KombinRepositoryImpl
import com.cyberqbit.ceptekabin.data.remote.api.HavaDurumuApiService
import com.cyberqbit.ceptekabin.data.repository.BarkodRepositoryImpl
import com.cyberqbit.ceptekabin.data.repository.HavaDurumuRepositoryImpl
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import com.cyberqbit.ceptekabin.domain.repository.BarkodRepository
import com.cyberqbit.ceptekabin.domain.repository.HavaDurumuRepository
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

    @Provides @Singleton
    fun provideKiyaketDao(db: CepteKabinDatabase): KiyaketDao = db.kiyaketDao()

    @Provides @Singleton
    fun provideKombinDao(db: CepteKabinDatabase): KombinDao = db.kombinDao()

    @Provides @Singleton
    fun provideKategoriDao(db: CepteKabinDatabase): KategoriDao = db.kategoriDao()

    @Provides @Singleton
    fun provideBarkodOnbellekDao(db: CepteKabinDatabase): BarkodOnbellekDao = db.barkodOnbellekDao()

    @Provides @Singleton
    fun provideSezonluUrunDao(db: CepteKabinDatabase): SezonluUrunDao = db.sezonluUrunDao()

    @Provides @Singleton
    fun provideKiyaketRepository(kiyaketDao: KiyaketDao): KiyaketRepository =
        KiyaketRepositoryImpl(kiyaketDao)

    // FIX: pass KiyaketDao so KombinRepositoryImpl can resolve Kiyaket relations
    @Provides @Singleton
    fun provideKombinRepository(
        kombinDao: KombinDao,
        kiyaketDao: KiyaketDao
    ): KombinRepository = KombinRepositoryImpl(kombinDao, kiyaketDao)

    @Provides @Singleton
    fun provideBarkodRepository(
        barkodOnbellekDao: BarkodOnbellekDao,
        sezonluUrunDao: SezonluUrunDao,
        okHttpClient: OkHttpClient
    ): BarkodRepository = BarkodRepositoryImpl(barkodOnbellekDao, sezonluUrunDao, okHttpClient)

    @Provides @Singleton
    fun provideHavaDurumuApiService(okHttpClient: OkHttpClient): HavaDurumuApiService =
        HavaDurumuApiService(okHttpClient)

    @Provides @Singleton
    fun provideHavaDurumuRepository(apiService: HavaDurumuApiService): HavaDurumuRepository =
        HavaDurumuRepositoryImpl(apiService)
}
