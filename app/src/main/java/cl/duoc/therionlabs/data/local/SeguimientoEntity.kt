package cl.duoc.therionlabs.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seguimientos",
    foreignKeys =
        [
            ForeignKey(
                entity = ProspectoEntity::class,
                parentColumns = ["id"],
                childColumns = ["prospectoId"],
                onDelete = ForeignKey.RESTRICT,
            ),
            ForeignKey(
                entity = UsuarioEntity::class,
                parentColumns = ["id"],
                childColumns = ["administrativoId"],
                onDelete = ForeignKey.RESTRICT,
            ),
        ],
    indices = [Index("prospectoId"), Index("administrativoId")],
)
data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prospectoId: Int,
    val administrativoId: String,
    val administrativoNombre: String,
    val estado: String,
    val canal: String,
    val observacion: String,
    val fecha: Long,
    val estadoEnvio: String = "PENDIENTE",
)
