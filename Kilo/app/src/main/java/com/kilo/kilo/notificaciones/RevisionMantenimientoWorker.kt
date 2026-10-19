package com.kilo.kilo.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kilo.kilo.data.KiloRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/** Puerta de entrada de Hilt: el Worker no lo crea Hilt, así que pide aquí sus dependencias. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RevisionEntryPoint {
    fun repositorio(): KiloRepository
    fun notificador(): NotificadorMantenimiento
}

/**
 * Revisa los mantenimientos una vez al día, aunque la app esté cerrada: así también
 * avisa cuando una pieza pasa a "Próximo" solo porque se acerca su fecha límite.
 */
class RevisionMantenimientoWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val dependencias = EntryPointAccessors.fromApplication(
            applicationContext,
            RevisionEntryPoint::class.java
        )
        val datos = dependencias.repositorio().datos.first()
        dependencias.notificador().revisar(datos)
        return Result.success()
    }

    companion object {
        private const val NOMBRE_TRABAJO = "revision_mantenimiento_diaria"

        /** Programa la revisión diaria (si ya estaba programada, no la duplica). */
        fun programar(contexto: Context) {
            val peticion = PeriodicWorkRequestBuilder<RevisionMantenimientoWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                peticion
            )
        }
    }
}