package com.kilo.kilo.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

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