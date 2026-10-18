package com.kilo.kilo.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

// ===================================================================
//  ENTIDADES DE ROOM (tablas). Son detalles internos de la capa de datos:
//  la UI y los ViewModels nunca las ven, solo ven los modelos de dominio.
// ===================================================================

@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val placa: String,
    val carroceria: String,
    val km: Int,
    /** Solo un vehículo tiene activo = true: el que se muestra en Inicio. */
    val activo: Boolean
)

@Entity(
    tableName = "piezas",
    foreignKeys = [
        ForeignKey(
            entity = VehiculoEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehiculoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehiculoId")]
)
data class PiezaEntity(
    @PrimaryKey val id: Long,
    val vehiculoId: Long,
    val nombre: String,
    val intervaloKm: Int,
    val intervaloMeses: Int?
)

@Entity(
    tableName = "registros",
    foreignKeys = [
        ForeignKey(
            entity = VehiculoEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehiculoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PiezaEntity::class,
            parentColumns = ["id"],
            childColumns = ["piezaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehiculoId"), Index("piezaId")]
)
data class RegistroEntity(
    @PrimaryKey val id: Long,
    val vehiculoId: Long,
    val piezaId: Long,
    val fecha: LocalDate,
    val km: Int,
    val nota: String
)

/** Un vehículo junto con sus piezas y registros (Room arma la relación). */
data class VehiculoConDetalle(
    @Embedded val vehiculo: VehiculoEntity,
    @Relation(parentColumn = "id", entityColumn = "vehiculoId")
    val piezas: List<PiezaEntity>,
    @Relation(parentColumn = "id", entityColumn = "vehiculoId")
    val registros: List<RegistroEntity>
)
