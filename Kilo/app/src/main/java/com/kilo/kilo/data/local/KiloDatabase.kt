package com.kilo.kilo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [VehiculoEntity::class, PiezaEntity::class, RegistroEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KiloDatabase : RoomDatabase() {
    abstract fun kiloDao(): KiloDao
}
