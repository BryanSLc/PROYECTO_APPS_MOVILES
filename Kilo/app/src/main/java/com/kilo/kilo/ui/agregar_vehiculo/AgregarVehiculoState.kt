package com.kilo.kilo.ui.agregar_vehiculo

import com.kilo.kilo.data.PIEZAS_BASE

/** Una pieza del formulario: su nombre, cada cuánto se cambia y cuándo se cambió por última vez. */
data class PiezaForm(
    /** Id solo para el formulario (clave de la lista); no es el id que se guarda. */
    val id: Long,
    val nombre: String = "",
    val limiteKm: String = "",
    val meses: String = "",
    val ultimoKm: String = ""
)

/** Piezas sugeridas; el usuario puede editarlas, quitarlas o agregar otras. */
fun piezasFormBase(): List<PiezaForm> = PIEZAS_BASE.mapIndexed { i, p ->
    PiezaForm(
        id = (i + 1).toLong(),
        nombre = p.nombre,
        limiteKm = p.intervaloKm.toString(),
        meses = p.intervaloMeses?.toString().orEmpty()
    )
}

data class AgregarVehiculoState(
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val placa: String = "",
    val kilometraje: String = "",
    val tipos: List<String> = listOf("Sedán", "SUV", "Pickup", "Moto"),
    val tipoSeleccionado: String = "Sedán",
    val piezas: List<PiezaForm> = piezasFormBase(),
    val error: String? = null
) {
    val puedeGuardar: Boolean
        get() = marca.isNotBlank() &&
                modelo.isNotBlank() &&
                placa.isNotBlank() &&
                kilometraje.isNotBlank()
}
