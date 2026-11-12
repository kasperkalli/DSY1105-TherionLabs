package cl.duoc.therionlabs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import cl.duoc.therionlabs.data.local.EventoEntity
import cl.duoc.therionlabs.data.viewmodel.*
import cl.duoc.therionlabs.domain.*
import cl.duoc.therionlabs.ui.components.IconosFormulario
import cl.duoc.therionlabs.ui.screens.*
import cl.duoc.therionlabs.ui.theme.TherionLabsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TherionLabsAppTheme { Aplicacion() } }
    }
}

@Composable
fun Aplicacion(
    acceso: AccesoViewModel = viewModel(),
    formulario: ProspectoViewModel = viewModel(factory = ProspectoViewModel.Factory),
    operacion: OperacionViewModel = viewModel(),
) {
    val usuario by acceso.sesion.collectAsState()
    val ocupado by acceso.ocupado.collectAsState()
    val error by acceso.error.collectAsState()
    var cuentaPreparada by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(usuario?.id) {
        formulario.establecerUsuario(usuario)
        operacion.establecerUsuario(usuario)
        cuentaPreparada = usuario?.id
    }
    val actual = usuario
    if (actual == null) AccesoScreen(ocupado, error, acceso::ingresar)
    else if (cuentaPreparada == actual.id)
        key(actual.id) {
            NavegacionSesion(actual, formulario, operacion) {
                formulario.descartarFormulario()
                operacion.limpiarEvento()
                acceso.cerrarSesion()
            }
        }
}

@Composable
private fun NavegacionSesion(
    usuario: SesionUsuario,
    formulario: ProspectoViewModel,
    operacion: OperacionViewModel,
    salir: () -> Unit,
) {
    val navegador = rememberNavController()
    val entrada by navegador.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val eventos by operacion.eventos.collectAsState()
    val gestiones by operacion.gestiones.collectAsState()
    val historial by formulario.estadoHistorial.collectAsState(initial = EstadoHistorial())
    val borradorEvento by operacion.borradorEvento.collectAsState()
    val erroresEvento by operacion.erroresEvento.collectAsState()
    val operacionOcupada by operacion.ocupado.collectAsState()
    val mensaje by operacion.mensaje.collectAsState()
    val ocupado = formulario.guardando || operacionOcupada
    val administrativo = PermisosPerfil.puedeAdministrar(usuario)
    val avisos = remember { SnackbarHostState() }
    var salidaPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }
    var cerrarSesion by remember { mutableStateOf(false) }
    val capturaAbierta = ruta == "formscreen" || ruta == "selector_sede"
    val eventoAbierto = ruta == "crear_evento"

    fun navegar(destino: String) {
        navegador.navigate(destino) { launchSingleTop = true }
    }
    fun cambiarSeccion(destino: String) {
        navegador.navigate(destino) {
            popUpTo("eventos")
            launchSingleTop = true
        }
    }
    fun abandonar(accion: () -> Unit) {
        if (ocupado) return
        if (
            (capturaAbierta && formulario.tieneBorrador()) ||
                (eventoAbierto && borradorEvento.tieneCambios())
        ) {
            salidaPendiente = accion
        } else accion()
    }
    fun capturar(evento: EventoEntity) {
        if (formulario.formulario.eventoId != evento.id && formulario.tieneBorrador()) {
            salidaPendiente = {
                formulario.seleccionarEvento(evento)
                navegar("formscreen")
            }
        } else {
            formulario.seleccionarEvento(evento)
            navegar("formscreen")
        }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            avisos.showSnackbar(it)
            operacion.limpiarMensaje()
        }
    }
    val idGuardado = formulario.registroGuardadoId
    LaunchedEffect(idGuardado, historial.registros) {
        if (idGuardado != null) {
            historial.registros
                .find { it.id.toLong() == idGuardado }
                ?.let { prospecto ->
                    navegador.navigate("prospectos/${prospecto.eventoId}") {
                        popUpTo("eventos")
                        launchSingleTop = true
                    }
                    formulario.consumirConfirmacionGuardado()
                    avisos.showSnackbar("Prospecto guardado en el evento.")
                }
        }
    }
    BackHandler(
        enabled =
            (capturaAbierta && ruta == "formscreen" && formulario.tieneBorrador()) ||
                (eventoAbierto && borradorEvento.tieneCambios())
    ) {
        abandonar { navegador.popBackStack() }
    }
    BackHandler(enabled = ocupado) {}

    salidaPendiente?.let {
        AlertDialog(
            onDismissRequest = { salidaPendiente = null },
            title = { Text("¿Salir sin guardar?") },
            text = { Text("Hay datos pendientes. Puedes seguir completándolos o descartarlos.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val accion = salidaPendiente
                        salidaPendiente = null
                        if (capturaAbierta || formulario.tieneBorrador())
                            formulario.descartarFormulario()
                        if (eventoAbierto) operacion.limpiarEvento()
                        accion?.invoke()
                    }
                ) {
                    Text("Descartar y salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { salidaPendiente = null }) { Text("Seguir completando") }
            },
        )
    }
    if (cerrarSesion)
        AlertDialog(
            onDismissRequest = { cerrarSesion = false },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text(
                    "Los registros guardados se conservan. Los borradores pendientes se descartarán."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cerrarSesion = false
                        salir()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = { TextButton(onClick = { cerrarSesion = false }) { Text("Cancelar") } },
        )

    Scaffold(
        snackbarHost = { SnackbarHost(avisos) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = ruta == "eventos",
                    enabled = !ocupado,
                    onClick = { abandonar { cambiarSeccion("eventos") } },
                    icon = { Icon(IconosFormulario.Calendario, null) },
                    label = { Text("Eventos") },
                )
                NavigationBarItem(
                    selected = ruta == "registros" || ruta == "prospectos/{id}",
                    enabled = !ocupado,
                    onClick = { abandonar { cambiarSeccion("registros") } },
                    icon = { Icon(IconosFormulario.Registros, null) },
                    label = { Text("Registros") },
                )
                NavigationBarItem(
                    selected = ruta == "cuenta",
                    enabled = !ocupado,
                    onClick = { abandonar { cambiarSeccion("cuenta") } },
                    icon = { Icon(IconosFormulario.Persona, null) },
                    label = { Text("Cuenta") },
                )
            }
        },
    ) { espacio ->
        NavHost(navegador, startDestination = "eventos", modifier = Modifier.padding(espacio)) {
            for (destino in listOf("eventos", "registros")) composable(destino) {
                EventosScreen(
                    usuario,
                    eventos,
                    historial,
                    ocupado,
                    destino == "registros",
                    { navegar("crear_evento") },
                    operacion::eliminarEvento,
                    ::capturar,
                    { navegar("prospectos/$it") },
                )
            }
            composable("crear_evento") {
                if (administrativo)
                    CrearEventoScreen(
                        borradorEvento,
                        erroresEvento,
                        ocupado,
                        operacion::actualizarEvento,
                        { operacion.crearEvento { navegador.popBackStack() } },
                        { abandonar { navegador.popBackStack() } },
                    )
                else AccesoRestringido()
            }
            composable("formscreen") {
                if (
                    PermisosPerfil.puedeCapturar(usuario) &&
                        formulario.formulario.eventoId.isNotBlank()
                ) {
                    FormScreen(
                        formulario,
                        { navegar("selector_sede") },
                        { abandonar { cambiarSeccion("registros") } },
                    )
                } else AccesoRestringido()
            }
            composable("selector_sede") {
                if (
                    PermisosPerfil.puedeCapturar(usuario) &&
                        formulario.formulario.eventoId.isNotBlank()
                ) {
                    SelectorSedeScreen(
                        formulario,
                        { if (!ocupado) navegador.popBackStack() },
                        { abandonar { cambiarSeccion("registros") } },
                    )
                } else AccesoRestringido()
            }
            composable("prospectos/{id}") { destino ->
                val id = destino.arguments?.getString("id").orEmpty()
                if (id != "anteriores" || administrativo) {
                    HistoryScreen(
                        formulario,
                        if (id == "anteriores") "" else id,
                        eventos.find { it.id == id }?.nombre ?: "Registros anteriores sin evento",
                        usuario,
                        gestiones,
                        ocupado,
                        {},
                        operacion::guardarGestion,
                    ) {
                        cambiarSeccion("registros")
                    }
                } else AccesoRestringido()
            }
            composable("cuenta") {
                Column(
                    Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text("Mi cuenta", style = MaterialTheme.typography.headlineMedium)
                    Text(usuario.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(if (administrativo) "Administrativo" else "Captador")
                    Text(
                        if (administrativo)
                            "Puedes crear eventos, consultar prospectos y registrar su seguimiento."
                        else
                            "Puedes registrar prospectos en eventos disponibles y consultar tus capturas."
                    )
                    Text(CatalogoRegional.region, style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Los datos se conservan en este dispositivo. La sesión termina al cerrar el proceso.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = { cerrarSesion = true }, enabled = !ocupado) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}

@Composable
private fun AccesoRestringido() {
    Text(
        "Esta función no está disponible para tu perfil. Selecciona Eventos para continuar.",
        Modifier.padding(24.dp),
    )
}
