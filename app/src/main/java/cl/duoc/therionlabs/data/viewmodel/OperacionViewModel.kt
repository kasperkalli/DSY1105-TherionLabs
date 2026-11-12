package cl.duoc.therionlabs.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import cl.duoc.therionlabs.data.local.AppDatabase
import cl.duoc.therionlabs.data.repository.OperacionRepository
import cl.duoc.therionlabs.domain.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OperacionViewModel(aplicacion: Application, private val guardado: SavedStateHandle) :
    AndroidViewModel(aplicacion) {
    private val repositorio = OperacionRepository(AppDatabase.getDatabase(aplicacion))
    private val usuario = MutableStateFlow<SesionUsuario?>(null)
    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()
    private val _ocupado = MutableStateFlow(false)
    val ocupado = _ocupado.asStateFlow()
    private val _erroresEvento = MutableStateFlow<Map<String, String>>(emptyMap())
    val erroresEvento = _erroresEvento.asStateFlow()
    val borradorEvento = guardado.getStateFlow("evento", BorradorEvento())
    val eventos =
        repositorio.eventos
            .map { eventos ->
                eventos.sortedWith(
                    compareBy(
                        { evento ->
                            runCatching { LocalDate.parse(evento.fecha, formatoFecha) }
                                .getOrDefault(LocalDate.MIN)
                        },
                        { it.nombre },
                    )
                )
            }
            .catch { error ->
                if (error is CancellationException) throw error
                _mensaje.value = "No se pudieron cargar los eventos."
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gestiones =
        combine(repositorio.seguimientos, usuario) { gestiones, actual ->
                if (PermisosPerfil.puedeAdministrar(actual)) gestiones else emptyList()
            }
            .catch { error ->
                if (error is CancellationException) throw error
                _mensaje.value = "No se pudo cargar el seguimiento."
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun establecerUsuario(actual: SesionUsuario?) {
        if (usuario.value?.id != actual?.id) {
            if (guardado.get<String>("responsable") != actual?.id) limpiarEvento()
            usuario.value = actual
            guardado["responsable"] = actual?.id
            _mensaje.value = null
        }
    }

    fun actualizarEvento(cambio: (BorradorEvento) -> BorradorEvento) {
        if (_ocupado.value || !PermisosPerfil.puedeAdministrar(usuario.value)) return
        guardado["evento"] = cambio(borradorEvento.value)
        if (_erroresEvento.value.isNotEmpty())
            _erroresEvento.value = ReglasEvento.validar(borradorEvento.value)
    }

    fun limpiarEvento() {
        guardado["evento"] = BorradorEvento()
        _erroresEvento.value = emptyMap()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun crearEvento(terminar: () -> Unit) {
        val actual = usuario.value ?: return
        if (_ocupado.value || !PermisosPerfil.puedeAdministrar(actual)) return
        val datos = borradorEvento.value
        _erroresEvento.value = ReglasEvento.validar(datos)
        if (_erroresEvento.value.isNotEmpty()) return
        ejecutar("Evento creado. Ya está disponible para captación.") {
            repositorio.crearEvento(actual, datos)
            limpiarEvento()
            terminar()
        }
    }

    fun eliminarEvento(id: String) {
        val actual = usuario.value ?: return
        if (!PermisosPerfil.puedeAdministrar(actual)) return
        ejecutar("Evento eliminado.") { repositorio.eliminarEvento(actual, id) }
    }

    fun guardarGestion(id: Int, estado: String, canal: String, nota: String, terminar: () -> Unit) {
        val actual = usuario.value ?: return
        if (!PermisosPerfil.puedeAdministrar(actual)) return
        ejecutar("Gestión guardada.") {
            repositorio.guardarGestion(actual, id, estado, canal, nota)
            terminar()
        }
    }

    private fun ejecutar(confirmacion: String, accion: suspend () -> Unit) {
        if (_ocupado.value) return
        _ocupado.value = true
        viewModelScope.launch {
            try {
                accion()
                _mensaje.value = confirmacion
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: IllegalArgumentException) {
                _mensaje.value = error.message ?: "Revisa los datos ingresados."
            } catch (_: Exception) {
                _mensaje.value = "No se pudo completar la operación. Inténtalo nuevamente."
            } finally {
                _ocupado.value = false
            }
        }
    }
}
