package com.kilo.kilo.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

// ===================================================================
//  REPOSITORIO (implementación): guarda los datos en el teléfono
// ===================================================================


@Singleton
class OfflineKiloRepository @Inject constructor(
    @ApplicationContext context: Context
) : KiloRepository {
    private val prefs = context.getSharedPreferences("kilo_datos", Context.MODE_PRIVATE)

    // Debe declararse antes de _datos: cargar() puede usarlo
    private var ultimoId = 0L

    private val _datos = MutableStateFlow(cargar())
    override val datos: StateFlow<DatosKilo> = _datos.asStateFlow()

    private fun nuevoId(): Long {
        ultimoId = max(System.currentTimeMillis(), ultimoId + 1)
        return ultimoId
    }

    // ------------------------------------------------------------ vehículos

    /** Cambia el kilometraje del vehículo activo. Devuelve un mensaje de error o null si salió bien. */
    override fun actualizarKm(nuevoKm: Int): String? {
        val actual = _datos.value
        val v = actual.vehiculo
        if (nuevoKm < v.km) {
            return "No puede ser menor al actual (${v.km.conComas()} km)"
        }
        guardar(actual.conVehiculo(v.copy(km = nuevoKm)))
        return null
    }

    /** Agrega un vehículo con sus piezas y lo deja como vehículo activo. */
    override fun agregarVehiculo(
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

    override fun seleccionarVehiculo(id: Long) {
        val actual = _datos.value
        if (actual.vehiculos.none { it.id == id } || actual.vehiculoActivoId == id) return
        guardar(actual.copy(vehiculoActivoId = id))
    }

    /** No permite borrar el último vehículo. */
    override fun eliminarVehiculo(id: Long): String? {
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
    override fun registrarMantenimiento(piezaId: Long, fecha: LocalDate, km: Int, nota: String): String? {
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

    /** Borra un registro del historial del vehículo activo. */
    override fun eliminarRegistro(registroId: Long): String? {
        val actual = _datos.value
        val v = actual.vehiculo
        if (v.registros.none { it.id == registroId }) return "No se encontró el registro"
        guardar(actual.conVehiculo(v.copy(registros = v.registros.filterNot { it.id == registroId })))
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