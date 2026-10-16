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

// ===================================================================
//  MODELOS
// ===================================================================

/**
 * Una pieza de UN vehículo con su límite de cambio.
 * [intervaloKm] es obligatorio; [intervaloMeses] es opcional (null = solo por km).
 */
data class Pieza(
    val id: Long,
    val nombre: String,
    val intervaloKm: Int,
    val intervaloMeses: Int? = null
) {
    val descripcionIntervalo: String
        get() = if (intervaloMeses != null) {
            "Cada ${intervaloKm.conComas()} km o $intervaloMeses meses, lo que ocurra primero"
        } else {
            "Cada ${intervaloKm.conComas()} km"
        }
}

/** Piezas sugeridas al agregar un vehículo (el usuario puede editarlas, quitarlas o agregar otras). */
data class PlantillaPieza(
    val nombre: String,
    val intervaloKm: Int,
    val intervaloMeses: Int?
)

val PIEZAS_BASE: List<PlantillaPieza> = listOf(
    PlantillaPieza("Aceite del motor", 10_000, 6),
    PlantillaPieza("Filtro de aceite", 10_000, 6),
    PlantillaPieza("Llantas", 40_000, 36),
    PlantillaPieza("Frenos", 30_000, 24),
    PlantillaPieza("Batería", 60_000, 36)
)

/** Pieza tal como la define el usuario en el formulario "Agregar vehículo". */
data class NuevaPieza(
    val nombre: String,
    val intervaloKm: Int,
    val intervaloMeses: Int?,
    /** Km del último cambio (opcional). Si es null, la pieza queda "Sin datos". */
    val ultimoKm: Int?
)

data class RegistroMantenimiento(
    val id: Long,
    val piezaId: Long,
    val fecha: LocalDate,
    val km: Int,
    val nota: String
)

data class Vehiculo(
    val id: Long,
    val nombre: String,
    val placa: String,
    val carroceria: String,
    val km: Int,
    val piezas: List<Pieza>,
    val registros: List<RegistroMantenimiento>
)

/** Todos los vehículos y cuál es el activo. Siempre hay al menos un vehículo. */
data class DatosKilo(
    val vehiculos: List<Vehiculo>,
    val vehiculoActivoId: Long
) {
    /** El vehículo que se está viendo en Inicio, Detalle, etc. */
    val vehiculo: Vehiculo
        get() = vehiculos.firstOrNull { it.id == vehiculoActivoId } ?: vehiculos.first()

    val registros: List<RegistroMantenimiento>
        get() = vehiculo.registros
}

enum class Situacion(val etiqueta: String) {
    VENCIDO("Vencido"),
    PROXIMO("Próximo"),
    AL_DIA("Al día"),
    SIN_DATOS("Sin datos")
}

data class EstadoDePieza(
    val pieza: Pieza,
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
                if (limitaPorTiempo && fechaLimite != null) "Venció el ${fechaLimite.formatoCorto()}"
                else if ((kmRestantes ?: 0) < 0) "${abs(kmRestantes ?: 0).conComas()} km de retraso"
                else "Llegó a su límite de km"
            else ->
                if (limitaPorTiempo && fechaLimite != null) "Antes del ${fechaLimite.formatoCorto()}"
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
//  comparando el kilometraje del vehículo contra el límite de cada pieza.
// ===================================================================

object CalculoMantenimiento {

    /** Desde este avance del intervalo (80%) la pieza pasa a "Próximo". */
    const val UMBRAL_PROXIMO = 0.80f

    fun estadoDe(
        pieza: Pieza,
        vehiculo: Vehiculo,
        hoy: LocalDate = LocalDate.now()
    ): EstadoDePieza {
        // El último mantenimiento es el más reciente (por fecha; si empatan, el último registrado)
        val ultimo = vehiculo.registros
            .filter { it.piezaId == pieza.id }
            .maxWithOrNull(compareBy<RegistroMantenimiento>({ it.fecha }, { it.id }))
            ?: return EstadoDePieza(pieza, Situacion.SIN_DATOS, null, null, null, null, 0f, false)

        val intervaloKm = pieza.intervaloKm.coerceAtLeast(1)
        val kmMeta = ultimo.km + intervaloKm
        val progresoKm = (vehiculo.km - ultimo.km).coerceAtLeast(0).toFloat() / intervaloKm

        // El tiempo solo cuenta si la pieza tiene límite en meses
        val fechaLimite = pieza.intervaloMeses?.let { ultimo.fecha.plusMonths(it.toLong()) }
        val progresoTiempo = if (fechaLimite != null) {
            val diasDelIntervalo = ChronoUnit.DAYS.between(ultimo.fecha, fechaLimite).coerceAtLeast(1)
            val diasPasados = ChronoUnit.DAYS.between(ultimo.fecha, hoy).coerceAtLeast(0)
            diasPasados.toFloat() / diasDelIntervalo
        } else {
            0f
        }

        val progreso = max(progresoKm, progresoTiempo)
        val situacion = when {
            progreso >= 1f -> Situacion.VENCIDO
            progreso >= UMBRAL_PROXIMO -> Situacion.PROXIMO
            else -> Situacion.AL_DIA
        }

        return EstadoDePieza(
            pieza = pieza,
            situacion = situacion,
            ultimo = ultimo,
            kmMeta = kmMeta,
            fechaLimite = fechaLimite,
            kmRestantes = kmMeta - vehiculo.km,
            progreso = progreso,
            limitaPorTiempo = fechaLimite != null && progresoTiempo > progresoKm
        )
    }

    fun estadosDe(vehiculo: Vehiculo, hoy: LocalDate = LocalDate.now()): List<EstadoDePieza> =
        vehiculo.piezas.map { estadoDe(it, vehiculo, hoy) }

    /** Estados del vehículo activo. */
    fun estadosDe(datos: DatosKilo, hoy: LocalDate = LocalDate.now()): List<EstadoDePieza> =
        estadosDe(datos.vehiculo, hoy)
}


// ===================================================================
//  REPOSITORIO: guarda los datos en el teléfono y avisa cuando cambian
// ===================================================================


@Singleton
class KiloRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("kilo_datos", Context.MODE_PRIVATE)

    // Debe declararse antes de _datos: cargar() puede usarlo
    private var ultimoId = 0L

    private val _datos = MutableStateFlow(cargar())
    val datos: StateFlow<DatosKilo> = _datos.asStateFlow()

    private fun nuevoId(): Long {
        ultimoId = max(System.currentTimeMillis(), ultimoId + 1)
        return ultimoId
    }

    // ------------------------------------------------------------ vehículos

    /** Cambia el kilometraje del vehículo activo. Devuelve un mensaje de error o null si salió bien. */
    fun actualizarKm(nuevoKm: Int): String? {
        val actual = _datos.value
        val v = actual.vehiculo
        if (nuevoKm < v.km) {
            return "No puede ser menor al actual (${v.km.conComas()} km)"
        }
        guardar(actual.conVehiculo(v.copy(km = nuevoKm)))
        return null
    }

    /** Agrega un vehículo con sus piezas y lo deja como vehículo activo. */
    fun agregarVehiculo(
        nombre: String,
        placa: String,
        carroceria: String,
        km: Int,
        piezas: List<NuevaPieza>
    ): String? {
        val actual = _datos.value
        if (nombre.isBlank()) return "Escribe la marca y el modelo"
        if (placa.isBlank()) return "Escribe la placa"
        if (km < 0) return "El kilometraje no es válido"
        if (actual.vehiculos.any { it.placa.equals(placa.trim(), ignoreCase = true) }) {
            return "Ya existe un vehículo con esa placa"
        }
        if (piezas.isEmpty()) return "Agrega al menos una pieza"

        val nombres = mutableSetOf<String>()
        for (p in piezas) {
            val n = p.nombre.trim()
            if (n.isEmpty()) return "Todas las piezas necesitan un nombre"
            if (!nombres.add(n.lowercase())) return "La pieza \"$n\" está repetida"
            if (p.intervaloKm <= 0) return "\"$n\": el límite de km debe ser mayor a 0"
            if (p.intervaloMeses != null && p.intervaloMeses <= 0) {
                return "\"$n\": los meses deben ser mayores a 0"
            }
            if (p.ultimoKm != null && p.ultimoKm > km) {
                return "\"$n\": el último cambio no puede ser mayor al kilometraje actual"
            }
        }

        val hoy = LocalDate.now()
        val listaPiezas = mutableListOf<Pieza>()
        val registros = mutableListOf<RegistroMantenimiento>()
        piezas.forEach { p ->
            val pieza = Pieza(nuevoId(), p.nombre.trim(), p.intervaloKm, p.intervaloMeses)
            listaPiezas += pieza
            if (p.ultimoKm != null) {
                registros += RegistroMantenimiento(nuevoId(), pieza.id, hoy, p.ultimoKm, "Registro inicial")
            }
        }

        val vehiculo = Vehiculo(
            id = nuevoId(),
            nombre = nombre.trim(),
            placa = placa.trim().uppercase(),
            carroceria = carroceria,
            km = km,
            piezas = listaPiezas,
            registros = registros
        )
        guardar(DatosKilo(actual.vehiculos + vehiculo, vehiculo.id))
        return null
    }

    fun seleccionarVehiculo(id: Long) {
        val actual = _datos.value
        if (actual.vehiculos.none { it.id == id } || actual.vehiculoActivoId == id) return
        guardar(actual.copy(vehiculoActivoId = id))
    }

    /** No permite borrar el último vehículo. */
    fun eliminarVehiculo(id: Long): String? {
        val actual = _datos.value
        if (actual.vehiculos.none { it.id == id }) return null
        if (actual.vehiculos.size <= 1) return "Debe quedar al menos un vehículo"
        val restantes = actual.vehiculos.filterNot { it.id == id }
        val activo = if (actual.vehiculoActivoId == id) restantes.first().id else actual.vehiculoActivoId
        guardar(DatosKilo(restantes, activo))
        return null
    }

    // ------------------------------------------------------------ mantenimientos

    /**
     * Registra que una pieza del vehículo activo se cambió en [km].
     * NO modifica el kilometraje del vehículo: eso solo se hace con [actualizarKm].
     */
    fun registrarMantenimiento(piezaId: Long, fecha: LocalDate, km: Int, nota: String): String? {
        val actual = _datos.value
        val v = actual.vehiculo
        if (v.piezas.none { it.id == piezaId }) return "No se encontró la pieza"
        if (fecha.isAfter(LocalDate.now())) return "La fecha no puede ser futura"
        if (km < 0) return "El kilometraje no es válido"
        if (km > v.km) {
            return "No puede ser mayor al kilometraje actual (${v.km.conComas()} km). " +
                "Primero actualiza el kilometraje."
        }

        val nuevo = RegistroMantenimiento(
            id = nuevoId(),
            piezaId = piezaId,
            fecha = fecha,
            km = km,
            nota = nota.trim()
        )
        guardar(actual.conVehiculo(v.copy(registros = v.registros + nuevo)))
        return null
    }

    private fun DatosKilo.conVehiculo(nuevo: Vehiculo): DatosKilo =
        copy(vehiculos = vehiculos.map { if (it.id == nuevo.id) nuevo else it })

    // ------------------------------------------------------------ guardar / cargar

    private fun guardar(datos: DatosKilo) {
        _datos.value = datos
        persistir(datos)
    }

    private fun persistir(datos: DatosKilo) {
        val json = JSONObject().apply {
            put("activo", datos.vehiculoActivoId)
            put("vehiculos", JSONArray().apply {
                datos.vehiculos.forEach { v ->
                    put(JSONObject().apply {
                        put("id", v.id)
                        put("nombre", v.nombre)
                        put("placa", v.placa)
                        put("carroceria", v.carroceria)
                        put("km", v.km)
                        put("piezas", JSONArray().apply {
                            v.piezas.forEach { p ->
                                put(JSONObject().apply {
                                    put("id", p.id)
                                    put("nombre", p.nombre)
                                    put("intervaloKm", p.intervaloKm)
                                    if (p.intervaloMeses != null) put("intervaloMeses", p.intervaloMeses)
                                })
                            }
                        })
                        put("registros", JSONArray().apply {
                            v.registros.forEach { r ->
                                put(JSONObject().apply {
                                    put("id", r.id)
                                    put("piezaId", r.piezaId)
                                    put("fecha", r.fecha.toString())
                                    put("km", r.km)
                                    put("nota", r.nota)
                                })
                            }
                        })
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
            val arregloV = raiz.getJSONArray("vehiculos")
            val vehiculos = (0 until arregloV.length()).map { i ->
                val v = arregloV.getJSONObject(i)
                val arregloP = v.getJSONArray("piezas")
                val arregloR = v.getJSONArray("registros")
                Vehiculo(
                    id = v.getLong("id"),
                    nombre = v.getString("nombre"),
                    placa = v.getString("placa"),
                    carroceria = v.getString("carroceria"),
                    km = v.getInt("km"),
                    piezas = (0 until arregloP.length()).map { j ->
                        val p = arregloP.getJSONObject(j)
                        Pieza(
                            id = p.getLong("id"),
                            nombre = p.getString("nombre"),
                            intervaloKm = p.getInt("intervaloKm"),
                            intervaloMeses = if (p.has("intervaloMeses")) p.getInt("intervaloMeses") else null
                        )
                    },
                    registros = (0 until arregloR.length()).map { j ->
                        val r = arregloR.getJSONObject(j)
                        RegistroMantenimiento(
                            id = r.getLong("id"),
                            piezaId = r.getLong("piezaId"),
                            fecha = LocalDate.parse(r.getString("fecha")),
                            km = r.getInt("km"),
                            nota = r.optString("nota", "")
                        )
                    }
                )
            }
            if (vehiculos.isEmpty() || vehiculos.any { it.piezas.isEmpty() }) {
                datosIniciales()
            } else {
                DatosKilo(vehiculos, raiz.getLong("activo"))
            }
        } catch (e: Exception) {
            datosIniciales()
        }
    }

    private fun datosIniciales(): DatosKilo {
        val piezas = PIEZAS_BASE.mapIndexed { i, p ->
            Pieza((i + 1).toLong(), p.nombre, p.intervaloKm, p.intervaloMeses)
        }
        // ids de piezas: 1 aceite, 2 filtro, 3 llantas, 4 frenos, 5 batería
        val registros = listOf(
            RegistroMantenimiento(1, 1, LocalDate.of(2026, 5, 20), 60_000, "Sintético 5W-30"),
            RegistroMantenimiento(2, 1, LocalDate.of(2025, 9, 22), 50_100, "Taller Central"),
            RegistroMantenimiento(3, 1, LocalDate.of(2025, 2, 14), 40_400, "Semisintético"),
            RegistroMantenimiento(4, 2, LocalDate.of(2025, 10, 10), 56_000, "Original"),
            RegistroMantenimiento(5, 4, LocalDate.of(2026, 1, 20), 55_000, "Balatas delanteras"),
            RegistroMantenimiento(6, 3, LocalDate.of(2025, 6, 15), 48_000, "Juego nuevo")
        )
        val mazda = Vehiculo(1, "Mazda 3 2019", "PBX-482", "Sedán", 68_450, piezas, registros)
        return DatosKilo(listOf(mazda), mazda.id)
    }

    private companion object {
        const val CLAVE = "datos_v2"
    }
}
