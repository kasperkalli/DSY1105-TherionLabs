package cl.duoc.therionlabs.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "prospectos",
    indices = [Index(value = ["identificador"], unique = true)]
)
data class ProspectoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Datos personales
    val rut: String,
    val nombre: String,
    val apellidos: String,
    val fechaNacimiento: String,
    val celular: String,
    val email: String,

    // Datos académicos
    val establecimiento: String,
    val comunaResidencia: String,
    val comunaEstablecimiento: String,
    val actividadActual: String,
    val jornada: String,

    // Sede y preferencias
    val region: String,
    val sede: String,
    val nivelInteres: String,
    val carrera1: String,
    val carrera2: String? = null,
    val carrera3: String? = null,

    @ColumnInfo(defaultValue = "''")
    val modalidad: String = "",

    @ColumnInfo(defaultValue = "''")
    val canalContacto: String = "",

    @ColumnInfo(defaultValue = "''")
    val observaciones: String = "",

    @ColumnInfo(defaultValue = "0")
    val autorizaContacto: Boolean = false,

    // Fecha de guardado y estado para la sincronización posterior
    @ColumnInfo(defaultValue = "0")
    val fechaCaptura: Long = System.currentTimeMillis(),

    @ColumnInfo(defaultValue = "0")
    val sincronizado: Boolean = false,

    // Recursos nativos opcionales adjuntos al registro.
    val fotoRuta: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val precisionMetros: Float? = null,
    val fechaUbicacion: Long? = null,
    val direccion: String? = null,

    // Identificador estable para reconocer el mismo registro al enviarlo al servidor.
    @ColumnInfo(defaultValue = "''")
    val identificador: String = UUID.randomUUID().toString()
)
