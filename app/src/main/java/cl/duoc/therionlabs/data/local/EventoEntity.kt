package cl.duoc.therionlabs.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val colegio: String,
    val fecha: String,
    val actividad: String,
    val activo: Boolean = true,
    @ColumnInfo(defaultValue = "''") val comuna: String = "",
)
