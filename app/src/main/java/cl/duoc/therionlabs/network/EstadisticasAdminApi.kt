package cl.duoc.therionlabs.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface EstadisticasAdminApi {

    @GET("api/admin/estadisticas/prospectos-por-evento")
    suspend fun obtenerProspectosPorEvento(
        @Header("X-Admin-Key") clave: String
    ): Response<Map<String, Long>>
}
