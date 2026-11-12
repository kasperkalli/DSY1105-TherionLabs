package cl.duoc.therionlabs.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.duoc.therionlabs.data.local.*
import cl.duoc.therionlabs.domain.ReglasSeguimiento
import cl.duoc.therionlabs.native.AccionesContacto
import cl.duoc.therionlabs.ui.components.CampoEntrada
import cl.duoc.therionlabs.ui.components.CampoSeleccion
import java.text.DateFormat
import java.util.Date

@Composable
fun DialogoSeguimiento(
    prospecto: ProspectoEntity,
    gestiones: List<SeguimientoEntity>,
    ocupado: Boolean,
    alCambiarBorrador: (Boolean) -> Unit,
    guardar: (Int, String, String, String, () -> Unit) -> Unit,
    cerrar: () -> Unit,
) {
    var estado by rememberSaveable(prospecto.id) { mutableStateOf("EN_SEGUIMIENTO") }
    var canal by
        rememberSaveable(prospecto.id) {
            mutableStateOf(if (prospecto.autorizaContacto) "Teléfono" else "Otro")
        }
    var nota by rememberSaveable(prospecto.id) { mutableStateOf("") }
    var intento by rememberSaveable(prospecto.id) { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val contexto = LocalContext.current
    val historial =
        gestiones
            .filter { it.prospectoId == prospecto.id }
            .sortedWith(
                compareByDescending<SeguimientoEntity> { it.fecha }.thenByDescending { it.id }
            )
    val canales = if (prospecto.autorizaContacto) ReglasSeguimiento.canales else listOf("Otro")
    LaunchedEffect(nota) { alCambiarBorrador(nota.isNotBlank()) }
    DisposableEffect(Unit) { onDispose { alCambiarBorrador(false) } }
    fun solicitarSalida() {
        if (ocupado) return
        if (nota.isNotBlank()) confirmarSalida = true else cerrar()
    }
    Dialog(
        onDismissRequest = ::solicitarSalida,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false),
    ) {
        BackHandler { solicitarSalida() }
        Surface(
            Modifier.fillMaxWidth().fillMaxHeight(0.92f).padding(12.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Contacto y seguimiento", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${prospecto.nombre} ${prospecto.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Estado actual: ${ReglasSeguimiento.etiqueta(historial.firstOrNull()?.estado ?: "SIN_CONTACTAR")}"
                )
                if (prospecto.autorizaContacto) {
                    OutlinedButton(
                        enabled = !ocupado,
                        onClick = {
                            mensaje =
                                AccionesContacto.telefono(contexto, prospecto.celular)
                                    ?: "Marcador abierto. Registra después el resultado."
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Abrir marcador telefónico")
                    }
                    OutlinedButton(
                        enabled = !ocupado,
                        onClick = {
                            mensaje =
                                AccionesContacto.correo(contexto, prospecto.email)
                                    ?: "Redacción de correo abierta. Confirma el envío en esa aplicación."
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Redactar correo")
                    }
                } else
                    Text(
                        "Este prospecto no autorizó contacto. Puedes registrar únicamente una gestión interna."
                    )
                mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                HorizontalDivider()
                CampoSeleccion(
                    "Resultado",
                    ReglasSeguimiento.etiqueta(estado),
                    ReglasSeguimiento.estados.values.toList(),
                    { valor ->
                        estado = ReglasSeguimiento.estados.entries.first { it.value == valor }.key
                    },
                    enabled = !ocupado,
                )
                CampoSeleccion(
                    "Canal utilizado",
                    canal,
                    canales,
                    { canal = it },
                    enabled = !ocupado,
                )
                CampoEntrada(
                    "Observación y resultado",
                    nota,
                    { nota = it },
                    lines = 3,
                    enabled = !ocupado,
                )
                Text("${nota.length}/500 caracteres", style = MaterialTheme.typography.labelSmall)
                if (intento)
                    ReglasSeguimiento.validar(estado, canal, nota)?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                Button(
                    enabled = !ocupado,
                    onClick = {
                        intento = true
                        if (ReglasSeguimiento.validar(estado, canal, nota) == null) {
                            guardar(prospecto.id, estado, canal, nota) {
                                nota = ""
                                intento = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (ocupado) "Guardando…" else "Guardar gestión")
                }
                Text("Historial (${historial.size})", style = MaterialTheme.typography.titleMedium)
                if (historial.isEmpty()) Text("Todavía no hay gestiones registradas.")
                historial.forEach { gestion ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                ReglasSeguimiento.etiqueta(gestion.estado),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(gestion.observacion)
                            Text(
                                "${gestion.canal} · ${DateFormat.getDateTimeInstance().format(Date(gestion.fecha))}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                gestion.administrativoNombre,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                OutlinedButton(
                    enabled = !ocupado,
                    onClick = ::solicitarSalida,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Volver a los prospectos")
                }
            }
        }
    }
    if (confirmarSalida)
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Descartar la observación?") },
            text = { Text("La gestión todavía no se ha guardado.") },
            confirmButton = {
                TextButton(
                    enabled = !ocupado,
                    onClick = {
                        confirmarSalida = false
                        cerrar()
                    },
                ) {
                    Text("Descartar y salir")
                }
            },
            dismissButton = {
                TextButton(enabled = !ocupado, onClick = { confirmarSalida = false }) {
                    Text("Seguir completando")
                }
            },
        )
}
