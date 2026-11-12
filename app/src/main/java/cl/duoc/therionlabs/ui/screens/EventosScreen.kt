package cl.duoc.therionlabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.therionlabs.data.local.*
import cl.duoc.therionlabs.data.viewmodel.EstadoHistorial
import cl.duoc.therionlabs.domain.*
import cl.duoc.therionlabs.ui.components.*
import cl.duoc.therionlabs.ui.theme.Amarillo
import cl.duoc.therionlabs.ui.theme.AzulMarino

@Composable
fun EventosScreen(
    usuario: SesionUsuario,
    eventos: List<EventoEntity>,
    historial: EstadoHistorial,
    ocupado: Boolean,
    verRegistros: Boolean,
    crear: () -> Unit,
    eliminar: (String) -> Unit,
    capturar: (EventoEntity) -> Unit,
    abrir: (String) -> Unit,
) {
    val administrativo = PermisosPerfil.puedeAdministrar(usuario)
    var busqueda by rememberSaveable { mutableStateOf("") }
    var eliminarEvento by remember { mutableStateOf<EventoEntity?>(null) }
    val visibles = eventos.filter {
        (administrativo || it.activo) &&
            CatalogoRegional.normalizar("${it.nombre} ${it.colegio} ${it.comuna} ${it.fecha}")
                .contains(CatalogoRegional.normalizar(busqueda))
    }
    eliminarEvento?.let { evento ->
        AlertDialog(
            onDismissRequest = { eliminarEvento = null },
            title = { Text("¿Eliminar evento?") },
            text = {
                Text(
                    "Se eliminará ${evento.nombre}. Solo se pueden eliminar eventos sin prospectos."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !ocupado,
                    onClick = {
                        eliminarEvento = null
                        eliminar(evento.id)
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { eliminarEvento = null }) { Text("Cancelar") }
            },
        )
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Surface(color = AzulMarino, shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "THERIONLABS · VALPARAÍSO",
                        color = Amarillo,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        if (verRegistros) "Registros por evento" else "Eventos de captación",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        if (administrativo) "Organiza las actividades y acompaña cada contacto."
                        else "Selecciona una actividad para registrar a sus interesados.",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            Text(
                "${eventos.size} eventos · ${historial.registros.size} prospectos${if (administrativo) "" else " de tu cuenta"}",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (administrativo && !verRegistros)
            item {
                Button(onClick = crear, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear evento")
                }
            }
        item {
            CampoEntrada("Buscar evento, institución, comuna o fecha", busqueda, { busqueda = it })
        }
        if (historial.cargando) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        historial.error?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
        if (visibles.isEmpty())
            item {
                Text(
                    if (eventos.isEmpty()) {
                        if (administrativo)
                            "Crea el primer evento para habilitar el registro de prospectos."
                        else
                            "Todavía no hay eventos. Administración debe crear uno en este dispositivo para comenzar."
                    } else "No se encontraron eventos para esta búsqueda."
                )
            }
        items(visibles, key = { it.id }) { evento ->
            val prospectos = historial.registros.filter { it.eventoId == evento.id }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(evento.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(evento.colegio, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${evento.fecha} · ${evento.comuna}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(evento.actividad, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${prospectos.size} prospectos${if (administrativo) "" else " de tu cuenta"}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (!administrativo && !verRegistros)
                        Button(
                            onClick = { capturar(evento) },
                            enabled = !ocupado,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Registrar prospecto")
                        }
                    OutlinedButton(
                        onClick = { abrir(evento.id) },
                        enabled = !ocupado,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (administrativo) "Ver prospectos y seguimiento"
                            else "Ver mis capturas"
                        )
                    }
                    if (
                        administrativo &&
                            !historial.cargando &&
                            historial.error == null &&
                            prospectos.isEmpty()
                    ) {
                        TextButton(onClick = { eliminarEvento = evento }, enabled = !ocupado) {
                            Text("Eliminar evento", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        if (administrativo && historial.registros.any { it.eventoId.isBlank() })
            item {
                OutlinedCard(
                    onClick = { abrir("anteriores") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "Registros anteriores sin evento",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "${historial.registros.count { it.eventoId.isBlank() }} prospectos conservados de la entrega anterior."
                        )
                        Text("Consultar registros", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
    }
}

@Composable
fun CrearEventoScreen(
    datos: BorradorEvento,
    errores: Map<String, String>,
    ocupado: Boolean,
    cambiar: ((BorradorEvento) -> BorradorEvento) -> Unit,
    guardar: () -> Unit,
    volver: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TituloOperacion(
                "Crear evento",
                "La actividad estará disponible para los captadores de este dispositivo.",
            )
        }
        item {
            CampoEntrada(
                "Nombre del evento",
                datos.nombre,
                { valor -> cambiar { it.copy(nombre = valor) } },
                errores["nombre"],
                enabled = !ocupado,
            )
        }
        item {
            CampoEntrada(
                "Colegio o institución",
                datos.colegio,
                { valor -> cambiar { it.copy(colegio = valor) } },
                errores["colegio"],
                enabled = !ocupado,
            )
        }
        item {
            CampoEntrada(
                "Fecha · DD/MM/AAAA",
                datos.fecha,
                { valor -> cambiar { it.copy(fecha = valor) } },
                errores["fecha"],
                enabled = !ocupado,
            )
        }
        item {
            CampoSeleccion(
                "Comuna",
                datos.comuna,
                CatalogoRegional.comunas,
                { valor -> cambiar { it.copy(comuna = valor) } },
                errores["comuna"],
                enabled = !ocupado,
            )
        }
        item {
            CampoEntrada(
                "Descripción",
                datos.actividad,
                { valor -> cambiar { it.copy(actividad = valor) } },
                errores["actividad"],
                lines = 3,
                enabled = !ocupado,
            )
        }
        item {
            Button(onClick = guardar, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
                Text(if (ocupado) "Guardando…" else "Crear evento")
            }
        }
        item { TextButton(onClick = volver, enabled = !ocupado) { Text("Volver a eventos") } }
    }
}
