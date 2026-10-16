package com.kilo.kilo.di

import com.kilo.kilo.data.KiloRepository
import com.kilo.kilo.data.OfflineKiloRepository
import com.kilo.kilo.data.OfflinePreferenciasRepository
import com.kilo.kilo.data.PreferenciasRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Aquí Hilt aprende "cuando alguien pida X, entrégale Y".
 * Con Hilt no se crea nada a mano: este módulo reemplaza al contenedor manual del ejemplo.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindKiloRepository(impl: OfflineKiloRepository): KiloRepository

    @Binds
    abstract fun bindPreferenciasRepository(impl: OfflinePreferenciasRepository): PreferenciasRepository
}