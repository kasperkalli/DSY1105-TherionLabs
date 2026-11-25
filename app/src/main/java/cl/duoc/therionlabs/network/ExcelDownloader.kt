
package cl.duoc.therionlabs.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object ExcelDownloader {

    suspend fun descargarYGuardar(
        contexto: Context,
        eventoId: String,
        claveAdministrativa: String,
        destino: Uri
    ) = withContext(Dispatchers.IO) {

        val respuesta = RetrofitClient.excelApi.descargarExcel(
            eventoId = eventoId,
            claveAdministrativa = claveAdministrativa
        )

        if (!respuesta.isSuccessful) {
            val mensaje = when (respuesta.code()) {
                403 -> "Clave administrativa incorrecta."
                404 -> "No se encontró el evento."
                else -> "Error al descargar Excel: HTTP ${respuesta.code()}"
            }
            throw IOException(mensaje)
        }

        val cuerpo = respuesta.body()
            ?: throw IOException("El servidor devolvió un archivo vacío.")

        cuerpo.use { archivo ->
            val salida = contexto.contentResolver.openOutputStream(destino)
                ?: throw IOException("No se pudo abrir el archivo de destino.")

            salida.use { destinoArchivo ->
                archivo.byteStream().copyTo(destinoArchivo)
                destinoArchivo.flush()
            }
        }
    }
}
