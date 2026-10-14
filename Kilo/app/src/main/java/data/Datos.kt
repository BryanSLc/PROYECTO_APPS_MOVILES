package com.kilo.kilo.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class TipoPieza(
    val nombre: String,
    val intervaloKm: Int,
    val intervaloMeses: Int
) {
    ACEITE("Aceite del motor", 10_000, 6),
    FILTRO("Filtro de aceite", 10_000, 6),
    LLANTAS("Llantas", 40_000, 36),
    FRENOS("Frenos", 30_000, 24),
    BATERIA("Batería", 60_000, 36);

    val descripcionIntervalo: String
        get() = "Cada ${intervaloKm.conComas()} km o $intervaloMeses meses, lo que ocurra primero"

    companion object {
        fun desdeNombre(nombre: String): TipoPieza? = values().firstOrNull { it.nombre == nombre }
        fun desdeClave(clave: String?): TipoPieza? = values().firstOrNull { it.name == clave }
    }
}

data class Vehiculo(
    val nombre: String,
    val placa: String,
    val carroceria: String,
    val km: Int
)

data class RegistroMantenimiento(
    val id: Long,
    val tipo: TipoPieza,
    val fecha: LocalDate,
    val km: Int,
    val nota: String
)

data class DatosKilo(
    val vehiculo: Vehiculo,
    val registros: List<RegistroMantenimiento>
)

enum class Situacion(val etiqueta: String) {
    VENCIDO("Vencido"),
    PROXIMO("Próximo"),
    AL_DIA("Al día"),
    SIN_DATOS("Sin datos")
}

data class EstadoDePieza(
    val tipo: TipoPieza,
    val situacion: Situacion,
    val ultimo: RegistroMantenimiento?,
    val kmMeta: Int?,
    val fechaLimite: LocalDate?,
    val kmRestantes: Int?,
    val progreso: Float,
    val limitaPorTiempo: Boolean
) {
    val detalle: String
        get() = when (situacion) {
            Situacion.SIN_DATOS -> "Sin registro"
            Situacion.VENCIDO ->
                if ((kmRestantes ?: 0) < 0) "${abs(kmRestantes ?: 0).conComas()} km de retraso"
                else "Venció el ${fechaLimite?.formatoCorto().orEmpty()}"
            else ->
                if (limitaPorTiempo) "Antes del ${fechaLimite?.formatoCorto().orEmpty()}"
                else "Faltan ${(kmRestantes ?: 0).conComas()} km"
        }
}

// ---------------- Formatos de texto

fun Int.conComas(): String = String.format(Locale.US, "%,d", this)

private val FORMATO_CORTO = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))

fun LocalDate.formatoCorto(): String = format(FORMATO_CORTO).replace(".", "")

val FORMATO_FECHA_SALIDA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

val FORMATO_FECHA_ENTRADA: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")


// ===================================================================
//  CÁLCULO: decide si cada pieza está vencida, próxima, al día o sin datos
// ===================================================================


object CalculoMantenimiento {

    const val UMBRAL_PROXIMO = 0.80f

    fun estadoDe(
        tipo: TipoPieza,
        datos: DatosKilo,
        hoy: LocalDate = LocalDate.now()
    ): EstadoDePieza {
        val ultimo = datos.registros
            .filter { it.tipo == tipo }
            .maxWithOrNull(compareBy<RegistroMantenimiento>({ it.km }, { it.fecha }))
            ?: return EstadoDePieza(tipo, Situacion.SIN_DATOS, null, null, null, null, 0f, false)

        val kmActual = datos.vehiculo.km
        val kmMeta = ultimo.km + tipo.intervaloKm
        val fechaLimite = ultimo.fecha.plusMonths(tipo.intervaloMeses.toLong())

        val progresoKm = (kmActual - ultimo.km).coerceAtLeast(0).toFloat() / tipo.intervaloKm

        val diasDelIntervalo = ChronoUnit.DAYS.between(ultimo.fecha, fechaLimite).coerceAtLeast(1)
        val diasPasados = ChronoUnit.DAYS.between(ultimo.fecha, hoy).coerceAtLeast(0)
        val progresoTiempo = diasPasados.toFloat() / diasDelIntervalo

        val progreso = max(progresoKm, progresoTiempo)
        val situacion = when {
            progreso >= 1f -> Situacion.VENCIDO
            progreso >= UMBRAL_PROXIMO -> Situacion.PROXIMO
            else -> Situacion.AL_DIA
        }

        return EstadoDePieza(
            tipo = tipo,
            situacion = situacion,
            ultimo = ultimo,
            kmMeta = kmMeta,
            fechaLimite = fechaLimite,
            kmRestantes = kmMeta - kmActual,
            progreso = progreso,
            limitaPorTiempo = progresoTiempo > progresoKm
        )
    }

    fun estadosDe(datos: DatosKilo, hoy: LocalDate = LocalDate.now()): List<EstadoDePieza> =
        TipoPieza.values().map { estadoDe(it, datos, hoy) }
}


// ===================================================================
//  REPOSITORIO: guarda los datos en el teléfono y avisa cuando cambian
// ===================================================================


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

    // ------------------------------------------------------------ guardar / cargar

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
        )
    )

    private companion object {
        const val CLAVE = "datos_v1"
    }
}