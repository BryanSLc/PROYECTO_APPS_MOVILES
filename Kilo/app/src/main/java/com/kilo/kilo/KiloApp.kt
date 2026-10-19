package com.kilo.kilo

import android.app.Application
import com.kilo.kilo.notificaciones.RevisionMantenimientoWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KiloApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Revisión diaria de mantenimientos próximos (para avisar con la app cerrada)
        RevisionMantenimientoWorker.programar(this)
    }
}