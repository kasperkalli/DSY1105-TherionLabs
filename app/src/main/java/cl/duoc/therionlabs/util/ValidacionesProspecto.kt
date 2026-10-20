package cl.duoc.therionlabs.util

import cl.duoc.therionlabs.data.model.ErroresFormularioProspectos
import cl.duoc.therionlabs.data.model.FormularioProspectos
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

object ValidacionesProspecto {

    const val MAXIMO_CARRERAS = 3

    private val formatoFecha = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    // Valida el formato del correo; no comprueba que exista la cuenta.
    private val formatoCorreo = Regex(
        """[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+"""
    )

    fun validar(
        datos: FormularioProspectos,
        hoy: LocalDate = LocalDate.now()
    ): ErroresFormularioProspectos {
        return ErroresFormularioProspectos(
            rut = when {
                datos.rut.isBlank() -> "Este campo es obligatorio"
                !esRutValido(datos.rut) -> "Revisa el RUT y su dígito verificador"
                else -> null
            },
            nombre = obligatorio(datos.nombre),
            apellidos = obligatorio(datos.apellidos),
            fechaNacimiento = when {
                datos.fechaNacimiento.isBlank() -> "Este campo es obligatorio"
                !esFechaNacimientoValida(datos.fechaNacimiento, hoy) ->
                    "Usa DD/MM/AAAA y una fecha existente que no sea futura"
                else -> null
            },
            celular = when {
                datos.celular.isBlank() -> "Este campo es obligatorio"
                !esCelularValido(datos.celular) ->
                    "Usa 9 dígitos comenzando con 9, o el formato +56 9…"
                else -> null
            },
            email = when {
                datos.email.isBlank() -> "Este campo es obligatorio"
                !esCorreoValido(datos.email) -> "Formato de correo no válido"
                else -> null
            },
            establecimiento = obligatorio(datos.establecimiento),
            comunaResidencia = obligatorio(datos.comunaResidencia),
            comunaEstablecimiento = obligatorio(datos.comunaEstablecimiento),
            actividadActual = obligatorio(datos.actividadActual, "Selecciona tu actividad actual"),
            jornada = obligatorio(datos.jornada, "Selecciona una jornada"),
            modalidad = obligatorio(datos.modalidad, "Selecciona una modalidad"),
            canalContacto = obligatorio(datos.canalContacto, "Selecciona cómo prefieres que te contacten"),
            region = obligatorio(datos.region, "Debes seleccionar una región"),
            sede = obligatorio(datos.sede, "Debes seleccionar una sede"),
            nivelInteres = obligatorio(datos.nivelInteres, "Selecciona tu nivel de interés"),
            carreras = when {
                datos.carreras.isEmpty() -> "Debes seleccionar al menos una carrera"
                datos.carreras.size > MAXIMO_CARRERAS ->
                    "Puedes seleccionar como máximo $MAXIMO_CARRERAS carreras"
                datos.carreras.any { it.isBlank() } -> "Selecciona carreras con un nombre válido"
                datos.carreras.map { it.trim() }.distinct().size != datos.carreras.size ->
                    "No puedes seleccionar la misma carrera más de una vez"
                else -> null
            }
        )
    }

    private fun obligatorio(
        valor: String,
        mensaje: String = "Este campo es obligatorio"
    ): String? = if (valor.isBlank()) mensaje else null

    fun esCorreoValido(valor: String): Boolean {
        val texto = valor.trim()
        if (texto.length > 254 || !formatoCorreo.matches(texto)) return false

        val parteLocal = texto.substringBefore('@')
        return parteLocal.length <= 64 &&
            !parteLocal.startsWith('.') &&
            !parteLocal.endsWith('.') &&
            ".." !in parteLocal
    }

    fun esRutValido(valor: String): Boolean {
        val sinPuntos = valor.trim()
            .uppercase(Locale.ROOT)
            .replace(".", "")

        if (!Regex("[0-9]{1,8}-?[0-9K]").matches(sinPuntos)) {
            return false
        }

        val rut = sinPuntos.replace("-", "")
        val cuerpo = rut.dropLast(1)
        val digitoIngresado = rut.last()

        if (cuerpo.toLong() == 0L) return false

        var suma = 0
        var multiplicador = 2

        // Se recorre el cuerpo de derecha a izquierda con factores del 2 al 7.
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val digitoEsperado = when (val resultado = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.toString().single()
        }

        return digitoIngresado == digitoEsperado
    }

    fun esCelularValido(valor: String): Boolean {
        val limpio = valor.trim().replace(Regex("""[\s()-]"""), "")
        return Regex("""(?:\+?56)?9[0-9]{8}""").matches(limpio)
    }

    fun esFechaNacimientoValida(
        valor: String,
        hoy: LocalDate = LocalDate.now()
    ): Boolean {
        val texto = valor.trim()
        if (!Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").matches(texto)) {
            return false
        }

        return try {
            val fecha = LocalDate.parse(texto, formatoFecha)
            fecha.year > 0 && !fecha.isAfter(hoy)
        } catch (_: DateTimeParseException) {
            false
        }
    }
}
