package cl.duoc.therionlabs.data.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.therionlabs.data.local.AppDatabase
import cl.duoc.therionlabs.data.local.EventoEntity
import cl.duoc.therionlabs.data.local.ProspectoEntity
import cl.duoc.therionlabs.data.model.ErroresFormularioProspectos
import cl.duoc.therionlabs.data.model.FormularioProspectos
import cl.duoc.therionlabs.data.repository.ProspectoRepository
import cl.duoc.therionlabs.domain.*
import cl.duoc.therionlabs.util.ValidacionesProspecto
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class EstadoHistorial(
    val registros: List<ProspectoEntity> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
)

class ProspectoViewModel(
    private val repositorio: ProspectoRepository,
    private val guardado: SavedStateHandle,
) : ViewModel() {

    var formulario by
        mutableStateOf(guardado.get<FormularioProspectos>("formulario") ?: FormularioProspectos())
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

    private val usuarioActivo = MutableStateFlow<SesionUsuario?>(null)
    val estadoHistorial: Flow<EstadoHistorial> =
        combine(repositorio.registros(), usuarioActivo) { registros, usuario ->
                EstadoHistorial(
                    registros =
                        registros.filter { PermisosPerfil.puedeConsultar(usuario, it.captadorId) },
                    cargando = false,
                )
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.e(TAG, "Error al consultar Room", error)
                emit(
                    EstadoHistorial(
                        cargando = false,
                        error = "No se pudieron cargar los registros.",
                    )
                )
            }

    fun establecerUsuario(usuario: SesionUsuario?) {
        if (usuarioActivo.value?.id != usuario?.id) {
            // Un borrador restaurado solo pertenece a la misma cuenta.
            val responsable = guardado.get<String>("responsable")
            if (responsable != usuario?.id) descartarFormulario()
            usuarioActivo.value = usuario
            guardado["responsable"] = usuario?.id
        }
    }

    fun seleccionarEvento(evento: EventoEntity) {
        if (guardando || !PermisosPerfil.puedeCapturar(usuarioActivo.value) || !evento.activo)
            return
        if (formulario.eventoId != evento.id) {
            formulario =
                FormularioProspectos(
                    eventoId = evento.id,
                    eventoNombre = evento.nombre,
                    establecimiento = evento.colegio,
                    comunaEstablecimiento = evento.comuna,
                )
            guardado["formulario"] = formulario
            guardado["formularioInicial"] = formulario
            erroresFormulario = ErroresFormularioProspectos()
        }
    }

    fun tieneBorrador(): Boolean =
        formulario !=
            (guardado.get<FormularioProspectos>("formularioInicial") ?: FormularioProspectos())

    fun descartarFormulario() {
        formulario = FormularioProspectos()
        guardado["formulario"] = formulario
        guardado["formularioInicial"] = formulario
        erroresFormulario = ErroresFormularioProspectos()
        mostrarErroresDatosPersonales = false
        mostrarErroresSeleccion = false
        errorGuardado = null
    }

    fun actualizarFormulario(nuevoFormulario: FormularioProspectos) {
        if (
            guardando ||
                registroGuardadoId != null ||
                !PermisosPerfil.puedeCapturar(usuarioActivo.value)
        )
            return
        // Las preferencias deben corresponder a la sede seleccionada.
        formulario =
            if (nuevoFormulario.region != formulario.region) {
                nuevoFormulario.copy(
                    region = CatalogoRegional.region,
                    sede = "",
                    carreras = emptyList(),
                )
            } else {
                nuevoFormulario.copy(
                    eventoId = formulario.eventoId,
                    eventoNombre = formulario.eventoNombre,
                    carreras =
                        nuevoFormulario.carreras.filter {
                            it in CatalogoRegional.carreras(nuevoFormulario.sede)
                        },
                )
            }
        guardado["formulario"] = formulario
        errorGuardado = null
        actualizarErrores()
    }

    fun alternarCarrera(carrera: String) {
        if (guardando || registroGuardadoId != null) return
        val carreraLimpia = carrera.trim()
        if (carreraLimpia.isBlank()) return

        val actuales = formulario.carreras
        val nuevasCarreras =
            when {
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
        erroresFormulario =
            errores.copy(
                rut =
                    errores.rut.takeIf {
                        mostrarErroresDatosPersonales || formulario.rut.isNotBlank()
                    },
                nombre = errores.nombre.takeIf { mostrarErroresDatosPersonales },
                apellidos = errores.apellidos.takeIf { mostrarErroresDatosPersonales },
                fechaNacimiento =
                    errores.fechaNacimiento.takeIf {
                        mostrarErroresDatosPersonales || formulario.fechaNacimiento.isNotBlank()
                    },
                celular =
                    errores.celular.takeIf {
                        mostrarErroresDatosPersonales || formulario.celular.isNotBlank()
                    },
                email =
                    errores.email.takeIf {
                        mostrarErroresDatosPersonales || formulario.email.isNotBlank()
                    },
                establecimiento = errores.establecimiento.takeIf { mostrarErroresDatosPersonales },
                comunaResidencia =
                    errores.comunaResidencia.takeIf { mostrarErroresDatosPersonales },
                comunaEstablecimiento =
                    errores.comunaEstablecimiento.takeIf { mostrarErroresDatosPersonales },
                actividadActual = errores.actividadActual.takeIf { mostrarErroresDatosPersonales },
                jornada = errores.jornada.takeIf { mostrarErroresDatosPersonales },
                modalidad = errores.modalidad.takeIf { mostrarErroresDatosPersonales },
                canalContacto = errores.canalContacto.takeIf { mostrarErroresDatosPersonales },
                region = errores.region.takeIf { mostrarErroresSeleccion },
                sede = errores.sede.takeIf { mostrarErroresSeleccion },
                nivelInteres = errores.nivelInteres.takeIf { mostrarErroresSeleccion },
                carreras = errores.carreras.takeIf { mostrarErroresSeleccion },
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

        val usuario = usuarioActivo.value ?: return
        if (!PermisosPerfil.puedeCapturar(usuario) || formulario.eventoId.isBlank()) {
            errorGuardado = "Selecciona primero un evento creado por administración."
            return
        }
        val datos = formulario
        val rutLimpio =
            datos.rut
                .filterNot { it == '.' || it == '-' || it.isWhitespace() }
                .uppercase(Locale.ROOT)
        val rutNormalizado = rutLimpio.dropLast(1) + "-" + rutLimpio.last()
        val digitosCelular = datos.celular.filter { it.isDigit() }
        val celularNormalizado =
            if (digitosCelular.length == 9) {
                "+56" + digitosCelular
            } else {
                "+" + digitosCelular
            }

        val prospecto =
            ProspectoEntity(
                eventoId = datos.eventoId,
                eventoNombre = datos.eventoNombre,
                captadorId = usuario.id,
                captador = usuario.nombre,
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
                autorizaContacto = datos.autorizaContacto,
            )

        guardando = true
        viewModelScope.launch {
            try {
                val id = repositorio.guardarProspecto(usuario, prospecto)
                check(id > 0L) { "Room no devolvió un identificador de fila válido" }
                descartarFormulario()
                erroresFormulario = ErroresFormularioProspectos()
                mostrarErroresDatosPersonales = false
                mostrarErroresSeleccion = false
                registroGuardadoId = id
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: IllegalArgumentException) {
                errorGuardado = error.message ?: "Revisa los datos del registro."
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

    fun limpiarErrorBorrado() {
        errorBorrado = null
    }

    companion object {
        private const val TAG = "ProspectoViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = checkNotNull(this[APPLICATION_KEY])
                val baseDeDatos = AppDatabase.getDatabase(aplicacion)
                val repositorio = ProspectoRepository(baseDeDatos)
                ProspectoViewModel(repositorio, createSavedStateHandle())
            }
        }
    }

    var errorBorrado by mutableStateOf<String?>(null)
        private set

    fun borrarProspecto(id: Int) {
        val usuario = usuarioActivo.value ?: return
        if (!PermisosPerfil.puedeAdministrar(usuario)) return
        errorBorrado = null

        viewModelScope.launch {
            try {
                repositorio.borrarProspecto(usuario, id)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Log.e(TAG, "Error al borrar el prospecto", error)
                errorBorrado =
                    "No se pudo borrar. Los prospectos con gestiones conservan su historial y no se pueden eliminar."
            }
        }
    }
}
