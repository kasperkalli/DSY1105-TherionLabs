
package cl.duoc.therionlabs.data.repository

import cl.duoc.therionlabs.data.local.ProspectoDAO
import cl.duoc.therionlabs.data.local.ProspectoEntity
import kotlinx.coroutines.flow.Flow

class ProspectoRepository(
    private val prospectoDao: ProspectoDAO
) {

    val todosLosProspectos: Flow<List<ProspectoEntity>> =
        prospectoDao.obtenerProspectos()

    suspend fun guardarProspecto(
        prospecto: ProspectoEntity
    ): Long {
        return prospectoDao.insertarProspecto(prospecto)
    }

    suspend fun obtenerProspectoPorId(
        id: Int
    ): ProspectoEntity? {
        return prospectoDao.obtenerPorId(id)
    }

    suspend fun borrarProspecto(id: Int) {
        prospectoDao.borrarProspecto(id)
    }
}
