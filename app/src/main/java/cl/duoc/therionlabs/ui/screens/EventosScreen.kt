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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import cl.duoc.therionlabs.network.RetrofitClient
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.CancellationException

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

    // Estadísticas reales obtenidas de MariaDB
    var claveAdmin by remember { mutableStateOf("") }
    var claveIngresada by remember { mutableStateOf("") }
    var mostrarClave by remember { mutableStateOf(false) }

    var totalesServidor by remember {
        mutableStateOf<Map<String, Long>?>(null)
    }

    var consultandoTotales by remember {
        mutableStateOf(false)
    }

    var errorTotales by remember {
        mutableStateOf<String?>(null)
    }

    var numeroConsulta by remember {
        mutableIntStateOf(0)
    }

    // Consultar estadísticas cuando Administración
    // ingresa la clave o solicita actualizar.
    LaunchedEffect(administrativo, claveAdmin, numeroConsulta) {

        if (!administrativo || claveAdmin.isBlank()) {
            totalesServidor = null
            return@LaunchedEffect
        }

        consultandoTotales = true
        errorTotales = null

        try {
            val respuesta = RetrofitClient.estadisticasAdminApi
                .obtenerProspectosPorEvento(claveAdmin)

            if (respuesta.isSuccessful) {

                val datos = respuesta.body()

                if (datos != null) {
                    totalesServidor = datos
                    errorTotales = null
                } else {
                    totalesServidor = null
                    errorTotales = "El servidor devolvió una respuesta vacía."
                }

            } else {

                totalesServidor = null

                errorTotales = when (respuesta.code()) {
                    401, 403 -> "Clave administrativa incorrecta."
                    else -> "Error del servidor: HTTP ${respuesta.code()}"
                }
            }

        } catch (error: CancellationException) {
            throw error

        } catch (error: Exception) {

            totalesServidor = null
            errorTotales = "No se pudieron consultar las estadísticas del servidor."

        } finally {
            consultandoTotales = false
        }
    }

    if (administrativo && mostrarClave) {
        AlertDialog(
            onDismissRequest = {
                claveIngresada = ""
                mostrarClave = false
            },
            title = {
                Text("Estadísticas de Administración")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Ingresa tu clave administrativa para consultar " +
                                "los prospectos registrados en MariaDB."
                    )

                    OutlinedTextField(
                        value = claveIngresada,
                        onValueChange = { claveIngresada = it },
                        label = { Text("Clave administrativa") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = claveIngresada.isNotBlank(),
                    onClick = {
                        claveAdmin = claveIngresada
                        claveIngresada = ""
                        mostrarClave = false
                        numeroConsulta++
                    }
                ) {
                    Text("Consultar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        claveIngresada = ""
                        mostrarClave = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
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
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = if (administrativo) {
                        val total = totalesServidor?.values?.sum()

                        if (total != null) {
                            "${eventos.size} eventos · $total prospectos en servidor"
                        } else {
                            "${eventos.size} eventos · Total del servidor no disponible"
                        }

                    } else {
                        "${eventos.size} eventos · " +
                                "${historial.registros.size} prospectos de tu cuenta"
                    },
                    style = MaterialTheme.typography.labelLarge
                )

                if (administrativo) {

                    OutlinedButton(
                        onClick = {
                            if (claveAdmin.isBlank()) {
                                mostrarClave = true
                            } else {
                                numeroConsulta++
                            }
                        },
                        enabled = !consultandoTotales,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (consultandoTotales) {
                                "Consultando servidor..."
                            } else if (claveAdmin.isBlank()) {
                                "Consultar prospectos del servidor"
                            } else {
                                "Actualizar totales del servidor"
                            }
                        )
                    }

                    if (consultandoTotales) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    errorTotales?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )

                        TextButton(
                            onClick = { mostrarClave = true }
                        ) {
                            Text("Ingresar clave nuevamente")
                        }
                    }

                    if (totalesServidor != null) {
                        Text(
                            text = "Cantidades consultadas en MariaDB.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
                        text = if (administrativo) {
                            val cantidad = totalesServidor?.get(evento.id) ?: 0L
                            if (totalesServidor != null) {
                                "$cantidad prospectos en servidor"
                            } else {
                                "Total de prospectos no disponible"
                            }
                        } else {
                            "${prospectos.size} prospectos de tu cuenta"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (administrativo) {
                                "Ver prospectos y seguimiento"
                            } else {
                                "Ver mis capturas"
                            }
                        )
                    }


                    if (administrativo) {
                        ExportarExcelBoton(
                            eventoId = evento.id,
                            ocupado = ocupado
                        )
                    }
                    if (
                        administrativo &&
                        !ocupado &&
                        !consultandoTotales &&
                        totalesServidor != null &&
                        (totalesServidor?.get(evento.id) ?: 0L) == 0L &&
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
            var mostrarCalendario by remember {
                mutableStateOf(false)
            }

            val hoy = LocalDate.now()
            val fechaMaxima = hoy.plusYears(5)
            val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu")

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Fecha del evento",
                    style = MaterialTheme.typography.labelLarge
                )

                OutlinedButton(
                    onClick = { mostrarCalendario = true },
                    enabled = !ocupado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = datos.fecha.ifBlank {
                            "Seleccionar fecha"
                        }
                    )
                }

                errores["fecha"]?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (mostrarCalendario) {

                val fechaInicial = runCatching {
                    LocalDate.parse(datos.fecha, formatoFecha)
                }.getOrNull()?.takeIf {
                    !it.isBefore(hoy) && !it.isAfter(fechaMaxima)
                }

                val estadoCalendario = rememberDatePickerState(
                    initialSelectedDateMillis = fechaInicial
                        ?.atStartOfDay(ZoneOffset.UTC)
                        ?.toInstant()
                        ?.toEpochMilli(),
                    yearRange = hoy.year..fechaMaxima.year,
                    selectableDates = object : SelectableDates {

                        override fun isSelectableDate(
                            utcTimeMillis: Long
                        ): Boolean {
                            val fecha = Instant
                                .ofEpochMilli(utcTimeMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            return !fecha.isBefore(hoy) &&
                                    !fecha.isAfter(fechaMaxima)
                        }

                        override fun isSelectableYear(
                            year: Int
                        ): Boolean {
                            return year in hoy.year..fechaMaxima.year
                        }
                    }
                )

                DatePickerDialog(
                    onDismissRequest = {
                        mostrarCalendario = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                estadoCalendario.selectedDateMillis
                                    ?.let { millis ->

                                        val fecha = Instant
                                            .ofEpochMilli(millis)
                                            .atZone(ZoneOffset.UTC)
                                            .toLocalDate()

                                        cambiar {
                                            it.copy(
                                                fecha = fecha.format(formatoFecha)
                                            )
                                        }
                                    }

                                mostrarCalendario = false
                            },
                            enabled = estadoCalendario.selectedDateMillis != null
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                mostrarCalendario = false
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(
                        state = estadoCalendario
                    )
                }
            }
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
