package cl.duoc.therionlabs.domain

import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

data class BorradorEvento(
    val nombre: String = "",
    val colegio: String = "",
    val fecha: String = "",
    val comuna: String = "",
    val actividad: String = "",
) : Serializable {
    fun tieneCambios() = listOf(nombre, colegio, fecha, comuna, actividad).any { it.isNotBlank() }
}

object ReglasEvento {
    private val eventosDemostracion = setOf("DEM-01", "DEM-02", "DEM-03")

    fun esDemostracion(id: String) = id in eventosDemostracion

    fun validar(d: BorradorEvento): Map<String, String> = buildMap {
        if (d.nombre.trim().length !in 3..100)
            put("nombre", "Ingresa un nombre de 3 a 100 caracteres.")
        if (d.colegio.trim().length !in 3..150)
            put("colegio", "Ingresa la institución (3 a 150 caracteres).")
        if (d.comuna !in CatalogoRegional.comunas)
            put("comuna", "Selecciona una comuna de la Región de Valparaíso.")
        if (d.actividad.trim().length !in 3..300)
            put("actividad", "Describe la actividad (3 a 300 caracteres).")
        try {
            LocalDate.parse(
                d.fecha,
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
            )
        } catch (_: Exception) {
            put("fecha", "Usa una fecha válida: DD/MM/AAAA.")
        }
    }
}

class ProspectoDuplicadoException(message: String) : IllegalArgumentException(message)
