package cl.duoc.therionlabs.data.model

// Solo contiene datos: no depende de Compose, Room ni del ViewModel.
data class FormularioProspectos(
    val rut: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",
    val celular: String = "",
    val email: String = "",
    val establecimiento: String = "",
    val comunaResidencia: String = "",
    val comunaEstablecimiento: String = "",
    val actividadActual: String = "",
    val jornada: String = "",
    val region: String = "",
    val sede: String = "",
    val nivelInteres: String = "",
    val carreras: List<String> = emptyList(),
    val modalidad: String = "",
    val canalContacto: String = "",
    val observaciones: String = "",
    val fotoRuta: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val precisionMetros: Float? = null,
    val fechaUbicacion: Long? = null,
    val direccion: String? = null,
    val autorizaContacto: Boolean = false
)

// null significa que ese campo no tiene un mensaje de error.
data class ErroresFormularioProspectos(
    val rut: String? = null,
    val nombre: String? = null,
    val apellidos: String? = null,
    val fechaNacimiento: String? = null,
    val celular: String? = null,
    val email: String? = null,
    val establecimiento: String? = null,
    val comunaResidencia: String? = null,
    val comunaEstablecimiento: String? = null,
    val actividadActual: String? = null,
    val jornada: String? = null,
    val modalidad: String? = null,
    val canalContacto: String? = null,
    val region: String? = null,
    val sede: String? = null,
    val nivelInteres: String? = null,
    val carreras: String? = null
) {
    val hayErroresDatosPersonales: Boolean
        get() = listOf(
            rut, nombre, apellidos, fechaNacimiento, celular, email,
            establecimiento, comunaResidencia, comunaEstablecimiento,
            actividadActual, jornada, modalidad, canalContacto
        ).any { it != null }

    val hayErroresSeleccion: Boolean
        get() = listOf(region, sede, nivelInteres, carreras).any { it != null }

    val hayErrores: Boolean
        get() = hayErroresDatosPersonales || hayErroresSeleccion
}
