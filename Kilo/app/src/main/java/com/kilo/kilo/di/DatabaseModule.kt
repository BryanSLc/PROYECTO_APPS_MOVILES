package com.kilo.kilo.di

import android.content.Context
import androidx.room.Room
import com.kilo.kilo.data.local.KiloDao
import com.kilo.kilo.data.local.KiloDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt aprende a construir la base de datos Room y su DAO. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKiloDatabase(@ApplicationContext context: Context): KiloDatabase =
        Room.databaseBuilder(context, KiloDatabase::class.java, "kilo.db").build()

    @Provides
    fun provideKiloDao(database: KiloDatabase): KiloDao = database.kiloDao()
}
