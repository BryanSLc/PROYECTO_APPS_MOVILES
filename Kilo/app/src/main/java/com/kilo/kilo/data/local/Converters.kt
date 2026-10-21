package com.kilo.kilo.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room solo guarda tipos básicos: la fecha se guarda como texto ISO (2026-05-20). */
class Converters {
    @TypeConverter
    fun fechaATexto(fecha: LocalDate?): String? = fecha?.toString()

    @TypeConverter
    fun textoAFecha(texto: String?): LocalDate? = texto?.let(LocalDate::parse)
}
