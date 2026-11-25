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


    @Query("SELECT * FROM prospectos ORDER BY id DESC")
    suspend fun obtenerParaExportacion(): List<ProspectoEntity>


    // Obtener prospectos pendientes de sincronización
    @Query("""
        SELECT * FROM prospectos
        WHERE sincronizado = 0
        AND eventoId != ''
        ORDER BY id ASC
    """)
    suspend fun obtenerPendientesSincronizacion(): List<ProspectoEntity>

    // Marcar como sincronizado después de recibir confirmación
    @Query("""
        UPDATE prospectos
        SET sincronizado = 1
        WHERE id = :id
        AND identificador = :identificador
    """)

    suspend fun marcarComoSincronizado(
        id: Int,
        identificador: String
    ): Int

    @Query("""
    UPDATE prospectos
    SET sincronizado = 2
    WHERE id = :id
    AND identificador = :identificador
    AND sincronizado = 0
""")
    suspend fun marcarComoRechazado(
        id: Int,
        identificador: String
    ): Int


    @Query("""
    SELECT COUNT(*)
    FROM prospectos
    WHERE sincronizado = 0
    AND captadorId = :usuarioId
""")
    suspend fun contarPendientes(usuarioId: String): Int

    @Query("""
    SELECT COUNT(*)
    FROM prospectos
    WHERE sincronizado = 1
    AND captadorId = :usuarioId
""")
    suspend fun contarSincronizados(usuarioId: String): Int

    @Query("""
    SELECT COUNT(*)
    FROM prospectos
    WHERE sincronizado = 2
    AND captadorId = :usuarioId
""")
    suspend fun contarRechazados(usuarioId: String): Int


}
