package cl.duoc.therionlabs.native

import android.content.Context
import android.content.Intent
import android.net.Uri
import cl.duoc.therionlabs.util.ValidacionesProspecto

object AccionesContacto {
    fun telefono(context: Context, phone: String): String? {
        if (!ValidacionesProspecto.esCelularValido(phone))
            return "El teléfono registrado no tiene un formato válido."
        return open(
            context,
            Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone.filter { it.isDigit() || it == '+' }, null),
            ),
        )
    }

    fun correo(context: Context, email: String): String? {
        if (!ValidacionesProspecto.esCorreoValido(email))
            return "El correo registrado no tiene un formato válido."
        val intent =
            Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email.trim(), null))
                .putExtra(Intent.EXTRA_SUBJECT, "Información de orientación estudiantil")
        return open(context, intent)
    }

    private fun open(context: Context, intent: Intent): String? =
        try {
            context.startActivity(intent)
            null
        } catch (_: android.content.ActivityNotFoundException) {
            "No hay una aplicación compatible para esta acción."
        } catch (_: SecurityException) {
            "No fue posible abrir la aplicación de contacto."
        }
}
