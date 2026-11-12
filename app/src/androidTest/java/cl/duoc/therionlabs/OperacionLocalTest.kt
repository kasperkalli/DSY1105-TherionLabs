package cl.duoc.therionlabs

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.therionlabs.data.local.*
import cl.duoc.therionlabs.data.repository.*
import cl.duoc.therionlabs.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OperacionLocalTest {
    private lateinit var base: AppDatabase
    private lateinit var operacion: OperacionRepository
    private lateinit var prospectos: ProspectoRepository
    private lateinit var administrativo: SesionUsuario
    private lateinit var captador: SesionUsuario

    @Before
    fun preparar() = runBlocking {
        base =
            Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    AppDatabase::class.java,
                )
                .build()
        operacion = OperacionRepository(base)
        prospectos = ProspectoRepository(base)
        operacion.inicializar()
        administrativo = requireNotNull(operacion.ingresar("administrativo", "Admin2026!"))
        captador = requireNotNull(operacion.ingresar("captador", "Captador2026!"))
    }

    @After
    fun cerrar() {
        base.close()
    }

    @Test
    fun respetaLosRolesYExigeUnEventoExistente() = runBlocking {
        debeFallar { operacion.crearEvento(captador, evento()) }
        debeFallar { prospectos.guardarProspecto(captador, ficha("inexistente")) }
        val id = operacion.crearEvento(administrativo, evento())
        debeFallar { prospectos.guardarProspecto(administrativo, ficha(id)) }
        val fichaId = prospectos.guardarProspecto(captador, ficha(id)).toInt()
        assertEquals("CAP-01", base.prospectoDao().obtenerPorId(fichaId)?.captadorId)
        debeFallar { operacion.eliminarEvento(administrativo, id) }
        debeFallar {
            operacion.guardarGestion(
                captador,
                fichaId,
                "CONTACTADO",
                "Correo",
                "Se entregó información",
            )
        }
    }

    @Test
    fun controlaDuplicadosPorEventoYConservaLasGestiones() = runBlocking {
        val primero = operacion.crearEvento(administrativo, evento())
        val segundo =
            operacion.crearEvento(administrativo, evento().copy(nombre = "Visita de orientación"))
        val fichaId = prospectos.guardarProspecto(captador, ficha(primero)).toInt()
        debeFallar {
            prospectos.guardarProspecto(
                captador,
                ficha(primero).copy(rut = "123456785", email = "otra@ejemplo.cl"),
            )
        }
        debeFallar {
            prospectos.guardarProspecto(
                captador,
                ficha(primero).copy(rut = "11.111.111-1", email = " PERSONA@EJEMPLO.CL "),
            )
        }
        prospectos.guardarProspecto(captador, ficha(segundo))
        assertEquals(2, base.prospectoDao().obtenerProspectos().first().size)
        operacion.guardarGestion(
            administrativo,
            fichaId,
            "EN_SEGUIMIENTO",
            "Correo",
            "Se preparó información académica",
        )
        operacion.guardarGestion(
            administrativo,
            fichaId,
            "CONTACTADO",
            "Correo",
            "Se entregó información académica",
        )
        assertEquals(2, operacion.seguimientos.first().size)
        debeFallar { prospectos.borrarProspecto(administrativo, fichaId) }
        assertNotNull(base.prospectoDao().obtenerPorId(fichaId))
    }

    private fun evento() =
        BorradorEvento(
            "Feria vocacional",
            "Colegio de prueba",
            "20/11/2026",
            "Quilpué",
            "Orientación académica",
        )

    private fun ficha(eventoId: String) =
        ProspectoEntity(
            eventoId = eventoId,
            rut = "12.345.678-5",
            nombre = "Persona",
            apellidos = "De prueba",
            fechaNacimiento = "01/01/2005",
            celular = "+56912345678",
            email = "persona@ejemplo.cl",
            establecimiento = "Colegio de prueba",
            comunaResidencia = "Quilpué",
            comunaEstablecimiento = "Quilpué",
            actividadActual = "4to Medio",
            jornada = "Diurna",
            region = CatalogoRegional.region,
            sede = "Valparaíso",
            nivelInteres = "Interés Alto",
            carrera1 = "Analista Programador",
            autorizaContacto = true,
        )

    private suspend fun debeFallar(accion: suspend () -> Unit) {
        var fallo = false
        try {
            accion()
        } catch (_: Exception) {
            fallo = true
        }
        assertTrue("La operación debía ser rechazada", fallo)
    }
}
