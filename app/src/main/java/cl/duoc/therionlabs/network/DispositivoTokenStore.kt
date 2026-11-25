package cl.duoc.therionlabs.network

import android.content.Context

class DispositivoTokenStore(context: Context) {

    private val preferencias = context.applicationContext
        .getSharedPreferences(
            "therionlabs_dispositivo",
            Context.MODE_PRIVATE
        )

    fun guardarToken(usuarioId: String, token: String) {
        require(usuarioId.isNotBlank())
        require(token.isNotBlank())

        preferencias.edit()
            .putString("device_token_$usuarioId", token.trim())
            .apply()
    }

    fun obtenerToken(usuarioId: String): String? {
        return preferencias
            .getString("device_token_$usuarioId", null)
            ?.takeIf { it.isNotBlank() }
    }

    fun eliminarToken(usuarioId: String) {
        val codigoActual = obtenerCodigoCaptador(usuarioId)
        val editor = preferencias.edit()
            .remove("device_token_$usuarioId")
            .remove("captador_id_$usuarioId")
            .remove("captador_codigo_$usuarioId")
        if (codigoActual != null) {
            editor.remove("device_token_por_codigo_$codigoActual")
        }
        editor.apply()
    }


    // guardar token e identidad

    fun guardarVinculacion(
        usuarioId: String,
        token: String,
        captadorId: String,
        codigoCaptador: String
    ) {
        require(usuarioId.isNotBlank())
        require(token.isNotBlank())
        require(captadorId.isNotBlank())
        require(codigoCaptador.isNotBlank())
        // Consultar la identidad anterior
        val codigoAnterior = obtenerCodigoCaptador(usuarioId)
        val editor = preferencias.edit()

        // Eliminar la credencial anterior
        if (
            codigoAnterior != null &&
            codigoAnterior != codigoCaptador
        ) {
            editor.remove("device_token_por_codigo_$codigoAnterior")
        }
        // Guardar la nueva vinculación
        editor
            .putString("device_token_$usuarioId", token.trim())
            .putString("captador_id_$usuarioId", captadorId)
            .putString("captador_codigo_$usuarioId", codigoCaptador)
            .putString(
                "device_token_por_codigo_$codigoCaptador",
                token.trim()
            )
            .apply()
    }


    fun obtenerCaptadorId(usuarioId: String): String? {
        return preferencias
            .getString("captador_id_$usuarioId", null)
            ?.takeIf { it.isNotBlank() }
    }

    fun obtenerCodigoCaptador(usuarioId: String): String? {
        return preferencias
            .getString("captador_codigo_$usuarioId", null)
            ?.takeIf { it.isNotBlank() }
    }

    fun obtenerTokenPorCodigo(codigoCaptador: String): String? {
        return preferencias
            .getString("device_token_por_codigo_$codigoCaptador", null)
            ?.takeIf { it.isNotBlank() }
    }

}
