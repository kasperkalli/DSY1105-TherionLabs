package cl.duoc.therionlabs.data.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.therionlabs.data.local.AppDatabase
import cl.duoc.therionlabs.data.local.ProspectoEntity
import cl.duoc.therionlabs.data.model.ErroresFormularioProspectos
import cl.duoc.therionlabs.data.model.FormularioProspectos
import cl.duoc.therionlabs.data.repository.ProspectoRepository
import cl.duoc.therionlabs.util.ValidacionesProspecto
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class EstadoHistorial(
    val registros: List<ProspectoEntity> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class ProspectoViewModel(
    private val repositorio: ProspectoRepository
) : ViewModel() {

    var formulario by mutableStateOf(FormularioProspectos())
        private set

    var erroresFormulario by mutableStateOf(ErroresFormularioProspectos())
        private set

    private var mostrarErroresDatosPersonales = false
    private var mostrarErroresSeleccion = false

    val maximoCarreras: Int = ValidacionesProspecto.MAXIMO_CARRERAS

    val puedeAgregarCarreras: Boolean
        get() = formulario.carreras.size < maximoCarreras

    val puedeElegirSede: Boolean
        get() = formulario.region.isNotBlank()

    var guardando by mutableStateOf(false)
        private set

    var errorGuardado by mutableStateOf<String?>(null)
        private set

    var registroGuardadoId by mutableStateOf<Long?>(null)
        private set

    val estadoHistorial: Flow<EstadoHistorial> = repositorio.todosLosProspectos
        .map { registros ->
            EstadoHistorial(registros = registros, cargando = false)
        }
        .catch { error ->
            Log.e(TAG, "Error al consultar los prospectos en Room", error)
            emit(
                EstadoHistorial(
                    cargando = false,
                    error = "No se pudieron cargar los registros."
                )
            )
        }

    fun actualizarFormulario(nuevoFormulario: FormularioProspectos) {
        if (guardando || registroGuardadoId != null) return
        // Una sede de la región anterior no debe conservarse al cambiar de región.
        formulario = if (nuevoFormulario.region != formulario.region) {
            nuevoFormulario.copy(sede = "")
        } else {
            nuevoFormulario
        }
        errorGuardado = null
        actualizarErrores()
    }

    fun seleccionarRegion(region: String) {
        actualizarFormulario(formulario.copy(region = region))
    }

    fun alternarCarrera(carrera: String) {
        if (guardando || registroGuardadoId != null) return
        val carreraLimpia = carrera.trim()
        if (carreraLimpia.isBlank()) return

        val actuales = formulario.carreras
        val nuevasCarreras = when {
            carreraLimpia in actuales -> actuales - carreraLimpia
            puedeAgregarCarreras -> actuales + carreraLimpia
            else -> return
        }
        actualizarFormulario(formulario.copy(carreras = nuevasCarreras))
    }

    fun limpiarCarreras() {
        actualizarFormulario(formulario.copy(carreras = emptyList()))
    }

    fun validarDatosPersonales(): Boolean {
        if (guardando || registroGuardadoId != null) return false
        mostrarErroresDatosPersonales = true
        val errores = actualizarErrores()
        return !errores.hayErroresDatosPersonales
    }

    private fun actualizarErrores(): ErroresFormularioProspectos {
        val errores = ValidacionesProspecto.validar(formulario)
        // Los formatos se revisan al escribir. Los vacíos se muestran al continuar.
        erroresFormulario = errores.copy(
            rut = errores.rut.takeIf { mostrarErroresDatosPersonales || formulario.rut.isNotBlank() },
            nombre = errores.nombre.takeIf { mostrarErroresDatosPersonales },
            apellidos = errores.apellidos.takeIf { mostrarErroresDatosPersonales },
            fechaNacimiento = errores.fechaNacimiento.takeIf {
                mostrarErroresDatosPersonales || formulario.fechaNacimiento.isNotBlank()
            },
            celular = errores.celular.takeIf {
                mostrarErroresDatosPersonales || formulario.celular.isNotBlank()
            },
            email = errores.email.takeIf {
                mostrarErroresDatosPersonales || formulario.email.isNotBlank()
            },
            establecimiento = errores.establecimiento.takeIf { mostrarErroresDatosPersonales },
            comunaResidencia = errores.comunaResidencia.takeIf { mostrarErroresDatosPersonales },
            comunaEstablecimiento = errores.comunaEstablecimiento.takeIf { mostrarErroresDatosPersonales },
            actividadActual = errores.actividadActual.takeIf { mostrarErroresDatosPersonales },
            jornada = errores.jornada.takeIf { mostrarErroresDatosPersonales },
            modalidad = errores.modalidad.takeIf { mostrarErroresDatosPersonales },
            canalContacto = errores.canalContacto.takeIf { mostrarErroresDatosPersonales },
            region = errores.region.takeIf { mostrarErroresSeleccion },
            sede = errores.sede.takeIf { mostrarErroresSeleccion },
            nivelInteres = errores.nivelInteres.takeIf { mostrarErroresSeleccion },
            carreras = errores.carreras.takeIf { mostrarErroresSeleccion }
        )
        return errores
    }

    fun guardarProspecto() {
        // Evita dos guardados mientras se procesa el mismo clic.
        if (guardando || registroGuardadoId != null) return
        errorGuardado = null
        mostrarErroresDatosPersonales = true
        mostrarErroresSeleccion = true

        val errores = actualizarErrores()
        if (errores.hayErroresDatosPersonales) {
            errorGuardado = "Vuelve a la primera pantalla y revisa los campos marcados."
            return
        }
        if (errores.hayErroresSeleccion) {
            return
        }

        val datos = formulario
        val rutLimpio = datos.rut.trim()
            .replace(".", "")
            .replace("-", "")
            .uppercase(Locale.ROOT)
        val rutNormalizado = rutLimpio.dropLast(1) + "-" + rutLimpio.last()
        val digitosCelular = datos.celular.filter { it.isDigit() }
        val celularNormalizado = if (digitosCelular.length == 9) {
            "+56" + digitosCelular
        } else {
            "+" + digitosCelular
        }

        val prospecto = ProspectoEntity(
            rut = rutNormalizado,
            nombre = datos.nombre.trim(),
            apellidos = datos.apellidos.trim(),
            fechaNacimiento = datos.fechaNacimiento.trim(),
            celular = celularNormalizado,
            email = datos.email.trim(),
            establecimiento = datos.establecimiento.trim(),
            comunaResidencia = datos.comunaResidencia.trim(),
            comunaEstablecimiento = datos.comunaEstablecimiento.trim(),
            actividadActual = datos.actividadActual,
            jornada = datos.jornada,
            region = datos.region,
            sede = datos.sede,
            nivelInteres = datos.nivelInteres,
            carrera1 = datos.carreras.first(),
            carrera2 = datos.carreras.getOrNull(1),
            carrera3 = datos.carreras.getOrNull(2),
            modalidad = datos.modalidad,
            canalContacto = datos.canalContacto,
            observaciones = datos.observaciones.trim(),
            fotoRuta = datos.fotoRuta,
            latitud = datos.latitud,
            longitud = datos.longitud,
            precisionMetros = datos.precisionMetros,
            fechaUbicacion = datos.fechaUbicacion,
            direccion = datos.direccion,
            autorizaContacto = datos.autorizaContacto
        )

        guardando = true
        viewModelScope.launch {
            try {
                val id = repositorio.guardarProspecto(prospecto)
                check(id > 0L) { "Room no devolvió un identificador de fila válido" }
                formulario = FormularioProspectos()
                erroresFormulario = ErroresFormularioProspectos()
                mostrarErroresDatosPersonales = false
                mostrarErroresSeleccion = false
                registroGuardadoId = id
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Log.e(TAG, "Error al guardar el prospecto en Room", error)
                errorGuardado = "No se pudo guardar el registro. Tus respuestas se conservaron."
            } finally {
                guardando = false
            }
        }
    }

    fun consumirConfirmacionGuardado() {
        registroGuardadoId = null
    }

    fun limpiarErrorBorrado(){
        errorBorrado = null
    }

    companion object {
        private const val TAG = "ProspectoViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[APPLICATION_KEY])
                val baseDeDatos = AppDatabase.getDatabase(aplicacion)
                val repositorio = ProspectoRepository(baseDeDatos.prospectoDao())
                ProspectoViewModel(repositorio)
            }
        }
    }

    var errorBorrado by mutableStateOf<String?>(null)
        private set

    fun borrarProspecto(id: Int) {
        errorBorrado = null

        viewModelScope.launch {
            try {
                repositorio.borrarProspecto(id)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Log.e(TAG, "Error al borrar el prospecto", error)
                errorBorrado = "No se pudo borrar el prospecto. Inténtalo nuevamente."
            }
        }
    }
}
