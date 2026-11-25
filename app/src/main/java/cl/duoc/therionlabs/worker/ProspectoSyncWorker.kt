
package cl.duoc.therionlabs.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.duoc.therionlabs.data.local.AppDatabase
import cl.duoc.therionlabs.network.DispositivoTokenStore
import cl.duoc.therionlabs.network.ParticipacionEventoClient
import cl.duoc.therionlabs.network.RetrofitClient
import cl.duoc.therionlabs.network.toSyncRequest
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.util.UUID
import android.util.Log

class ProspectoSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val database = AppDatabase.getDatabase(appContext)
    private val tokenStore = DispositivoTokenStore(appContext)
    private val participacionClient =
        ParticipacionEventoClient(appContext)


    override suspend fun doWork(): Result {
        Log.i(
            "TherionLabsSync",
            "WORKER INICIADO - Comenzando sincronización"
        )

        val dao = database.prospectoDao()
        val pendientes = dao.obtenerPendientesSincronizacion()

        Log.i(
            "TherionLabsSync",
            "Prospectos pendientes encontrados: ${pendientes.size}"
        )
        try {

            for (prospecto in pendientes) {

                // Comprobar identificadores antes de enviar
                val usuarioId = prospecto.captadorId

                if (usuarioId.isBlank()) {
                    return Result.failure()
                }

                try {
                    UUID.fromString(prospecto.identificador)
                    UUID.fromString(prospecto.eventoId)
                } catch (_: IllegalArgumentException) {
                    return Result.failure()
                }


                val token = tokenStore.obtenerTokenPorCodigo(usuarioId)
                    ?: tokenStore.obtenerToken(usuarioId)

                if (token.isNullOrBlank()) {
                    Log.w(
                        "TherionLabsSync",
                        "No hay token vinculado para $usuarioId"
                    )
                    continue
                }

                val verificacion = RetrofitClient.dispositivoApi
                    .verificarDispositivo(token)

                if (!verificacion.isSuccessful) {
                    val codigo = verificacion.code()
                    if (codigo == 429 || codigo in 500..599) {
                        return Result.retry()
                    }
                    Log.w(
                        "TherionLabsSync",
                        "No se pudo verificar el token de $usuarioId: HTTP $codigo"
                    )
                    continue
                }
                val dispositivo = verificacion.body()
                if (
                    dispositivo?.autorizado != true ||
                    dispositivo.identificadorLocal != usuarioId
                ) {
                    Log.w(
                        "TherionLabsSync",
                        "El token no corresponde al captador $usuarioId"
                    )
                    continue
                }
                try {
                    participacionClient.unirseEvento(
                        usuarioId = usuarioId,
                        eventoId = prospecto.eventoId
                    )

                } catch (cancelacion: CancellationException) {
                    throw cancelacion

                } catch (error: IOException) {
                    // Fallo de red o del servidor.
                    Log.w(
                        "TherionLabsSync",
                        "Error temporal de participación: ${error.message}"
                    )
                    return Result.retry()

                } catch (error: IllegalArgumentException) {
                    // Error de validación o identidad.
                    Log.w(
                        "TherionLabsSync",
                        "Participación rechazada para ${prospecto.identificador}: " +
                                error.message
                    )
                    continue
                }


                // Convertir el registro Room al DTO del backend
                val solicitud = prospecto.toSyncRequest()

                // Enviar mediante Retrofit
                val respuesta = RetrofitClient.prospectoApi
                    .sincronizarProspecto(
                        token = token,
                        prospecto = solicitud
                    )

                if (respuesta.isSuccessful) {

                    val confirmacion = respuesta.body()

                    // Marcar solo si Spring Boot confirmó
                    // exactamente el mismo prospecto
                    if (
                        confirmacion?.estado == "SINCRONIZADO" &&
                        confirmacion.prospectoId == prospecto.identificador
                    ) {
                        dao.marcarComoSincronizado(
                            id = prospecto.id,
                            identificador = prospecto.identificador
                        )
                    } else {
                        return Result.failure()
                    }



                } else if (respuesta.code() == 409) {

                    Log.w(
                        "TherionLabsSync",
                        "Prospecto ${prospecto.identificador} rechazado: HTTP 409"
                    )
                    val cuerpoError = respuesta.errorBody()?.string().orEmpty()

                    val rechazoConfirmado = try {
                        val json = org.json.JSONObject(cuerpoError)
                        json.optString("estado") == "CONFLICTO" &&
                                json.optString("prospectoId") ==
                                prospecto.identificador &&
                                json.optString("mensaje").isNotBlank()

                    } catch (_: Exception) {
                        false
                    }
                    if (rechazoConfirmado) {
                        val actualizados = dao.marcarComoRechazado(
                            id = prospecto.id,
                            identificador = prospecto.identificador
                        )

                        if (actualizados != 1) {
                            Log.e(
                                "TherionLabsSync",
                                "No se pudo actualizar el estado del prospecto " +
                                        "${prospecto.identificador} en Room."
                            )
                            return Result.failure()
                        }
                        Log.i(
                            "TherionLabsSync",
                            "Prospecto rechazado guardado en Room. " +
                                    "Filas actualizadas: $actualizados"
                        )
                    } else {
                        Log.e(
                            "TherionLabsSync",
                            "HTTP 409 sin confirmación válida del rechazo."
                        )
                        return Result.failure()
                    }
                    continue
            } else if (respuesta.code() in 500..599 ||
                    respuesta.code() == 429) {

                    // Error temporal del servidor
                    return Result.retry()

                } else {

                    // Error de validación o autorización
                    return Result.failure()
                }
            }

            return Result.success()

        } catch (e: CancellationException) {
            throw e

        } catch (_: IOException) {
            // Internet interrumpido o servidor inaccesible
            return Result.retry()

        } catch (_: Exception) {
            // No ocultar errores inesperados como sincronizaciones exitosas
            return Result.failure()
        }
    }
}
