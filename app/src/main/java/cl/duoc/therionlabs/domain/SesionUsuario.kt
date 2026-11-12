package cl.duoc.therionlabs.domain

data class SesionUsuario(val id: String, val nombre: String, val rol: String)

object Roles {
    const val CAPTADOR = "CAPTADOR"
    const val ADMINISTRATIVO = "ADMINISTRATIVO"
}

object PermisosPerfil {
    fun puedeCapturar(usuario: SesionUsuario?) = usuario?.rol == Roles.CAPTADOR

    fun puedeAdministrar(usuario: SesionUsuario?) = usuario?.rol == Roles.ADMINISTRATIVO

    fun puedeConsultar(usuario: SesionUsuario?, responsable: String) =
        usuario != null &&
            (puedeAdministrar(usuario) || (puedeCapturar(usuario) && usuario.id == responsable))
}
