package cl.duoc.therionlabs.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.therionlabs.ui.theme.Amarillo
import cl.duoc.therionlabs.ui.theme.AzulMarino
import cl.duoc.therionlabs.ui.theme.AzulProfundo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    textoAccion: String,
    onAccion: () -> Unit,
    onAtras: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text("THERIONLABS", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            onAtras?.let { accion ->
                IconButton(onClick = accion) {
                    Icon(IconosFormulario.Atras, contentDescription = "Volver")
                }
            }
        },
        actions = {
            TextButton(onClick = onAccion) {
                Icon(IconosFormulario.Registros, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(textoAccion)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun CabeceraFormulario(titulo: String, descripcion: String, paso: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = RoundedCornerShape(26.dp), color = AzulMarino) {
            Box(Modifier.fillMaxWidth()) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(alpha = 0.045f), size.width * 0.43f,
                        Offset(size.width * 1.02f, size.height * 0.08f))
                    drawCircle(Amarillo.copy(alpha = 0.12f), size.width * 0.20f,
                        Offset(size.width * 0.99f, size.height * 0.98f))
                }
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            Surface(color = Amarillo, shape = RoundedCornerShape(7.dp)) {
                                Text("REGISTRO DIGITAL", Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    color = AzulProfundo, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("0$paso / 02", color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium)
                    }
                    Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(descripcion, style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f))
                }
            }
        }
        var destino by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(paso) { destino = paso / 2f }
        val progreso by animateFloatAsState(destino, tween(450), label = "Avance del registro")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("1. Tus datos", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text("2. Preferencias", style = MaterialTheme.typography.labelMedium,
                    color = if (paso == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun TituloSeccion(numero: String, titulo: String, descripcion: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(numero, Modifier.padding(11.dp), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(descripcion, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TarjetaCampo(
    titulo: String,
    icono: ImageVector,
    mensajeError: String? = null,
    opcional: Boolean = false,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    var tieneFoco by remember { mutableStateOf(false) }
    val borde by animateColorAsState(
        targetValue = when {
            mensajeError != null -> MaterialTheme.colorScheme.error
            tieneFoco -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        }, animationSpec = tween(180), label = "Borde del campo"
    )
    val fondo by animateColorAsState(
        targetValue = if (mensajeError != null) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(180), label = "Fondo del campo"
    )
    Card(
        modifier = modifier.fillMaxWidth().onFocusChanged { tieneFoco = it.hasFocus }.focusGroup(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(1.dp, borde)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) {
                    Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(9.dp).size(19.dp))
                }
                Text(if (opcional) titulo else "$titulo *", Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall)
                if (opcional) {
                    Text("Opcional", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(11.dp))
            contenido()
            MensajeCampo(mensajeError)
        }
    }
}

@Composable
fun MensajeCampo(mensaje: String?) {
    var ultimoMensaje by remember { mutableStateOf("") }
    LaunchedEffect(mensaje) { if (mensaje != null) ultimoMensaje = mensaje }
    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
        exit = fadeOut(tween(120)) + shrinkVertically(tween(180))
    ) {
        Row(Modifier.padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Top) {
            Icon(IconosFormulario.Aviso, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 2.dp).size(15.dp))
            Text(mensaje ?: ultimoMensaje, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

@Composable
fun CampoTextoTarjeta(
    titulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    icono: ImageVector,
    mensajeError: String? = null,
    ejemplo: String = "",
    tipoTeclado: KeyboardType = KeyboardType.Text,
    opcional: Boolean = false,
    multilinea: Boolean = false
) {
    TarjetaCampo(titulo, icono, mensajeError, opcional) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,

            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = titulo
                },
            placeholder = {
                Text(
                    text = ejemplo,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = !multilinea,
            minLines = if (multilinea) 3 else 1,
            isError = mensajeError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = tipoTeclado,
                imeAction = if (multilinea) {
                    ImeAction.Default
                } else {
                    ImeAction.Next
                }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampo()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSeleccionTarjeta(
    titulo: String,
    valor: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit,
    icono: ImageVector,
    mensajeError: String? = null,
    habilitado: Boolean = true,
    ejemplo: String = "Selecciona una opción"
) {
    var expandido by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(habilitado) { if (!habilitado) expandido = false }
    TarjetaCampo(titulo, icono, mensajeError) {
        ExposedDropdownMenuBox(expanded = expandido,
            onExpandedChange = { if (habilitado) expandido = !expandido }) {
            OutlinedTextField(
                value = valor, onValueChange = {}, readOnly = true, enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = habilitado)
                    .semantics { contentDescription = titulo },
                placeholder = { Text(ejemplo, style = MaterialTheme.typography.bodyMedium) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                singleLine = true, isError = mensajeError != null,
                shape = RoundedCornerShape(12.dp), colors = coloresCampo()
            )
            ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false },
                modifier = Modifier.heightIn(max = 320.dp)) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(text = { Text(opcion) }, onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    })
                }
            }
        }
    }
}

@Composable
fun CampoOpcionesTarjeta(
    titulo: String,
    valor: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit,
    icono: ImageVector,
    mensajeError: String? = null
) {
    TarjetaCampo(titulo, icono, mensajeError) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            opciones.forEach { opcion ->
                val seleccionado = valor == opcion
                val fondo by animateColorAsState(
                    if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tween(160), label = "Opción seleccionada"
                )
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(fondo)
                    .selectable(selected = seleccionado, role = Role.RadioButton, onClick = { onSeleccionar(opcion) })
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = seleccionado, onClick = null)
                    Text(opcion, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp))
                }
            }
        }
    }
}

@Composable
fun EtiquetaLocal(modifier: Modifier = Modifier, texto: String = "Guardado en este teléfono") {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(IconosFormulario.Candado, contentDescription = null, modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
