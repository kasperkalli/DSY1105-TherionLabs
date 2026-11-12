package cl.duoc.therionlabs.data.repository

import androidx.room.withTransaction
import cl.duoc.therionlabs.data.local.*
import cl.duoc.therionlabs.domain.*
import cl.duoc.therionlabs.util.ValidacionesProspecto
import java.util.Locale

class ProspectoRepository(private val db: AppDatabase) {
    private val dao = db.prospectoDao()

    fun registros() = dao.obtenerProspectos()

    suspend fun guardarProspecto(usuario: SesionUsuario, prospecto: ProspectoEntity): Long =
        db.withTransaction {
            val responsable = requireNotNull(db.operacionDao().usuarioPorId(usuario.id))
            require(responsable.rol == Roles.CAPTADOR) {
                "Solo el captador puede registrar prospectos."
            }
            val event =
                requireNotNull(db.operacionDao().evento(prospecto.eventoId)) {
                    "Selecciona un evento disponible."
                }
            require(event.activo && !ReglasEvento.esDemostracion(event.id)) {
                "El evento no está disponible."
            }
            require(
                prospecto.region == CatalogoRegional.region &&
                    prospecto.sede in CatalogoRegional.sedes
            )
            val carreras = listOfNotNull(prospecto.carrera1, prospecto.carrera2, prospecto.carrera3)
            require(
                carreras.size in 1..3 &&
                    carreras.distinct().size == carreras.size &&
                    carreras.all { it in CatalogoRegional.carreras(prospecto.sede) }
            ) {
                "Revisa las carreras de la sede elegida."
            }
            require(
                ValidacionesProspecto.esRutValido(prospecto.rut) &&
                    ValidacionesProspecto.esCorreoValido(prospecto.email)
            )
            val rut =
                prospecto.rut
                    .filterNot { it == '.' || it == '-' || it.isWhitespace() }
                    .uppercase(Locale.ROOT)
            val email = prospecto.email.trim().lowercase(Locale.ROOT)
            if (dao.existeRut(event.id, rut))
                throw ProspectoDuplicadoException("Este RUT ya está registrado en el evento.")
            if (dao.existeCorreo(event.id, email))
                throw ProspectoDuplicadoException("Este correo ya está registrado en el evento.")
            dao.insertarProspecto(
                prospecto.copy(
                    eventoNombre = event.nombre,
                    captadorId = responsable.id,
                    captador = responsable.nombre,
                    email = email,
                )
            )
        }

    suspend fun borrarProspecto(usuario: SesionUsuario, id: Int) = db.withTransaction {
        require(db.operacionDao().usuarioPorId(usuario.id)?.rol == Roles.ADMINISTRATIVO) {
            "Solo administración puede eliminar registros."
        }
        dao.borrarProspecto(id)
    }
}
