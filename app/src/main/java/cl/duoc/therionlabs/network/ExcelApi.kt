package cl.duoc.therionlabs.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ExcelApi {

    @Streaming
    @GET("api/admin/prospectos/{eventoId}/excel")
    suspend fun descargarExcel(
        @Path("eventoId") eventoId: String,
        @Header("X-Admin-Key") claveAdministrativa: String
    ): Response<ResponseBody>

}
