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

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CepteKabinDatabase {
        return CepteKabinDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideKiyaketDao(database: CepteKabinDatabase): KiyaketDao {
        return database.kiyaketDao()
    }

    @Provides
    @Singleton
    fun provideKombinDao(database: CepteKabinDatabase): KombinDao {
        return database.kombinDao()
    }

    @Provides
    @Singleton
    fun provideKategoriDao(database: CepteKabinDatabase): KategoriDao {
        return database.kategoriDao()
    }

    @Provides
    @Singleton
    fun provideBarkodOnbellekDao(database: CepteKabinDatabase): BarkodOnbellekDao {
        return database.barkodOnbellekDao()
    }

    @Provides
    @Singleton
    fun provideSezonluUrunDao(database: CepteKabinDatabase): SezonluUrunDao {
        return database.sezonluUrunDao()
    }

    @Provides
    @Singleton
    fun provideKiyaketRepository(kiyaketDao: KiyaketDao): KiyaketRepository {
        return KiyaketRepositoryImpl(kiyaketDao)
    }

    @Provides
    @Singleton
    fun provideKombinRepository(kombinDao: KombinDao): KombinRepository {
        return KombinRepositoryImpl(kombinDao)
    }

    @Provides
    @Singleton
    fun provideBarkodRepository(
        barkodOnbellekDao: BarkodOnbellekDao,
        sezonluUrunDao: SezonluUrunDao,
        okHttpClient: OkHttpClient
    ): BarkodRepository {
        return BarkodRepositoryImpl(barkodOnbellekDao, sezonluUrunDao, okHttpClient)
    }

    @Provides
    @Singleton
    fun provideHavaDurumuApiService(okHttpClient: OkHttpClient): HavaDurumuApiService {
        return HavaDurumuApiService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideHavaDurumuRepository(apiService: HavaDurumuApiService): HavaDurumuRepository {
        return HavaDurumuRepositoryImpl(apiService)
    }
}
