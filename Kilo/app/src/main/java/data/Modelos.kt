package com.kilo.kilo.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs


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


fun Int.conComas(): String = String.format(Locale.US, "%,d", this)

private val FORMATO_CORTO = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))


fun LocalDate.formatoCorto(): String = format(FORMATO_CORTO).replace(".", "")


val FORMATO_FECHA_SALIDA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")


val FORMATO_FECHA_ENTRADA: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")