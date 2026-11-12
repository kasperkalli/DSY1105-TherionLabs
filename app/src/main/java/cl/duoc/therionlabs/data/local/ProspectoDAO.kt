package cl.duoc.therionlabs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspectoDAO {
    @Query(
        "SELECT EXISTS(SELECT 1 FROM prospectos WHERE eventoId = :eventoId AND REPLACE(REPLACE(REPLACE(UPPER(rut), '.', ''), '-', ''), ' ', '') = :rut)"
    )
    suspend fun existeRut(eventoId: String, rut: String): Boolean

    @Query(
        "SELECT EXISTS(SELECT 1 FROM prospectos WHERE eventoId = :eventoId AND LOWER(TRIM(email)) = :email)"
    )
    suspend fun existeCorreo(eventoId: String, email: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarProspecto(prospecto: ProspectoEntity): Long

    @Query("SELECT * FROM prospectos ORDER BY id DESC")
    fun obtenerProspectos(): Flow<List<ProspectoEntity>>

    @Query("SELECT * FROM prospectos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): ProspectoEntity?

    @Query("DELETE FROM prospectos WHERE id = :id") suspend fun borrarProspecto(id: Int)
}
