package cl.duoc.therionlabs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OperacionDAO {
    @Query("SELECT * FROM usuarios WHERE usuario = :nombre LIMIT 1")
    suspend fun usuario(nombre: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun usuarioPorId(id: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios") suspend fun totalUsuarios(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarEventos(eventos: List<EventoEntity>)

    @Query("SELECT * FROM eventos ORDER BY fecha ASC, nombre ASC")
    fun eventos(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos") suspend fun listaEventos(): List<EventoEntity>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun evento(id: String): EventoEntity?

    @Query(
        "DELETE FROM eventos WHERE id = :id AND NOT EXISTS (SELECT 1 FROM prospectos WHERE eventoId = :id)"
    )
    suspend fun eliminarEventoVacio(id: String): Int

    @Query("SELECT * FROM seguimientos ORDER BY fecha DESC, id DESC")
    fun seguimientos(): Flow<List<SeguimientoEntity>>

    @Insert suspend fun insertarSeguimiento(item: SeguimientoEntity): Long
}
