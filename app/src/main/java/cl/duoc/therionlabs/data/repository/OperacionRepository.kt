package cl.duoc.therionlabs.data.repository

import androidx.room.withTransaction
import cl.duoc.therionlabs.data.local.*
import cl.duoc.therionlabs.domain.*
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OperacionRepository(private val db: AppDatabase) {
    private val dao = db.operacionDao()
    val eventos =
        dao.eventos().map { items -> items.filterNot { ReglasEvento.esDemostracion(it.id) } }
    val seguimientos = dao.seguimientos()

    suspend fun inicializar() =
        withContext(Dispatchers.IO) {
            db.withTransaction {
                if (dao.totalUsuarios() == 0) {
                    val cuentas =
                        listOf(
                                listOf(
                                    "CAP-01",
                                    "captador",
                                    "Captador de terreno",
                                    Roles.CAPTADOR,
                                    "Captador2026!",
                                ),
                                listOf(
                                    "CAP-02",
                                    "captador2",
                                    "Segundo captador",
                                    Roles.CAPTADOR,
                                    "Captador2026!",
                                ),
                                listOf(
                                    "ADM-01",
                                    "administrativo",
                                    "Administración de prospectos",
                                    Roles.ADMINISTRATIVO,
                                    "Admin2026!",
                                ),
                            )
                            .map { fila ->
                                val sal = ProtectorClave.generarSal()
                                UsuarioEntity(
                                    fila[0],
                                    fila[1],
                                    fila[2],
                                    fila[3],
                                    sal,
                                    ProtectorClave.derivar(fila[4], sal),
                                )
                            }
                    dao.insertarUsuarios(cuentas)
                }
            }
        }

    suspend fun eliminarEvento(usuario: SesionUsuario, id: String) = db.withTransaction {
        require(dao.usuarioPorId(usuario.id)?.rol == Roles.ADMINISTRATIVO) {
            "Solo administración puede eliminar eventos."
        }
        requireNotNull(dao.evento(id)) { "El evento ya no existe." }
        require(dao.eliminarEventoVacio(id) == 1) {
            "No se puede eliminar un evento con prospectos registrados."
        }
    }

    suspend fun crearEvento(usuario: SesionUsuario, borrador: BorradorEvento): String =
        db.withTransaction {
            require(dao.usuarioPorId(usuario.id)?.rol == Roles.ADMINISTRATIVO) {
                "Solo administración puede crear eventos."
            }
            require(ReglasEvento.validar(borrador).isEmpty()) { "Revisa los campos del evento." }
            val eventos = dao.listaEventos().filterNot { ReglasEvento.esDemostracion(it.id) }
            require(
                eventos.none {
                    CatalogoRegional.normalizar(it.nombre) ==
                        CatalogoRegional.normalizar(borrador.nombre) &&
                        CatalogoRegional.normalizar(it.colegio) ==
                            CatalogoRegional.normalizar(borrador.colegio) &&
                        it.fecha == borrador.fecha
                }
            ) {
                "Ya existe ese evento para la misma institución y fecha."
            }
            val id = java.util.UUID.randomUUID().toString()
            dao.insertarEventos(
                listOf(
                    EventoEntity(
                        id,
                        borrador.nombre.trim(),
                        borrador.colegio.trim(),
                        borrador.fecha,
                        borrador.actividad.trim(),
                        comuna = borrador.comuna.trim(),
                    )
                )
            )
            id
        }

    suspend fun ingresar(nombre: String, clave: String): SesionUsuario? =
        withContext(Dispatchers.IO) {
            val usuario = dao.usuario(nombre.trim().lowercase(Locale.ROOT))
            if (
                usuario != null &&
                    ProtectorClave.coincide(clave, usuario.salt, usuario.passwordHash)
            )
                SesionUsuario(usuario.id, usuario.nombre, usuario.rol)
            else null
        }

    suspend fun guardarGestion(
        usuario: SesionUsuario,
        prospectoId: Int,
        estado: String,
        canal: String,
        observacion: String,
    ) = db.withTransaction {
        val cuentaGuardada = requireNotNull(dao.usuarioPorId(usuario.id))
        require(cuentaGuardada.rol == Roles.ADMINISTRATIVO) {
            "Solo el perfil administrativo puede registrar gestiones."
        }
        val prospecto =
            requireNotNull(db.prospectoDao().obtenerPorId(prospectoId)) {
                "El prospecto ya no está disponible."
            }
        require(prospecto.autorizaContacto || canal == "Otro") {
            "Sin autorización de contacto, registra solo una gestión interna con canal Otro."
        }
        val error = ReglasSeguimiento.validar(estado, canal, observacion)
        require(error == null) { error.orEmpty() }
        dao.insertarSeguimiento(
            SeguimientoEntity(
                prospectoId = prospectoId,
                administrativoId = usuario.id,
                administrativoNombre = cuentaGuardada.nombre,
                estado = estado,
                canal = canal,
                observacion = observacion.trim(),
                fecha = System.currentTimeMillis(),
            )
        )
    }
}
