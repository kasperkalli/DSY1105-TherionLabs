package cl.duoc.therionlabs.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val usuario: String,
    val nombre: String,
    val rol: String,
    val salt: String,
    val passwordHash: String,
)
