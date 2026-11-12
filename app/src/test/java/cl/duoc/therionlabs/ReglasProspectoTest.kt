package cl.duoc.therionlabs

import cl.duoc.therionlabs.data.model.FormularioProspectos
import cl.duoc.therionlabs.domain.*
import cl.duoc.therionlabs.util.ValidacionesProspecto
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ReglasProspectoTest {
    @Test
    fun lasSedesPertenecenSoloAValparaiso() {
        assertEquals(listOf("Valparaíso", "Viña del Mar"), CatalogoRegional.sedes)
        assertTrue(CatalogoRegional.carreras("Alameda").isEmpty())
        assertTrue(CatalogoRegional.carreras("Valparaíso").contains("Técnico en Enfermería"))
        assertFalse(CatalogoRegional.carreras("Viña del Mar").contains("Técnico en Enfermería"))
    }

    @Test
    fun rechazaCarrerasQueNoCorrespondenALaSede() {
        val formulario =
            FormularioProspectos(sede = "Viña del Mar", carreras = listOf("Técnico en Enfermería"))
        assertNotNull(ValidacionesProspecto.validar(formulario).carreras)
        assertNull(ValidacionesProspecto.validar(formulario.copy(sede = "Valparaíso")).carreras)
    }

    @Test
    fun rutConEspaciosConservaSuDigitoVerificador() {
        assertTrue(ValidacionesProspecto.esRutValido(" 12.345.678 - 5 "))
        assertFalse(ValidacionesProspecto.esRutValido("12.345.678-4"))
        assertFalse(ValidacionesProspecto.esRutValido("0-0"))
    }

    @Test
    fun validaFechasYCorreosSinAceptarFormatosIncompletos() {
        val hoy = LocalDate.of(2026, 11, 11)
        assertTrue(ValidacionesProspecto.esFechaNacimientoValida("29/02/2004", hoy))
        assertFalse(ValidacionesProspecto.esFechaNacimientoValida("29/02/2005", hoy))
        assertFalse(ValidacionesProspecto.esFechaNacimientoValida("12/11/2026", hoy))
        assertFalse(ValidacionesProspecto.esCorreoValido("persona..prueba@ejemplo.cl"))
        assertTrue(ValidacionesProspecto.esCorreoValido("persona@ejemplo.cl"))
    }

    @Test
    fun unCaptadorSoloConsultaSusRegistros() {
        val captador = SesionUsuario("CAP-01", "Captador", Roles.CAPTADOR)
        val administrador = SesionUsuario("ADM-01", "Administración", Roles.ADMINISTRATIVO)
        assertTrue(PermisosPerfil.puedeConsultar(captador, "CAP-01"))
        assertFalse(PermisosPerfil.puedeConsultar(captador, "CAP-02"))
        assertFalse(PermisosPerfil.puedeConsultar(captador, ""))
        assertTrue(PermisosPerfil.puedeConsultar(administrador, ""))
        assertFalse(PermisosPerfil.puedeCapturar(administrador))
    }

    @Test
    fun elEventoRequiereFechaRealYComunaDeLaRegion() {
        val evento =
            BorradorEvento(
                "Feria de orientación",
                "Colegio de prueba",
                "20/11/2026",
                "Quilpué",
                "Presentación de carreras",
            )
        assertTrue(ReglasEvento.validar(evento).isEmpty())
        assertNotNull(ReglasEvento.validar(evento.copy(comuna = "Santiago"))["comuna"])
        assertNotNull(ReglasEvento.validar(evento.copy(fecha = "31/02/2026"))["fecha"])
    }

    @Test
    fun lasGestionesExigenResultadoCanalYObservacion() {
        assertNull(
            ReglasSeguimiento.validar("CONTACTADO", "Correo", "Se entregó información académica")
        )
        assertNotNull(ReglasSeguimiento.validar("DESCONOCIDO", "Correo", "Se entregó información"))
        assertNotNull(ReglasSeguimiento.validar("CONTACTADO", "Correo", ""))
    }

    @Test
    fun lasClavesSeVerificanConLaSalDeCadaCuenta() {
        val sal = ProtectorClave.generarSal()
        val resumen = ProtectorClave.derivar("ClaveDePrueba!", sal)
        assertTrue(ProtectorClave.coincide("ClaveDePrueba!", sal, resumen))
        assertFalse(ProtectorClave.coincide("OtraClave", sal, resumen))
        assertNotEquals(
            resumen,
            ProtectorClave.derivar("ClaveDePrueba!", ProtectorClave.generarSal()),
        )
    }
}
