package cl.duoc.therionlabs.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cl.duoc.therionlabs.data.local.ProspectoEntity
import cl.duoc.therionlabs.data.viewmodel.EstadoHistorial
import cl.duoc.therionlabs.data.viewmodel.ProspectoViewModel
import cl.duoc.therionlabs.ui.components.*
import cl.duoc.therionlabs.ui.theme.Amarillo
import cl.duoc.therionlabs.ui.theme.AzulMarino
import cl.duoc.therionlabs.ui.theme.AzulProfundo
import cl.duoc.therionlabs.ui.theme.TherionLabsAppTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavHostController,
    prospectoViewModel: ProspectoViewModel
) {
    val estado by prospectoViewModel.estadoHistorial.collectAsState(
        initial = EstadoHistorial()
    )

    var prospectoABorrar by remember {
        mutableStateOf<ProspectoEntity?>(null)
    }

    HistoryScreenContent(
        estado = estado,
        onBorrarProspecto = { prospecto ->
            prospectoABorrar = prospecto
        }
    ) {
        navController.navigate("formscreen") {
            popUpTo("formscreen") {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    val seleccionado = prospectoABorrar

    if (seleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                prospectoABorrar = null
            },
            title = {
                Text("¿Borrar prospecto?")
            },
            text = {
                Text(
                    "Se eliminará el registro de " +
                            "${seleccionado.nombre} ${seleccionado.apellidos} " +
                            "de este teléfono."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prospectoABorrar = null
                        prospectoViewModel.borrarProspecto(seleccionado.id)
                    }
                ) {
                    Text(
                        text = "Borrar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prospectoABorrar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    val error = prospectoViewModel.errorBorrado

    if (error != null) {
        AlertDialog(
            onDismissRequest = {
                prospectoViewModel.limpiarErrorBorrado()
            },
            title = {
                Text("No se pudo borrar")
            },
            text = {
                Text(error)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prospectoViewModel.limpiarErrorBorrado()
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun HistoryScreenContent(estado: EstadoHistorial, onBorrarProspecto: (ProspectoEntity)-> Unit={},onVolverAlFormulario:() -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior("Registros guardados", "Formulario", onVolverAlFormulario) }
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val compacto = maxHeight < 460.dp
            Column(Modifier.widthIn(max = 720.dp).fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ResumenRegistros(
                    cantidad = if (estado.cargando || estado.error != null) null else estado.registros.size,
                    compacto = compacto
                )
                when {
                    estado.cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator()
                            Text("Cargando tus registros…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    estado.error != null -> {
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("No pudimos abrir los registros", style = MaterialTheme.typography.titleMedium)
                                Text(estado.error.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = onVolverAlFormulario) { Text("Volver al formulario") }
                            }
                        }
                    }
                    estado.registros.isEmpty() -> {
                        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                    Column(Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                            Icon(IconosFormulario.Registros, contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(20.dp).size(36.dp))
                                        }
                                        Text("Todo listo para comenzar", style = MaterialTheme.typography.titleLarge)
                                        Text("Los prospectos que registres aparecerán aquí.",
                                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Button(onClick = onVolverAlFormulario, shape = RoundedCornerShape(14.dp)) {
                                            Text("Crear primer registro")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Tus prospectos", style = MaterialTheme.typography.titleMedium)
                            Text("Más recientes primero", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LazyColumn(Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                            items(estado.registros, key = { it.id }) { prospecto -> TarjetaProspecto(prospecto = prospecto, onBorrar = {
                                onBorrarProspecto(prospecto)
                            }) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenRegistros(cantidad: Int?, compacto: Boolean) {
    Surface(shape = RoundedCornerShape(24.dp), color = AzulMarino) {
        Row(Modifier.fillMaxWidth().padding(if (compacto) 16.dp else 22.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("EN ESTE TELÉFONO", style = MaterialTheme.typography.labelSmall, color = Amarillo)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedContent(targetState = cantidad, label = "Total de registros") { total ->
                        Text(total?.toString() ?: "…", style = if (compacto) MaterialTheme.typography.headlineMedium
                            else MaterialTheme.typography.displaySmall, color = androidx.compose.ui.graphics.Color.White)
                    }
                    Text("registros\nguardados", style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.84f))
                }
                if (!compacto) {
                    Text("Consulta cada prospecto y abre sus detalles.", style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f))
                }
            }
            Surface(color = Amarillo, shape = RoundedCornerShape(17.dp)) {
                Icon(IconosFormulario.Registros, contentDescription = null,
                    tint = AzulProfundo, modifier = Modifier.padding(15.dp).size(27.dp))
            }
        }
    }
}

@Composable
private fun TarjetaProspecto(prospecto: ProspectoEntity, onBorrar:() -> Unit) {
    var expandida by rememberSaveable(prospecto.id) { mutableStateOf(false) }
    val rotacion by animateFloatAsState(if (expandida) 180f else 0f, tween(220), label = "Abrir detalles")
    val carreras = listOfNotNull(prospecto.carrera1, prospecto.carrera2, prospecto.carrera3).filter { it.isNotBlank() }
    Card(
        onClick = { expandida = !expandida },
        modifier = Modifier.fillMaxWidth().semantics { stateDescription = if (expandida) "Detalles abiertos" else "Detalles cerrados" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(IconosFormulario.Persona, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp).size(22.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${prospecto.nombre} ${prospecto.apellidos}", style = MaterialTheme.typography.titleMedium)
                    Text(formatearFechaCaptura(prospecto.fechaCaptura), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            LineaRegistro(IconosFormulario.Correo, prospecto.email)
            LineaRegistro(IconosFormulario.Telefono, prospecto.celular)
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    LineaRegistro(IconosFormulario.Edificio, prospecto.sede)
                    LineaRegistro(IconosFormulario.Estudios, carreras.firstOrNull() ?: "Sin carrera registrada")
                }
            }
            AnimatedVisibility(visible = expandida,
                enter = expandVertically(tween(250)) + fadeIn(tween(180)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(120))) {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DatoDetalle("RUT", prospecto.rut)
                    DatoDetalle("Fecha de nacimiento", prospecto.fechaNacimiento)
                    DatoDetalle("Comuna de residencia", prospecto.comunaResidencia)
                    DatoDetalle("Establecimiento", prospecto.establecimiento)
                    DatoDetalle("Comuna del establecimiento", prospecto.comunaEstablecimiento)
                    DatoDetalle("Actividad actual", prospecto.actividadActual)
                    DatoDetalle("Región", prospecto.region)
                    DatoDetalle("Jornada", prospecto.jornada)
                    DatoDetalle("Modalidad", prospecto.modalidad.ifBlank { "Sin registrar" })
                    DatoDetalle("Nivel de interés", prospecto.nivelInteres)
                    DatoDetalle("Preferencias de carrera", carreras.joinToString("\n"))
                    DatoDetalle("Canal de contacto", prospecto.canalContacto.ifBlank { "Sin registrar" })
                    DatoDetalle("Autoriza contacto", if (prospecto.autorizaContacto) "Sí" else "No")
                    DatoDetalle("Observaciones", prospecto.observaciones.ifBlank { "Sin observaciones" })
                    cl.duoc.therionlabs.ui.components.DetalleRecursosNativos(
                        prospecto.fotoRuta, prospecto.latitud, prospecto.longitud,
                        prospecto.precisionMetros, prospecto.fechaUbicacion, prospecto.direccion)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    OutlinedButton(
                        onClick = onBorrar,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Borrar")
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(IconosFormulario.Check, contentDescription = null, modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.tertiary)
                Text("Guardado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.weight(1f))
                Text(if (expandida) "Ocultar detalles" else "Ver detalles", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Icon(IconosFormulario.Abajo, contentDescription = null,
                    modifier = Modifier.size(18.dp).rotate(rotacion), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun LineaRegistro(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp).size(17.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DatoDetalle(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatearFechaCaptura(valor: Long): String {
    if (valor <= 0L) return "Sin fecha de captura registrada"
    return DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale("es", "CL"))
        .format(Instant.ofEpochMilli(valor).atZone(ZoneId.systemDefault()))
}

@Preview(name = "Historial", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HistoryScreenPreview() {
    val registro = remember {
        ProspectoEntity(id = 1, rut = "12345678-5", nombre = "Estudiante", apellidos = "de Prueba",
            fechaNacimiento = "15/08/2008", celular = "+56912345678", email = "demo@example.com",
            establecimiento = "Colegio de prueba", comunaResidencia = "Comuna de prueba",
            comunaEstablecimiento = "Comuna de prueba", actividadActual = "4to Medio", jornada = "Diurna",
            region = "Región de Valparaiso", sede = "Viña del Mar", nivelInteres = "Interés Alto",
            carrera1 = "Analista Programador", modalidad = "Presencial", canalContacto = "Correo electrónico")
    }
    TherionLabsAppTheme(darkTheme = false) {
        HistoryScreenContent(EstadoHistorial(registros = listOf(registro), cargando = false)) {}
    }
}

@Preview(name = "Historial vacío", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HistoryScreenEmptyPreview() {
    TherionLabsAppTheme(darkTheme = false) {
        HistoryScreenContent(EstadoHistorial(cargando = false)) {}
    }
}
