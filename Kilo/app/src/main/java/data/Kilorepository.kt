package com.kilo.kilo.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class KiloRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("kilo_datos", Context.MODE_PRIVATE)

    private val _datos = MutableStateFlow(cargar())
    val datos: StateFlow<DatosKilo> = _datos.asStateFlow()

    fun actualizarKm(nuevoKm: Int): String? {
        val actual = _datos.value
        if (nuevoKm < actual.vehiculo.km) {
            return "No puede ser menor al actual (${actual.vehiculo.km.conComas()} km)"
        }
        guardar(actual.copy(vehiculo = actual.vehiculo.copy(km = nuevoKm)))
        return null
    }

    fun registrarMantenimiento(tipo: TipoPieza, fecha: LocalDate, km: Int, nota: String): String? {
        val actual = _datos.value
        if (fecha.isAfter(LocalDate.now())) return "La fecha no puede ser futura"
        if (km < 0) return "El kilometraje no es válido"

        val nuevo = RegistroMantenimiento(
            id = System.currentTimeMillis(),
            tipo = tipo,
            fecha = fecha,
            km = km,
            nota = nota.trim()
        )
        guardar(
            DatosKilo(
                vehiculo = actual.vehiculo.copy(km = max(actual.vehiculo.km, km)),
                registros = actual.registros + nuevo
            )
        )
        return null
    }


    private fun guardar(datos: DatosKilo) {
        _datos.value = datos
        persistir(datos)
    }

    private fun persistir(datos: DatosKilo) {
        val json = JSONObject().apply {
            put("vehiculo", JSONObject().apply {
                put("nombre", datos.vehiculo.nombre)
                put("placa", datos.vehiculo.placa)
                put("carroceria", datos.vehiculo.carroceria)
                put("km", datos.vehiculo.km)
            })
            put("registros", JSONArray().apply {
                datos.registros.forEach { r ->
                    put(JSONObject().apply {
                        put("id", r.id)
                        put("tipo", r.tipo.name)
                        put("fecha", r.fecha.toString())
                        put("km", r.km)
                        put("nota", r.nota)
                    })
                }
            })
        }
        prefs.edit().putString(CLAVE, json.toString()).apply()
    }

    private fun cargar(): DatosKilo {
        val texto = prefs.getString(CLAVE, null)
        if (texto == null) {
            return datosIniciales().also { persistir(it) }
        }
        return try {
            val raiz = JSONObject(texto)
            val v = raiz.getJSONObject("vehiculo")
            val arreglo = raiz.getJSONArray("registros")
            DatosKilo(
                vehiculo = Vehiculo(
                    nombre = v.getString("nombre"),
                    placa = v.getString("placa"),
                    carroceria = v.getString("carroceria"),
                    km = v.getInt("km")
                ),
                registros = (0 until arreglo.length()).map { i ->
                    val o = arreglo.getJSONObject(i)
                    RegistroMantenimiento(
                        id = o.getLong("id"),
                        tipo = TipoPieza.valueOf(o.getString("tipo")),
                        fecha = LocalDate.parse(o.getString("fecha")),
                        km = o.getInt("km"),
                        nota = o.optString("nota", "")
                    )
                }
            )
        } catch (e: Exception) {
            datosIniciales()
        }
    }

    private fun datosIniciales() = DatosKilo(
        vehiculo = Vehiculo("Mazda 3 2019", "PBX-482", "Sedán", 68_450),
        registros = listOf(
            RegistroMantenimiento(1, TipoPieza.ACEITE, LocalDate.of(2026, 5, 20), 60_000, "Sintético 5W-30"),
            RegistroMantenimiento(2, TipoPieza.ACEITE, LocalDate.of(2025, 9, 22), 50_100, "Taller Central"),
            RegistroMantenimiento(3, TipoPieza.ACEITE, LocalDate.of(2025, 2, 14), 40_400, "Semisintético"),
            RegistroMantenimiento(4, TipoPieza.FILTRO, LocalDate.of(2025, 10, 10), 56_000, "Original"),
            RegistroMantenimiento(5, TipoPieza.FRENOS, LocalDate.of(2026, 1, 20), 55_000, "Balatas delanteras"),
            RegistroMantenimiento(6, TipoPieza.LLANTAS, LocalDate.of(2025, 6, 15), 48_000, "Juego nuevo")
            // La batería no tiene registros: aparece como "Sin datos"
        )
    )

    private companion object {
        const val CLAVE = "datos_v1"
    }
}