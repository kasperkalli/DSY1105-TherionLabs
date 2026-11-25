package cl.duoc.therionlabs.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.GET

data class CrearCaptadorRequest(
    val identificadorLocal: String,
    val nombre: String
)

data class CaptadorRegistradoResponse(
    val id: String,
    val identificadorLocal: String,
    val nombre: String,
    val activo: Boolean
)

data class CredencialCaptadorResponse(
    val dispositivoId: String,
    val credencial: String
)

interface CaptadorAdminApi {

    @POST("api/admin/captadores/{captadorId}/renovar-credencial")
    suspend fun renovarCredencial(
        @Header("X-Admin-Key") clave: String,
        @Path("captadorId") captadorId: String
    ): Response<CredencialCaptadorResponse>


    @GET("api/admin/captadores")
    suspend fun listarCaptadores(
        @Header("X-Admin-Key") clave: String
    ): Response<List<CaptadorRegistradoResponse>>


    @POST("api/admin/captadores")
    suspend fun registrarCaptador(
        @Header("X-Admin-Key") clave: String,
        @Body datos: CrearCaptadorRequest
    ): Response<CaptadorRegistradoResponse>

    @POST("api/admin/captadores/{captadorId}/dispositivo")
    suspend fun generarToken(
        @Header("X-Admin-Key") clave: String,
        @Path("captadorId") captadorId: String
    ): Response<CredencialCaptadorResponse>
}
