package cl.duoc.therionlabs.domain

object ReglasSeguimiento {
    val estados =
        linkedMapOf(
            "SIN_CONTACTAR" to "Sin contactar",
            "EN_SEGUIMIENTO" to "En seguimiento",
            "CONTACTADO" to "Contactado",
            "CERRADO" to "Cerrado",
        )
    val canales = listOf("Teléfono", "Correo", "Otro")

    fun validar(estado: String, canal: String, observacion: String): String? =
        when {
            estado !in estados -> "Selecciona un estado de seguimiento válido."
            canal !in canales -> "Selecciona el canal utilizado."
            observacion.trim().length !in 5..500 ->
                "Describe el resultado de la gestión (5 a 500 caracteres)."
            else -> null
        }

    fun etiqueta(estado: String) = estados[estado] ?: "Sin contactar"
}
