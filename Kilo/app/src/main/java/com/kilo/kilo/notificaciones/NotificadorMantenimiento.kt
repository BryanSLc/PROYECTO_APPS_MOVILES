package com.kilo.kilo.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kilo.kilo.MainActivity
import com.kilo.kilo.R
import com.kilo.kilo.data.CalculoMantenimiento
import com.kilo.kilo.data.DatosKilo
import com.kilo.kilo.data.EstadoDePieza
import com.kilo.kilo.data.PreferenciasRepository
import com.kilo.kilo.data.Situacion
import com.kilo.kilo.data.Vehiculo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** true si la app puede mostrar notificaciones (permiso concedido y no bloqueadas en el sistema). */
fun Context.tienePermisoNotificaciones(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED &&
            NotificationManagerCompat.from(this).areNotificationsEnabled()

/**
 * Avisa en el teléfono cuando una pieza está "Próxima" (a punto de vencerse).
 *
 *  - Solo avisa si el usuario tiene activadas las notificaciones en Configuración.
 *  - Solo avisa de las piezas en estado PROXIMO (80 % a 99 % de su intervalo).
 *  - Avisa UNA sola vez por pieza y por cambio: se recuerda a quién ya se avisó
 *    y, cuando la pieza deja de estar "Próxima", se olvida para poder avisar en el futuro.
 */
@Singleton
class NotificadorMantenimiento @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencias: PreferenciasRepository
) {
    private val recordatorio = context.getSharedPreferences("kilo_notificaciones", Context.MODE_PRIVATE)

    /** Evita avisos duplicados si dos partes de la app revisan al mismo tiempo. */
    private val candado = Mutex()

    private data class Aviso(val clave: String, val vehiculo: Vehiculo, val estado: EstadoDePieza)

    /** Revisa todos los vehículos y notifica las piezas que pasaron a "Próximo". */
    suspend fun revisar(datos: DatosKilo, hoy: LocalDate = LocalDate.now()) {
        withContext(Dispatchers.Default) {
            candado.withLock {
                if (!preferencias.preferencias.value.notificacionesActivas) return@withLock
                if (!context.tienePermisoNotificaciones()) return@withLock

                val proximas = datos.vehiculos.flatMap { vehiculo ->
                    CalculoMantenimiento.estadosDe(vehiculo, hoy)
                        .filter { it.situacion == Situacion.PROXIMO }
                        .map { estado ->
                            // La clave cambia si se registra un nuevo cambio de esa pieza
                            Aviso("${estado.pieza.id}:${estado.ultimo?.id}", vehiculo, estado)
                        }
                }

                val yaAvisadas = recordatorio.getStringSet(CLAVE_AVISADAS, emptySet()).orEmpty()
                val nuevas = proximas.filter { it.clave !in yaAvisadas }
                if (nuevas.isNotEmpty()) {
                    crearCanal()
                    nuevas.forEach { publicar(it) }
                }

                recordatorio.edit()
                    .putStringSet(CLAVE_AVISADAS, proximas.map { it.clave }.toSet())
                    .apply()
            }
        }
    }

    private fun crearCanal() {
        val canal = NotificationChannel(
            CANAL_ID,
            "Mantenimientos próximos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisa cuando una pieza está a punto de cumplir su intervalo de mantenimiento"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // El permiso ya se comprobó en revisar() con tienePermisoNotificaciones()
    @SuppressLint("MissingPermission")
    private fun publicar(aviso: Aviso) {
        val pieza = aviso.estado.pieza
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val texto = "${aviso.vehiculo.nombre} · ${aviso.estado.detalle()}"

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("${pieza.nombre} está a punto de vencerse")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(pieza.id.hashCode(), notificacion)
    }

    private companion object {
        const val CANAL_ID = "mantenimientos_proximos"
        const val CLAVE_AVISADAS = "avisadas"
    }
}