package cl.duoc.therionlabs.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.therionlabs.data.model.ErroresFormularioProspectos
import cl.duoc.therionlabs.data.model.FormularioProspectos
import cl.duoc.therionlabs.data.viewmodel.ProspectoViewModel
import cl.duoc.therionlabs.domain.CatalogoRegional
import cl.duoc.therionlabs.ui.components.*
import cl.duoc.therionlabs.ui.theme.TherionLabsAppTheme

@Composable
fun SelectorSedeScreen(
    prospectoViewModel: ProspectoViewModel,
    alVolver: () -> Unit,
    alVerRegistros: () -> Unit,
) {
    SelectorSedeContent(
        formulario = prospectoViewModel.formulario,
        errores = prospectoViewModel.erroresFormulario,
        guardando = prospectoViewModel.guardando,
        errorGuardado = prospectoViewModel.errorGuardado,
        maximoCarreras = prospectoViewModel.maximoCarreras,
        puedeAgregarCarreras = prospectoViewModel.puedeAgregarCarreras,
        puedeElegirSede = prospectoViewModel.puedeElegirSede,
        onFormularioChange = prospectoViewModel::actualizarFormulario,
        onCarreraClick = prospectoViewModel::alternarCarrera,
        onLimpiarCarreras = prospectoViewModel::limpiarCarreras,
        onGuardar = prospectoViewModel::guardarProspecto,
        onAtras = alVolver,
        onVerRegistros = alVerRegistros,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorSedeContent(
    formulario: FormularioProspectos,
    errores: ErroresFormularioProspectos,
    guardando: Boolean,
    errorGuardado: String?,
    maximoCarreras: Int,
    puedeAgregarCarreras: Boolean,
    puedeElegirSede: Boolean,
    onFormularioChange: (FormularioProspectos) -> Unit,
    onCarreraClick: (String) -> Unit,
    onLimpiarCarreras: () -> Unit,
    onGuardar: () -> Unit,
    onAtras: () -> Unit,
    onVerRegistros: () -> Unit,
) {
    var recursoOcupado by remember { mutableStateOf(false) }
    var menuCarreras by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior("Tus preferencias", "Registros", onVerRegistros, onAtras) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                CabeceraFormulario(
                    "Intereses académicos",
                    formulario.eventoNombre.ifBlank {
                        "Selecciona una sede y hasta tres carreras."
                    },
                    2,
                )
                TituloSeccion(
                    "01",
                    "Dónde estudiar",
                    "Selecciona una sede de la Región de Valparaíso.",
                )
                Text(CatalogoRegional.region, style = MaterialTheme.typography.labelLarge)
                CampoSeleccionTarjeta(
                    "Sede",
                    formulario.sede,
                    CatalogoRegional.sedes,
                    { sede ->
                        onFormularioChange(
                            formulario.copy(
                                sede = sede,
                                carreras =
                                    formulario.carreras.filter {
                                        it in CatalogoRegional.carreras(sede)
                                    },
                            )
                        )
                    },
                    IconosFormulario.Edificio,
                    mensajeError = errores.sede,
                    habilitado = puedeElegirSede,
                    ejemplo = if (puedeElegirSede) "Selecciona una sede" else "Selecciona una sede",
                )
                CampoOpcionesTarjeta(
                    "Nivel de interés",
                    formulario.nivelInteres,
                    listOf("Interés Alto", "Interés Medio", "Interés Bajo"),
                    { onFormularioChange(formulario.copy(nivelInteres = it)) },
                    IconosFormulario.Interes,
                    mensajeError = errores.nivelInteres,
                )

                Text(
                    "Las carreras se filtran por sede. Al cambiarla se retiran las preferencias que no correspondan.",
                    style = MaterialTheme.typography.bodySmall,
                )
                TituloSeccion(
                    "02",
                    "Qué quieres estudiar",
                    "Elige hasta $maximoCarreras carreras, en orden de preferencia.",
                )
                TarjetaCampo("Carreras de interés", IconosFormulario.Estudios, errores.carreras) {
                    ExposedDropdownMenuBox(
                        expanded = menuCarreras,
                        onExpandedChange = { menuCarreras = !menuCarreras },
                    ) {
                        OutlinedTextField(
                            value =
                                if (formulario.carreras.isEmpty()) ""
                                else "${formulario.carreras.size} de $maximoCarreras carreras",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            placeholder = { Text("Elegir del listado") },
                            isError = errores.carreras != null,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCarreras)
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .semantics { contentDescription = "Carreras de interés" },
                            colors = coloresCampo(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = menuCarreras,
                            onDismissRequest = { menuCarreras = false },
                            modifier = Modifier.heightIn(max = 380.dp),
                        ) {
                            CatalogoRegional.porEscuela(formulario.sede).forEach { (area, carreras)
                                ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            area,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    onClick = {},
                                    enabled = false,
                                )
                                carreras.forEach { carrera ->
                                    val seleccionada = carrera in formulario.carreras
                                    val habilitada = seleccionada || puedeAgregarCarreras
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                carrera,
                                                fontWeight =
                                                    if (seleccionada) FontWeight.SemiBold
                                                    else FontWeight.Normal,
                                            )
                                        },
                                        leadingIcon = {
                                            Checkbox(
                                                checked = seleccionada,
                                                onCheckedChange = null,
                                                enabled = habilitada,
                                            )
                                        },
                                        enabled = habilitada,
                                        onClick = {
                                            onCarreraClick(carrera)
                                            menuCarreras = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Tus preferencias",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedContent(
                        targetState = formulario.carreras.size,
                        label = "Cantidad de carreras",
                    ) { cantidad ->
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                "$cantidad de $maximoCarreras",
                                Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
                for (indice in 0 until maximoCarreras) {
                    val carrera = formulario.carreras.getOrNull(indice)
                    TarjetaPreferencia(
                        indice + 1,
                        carrera,
                        !guardando,
                        onQuitar = { carrera?.let(onCarreraClick) },
                    )
                }
                if (formulario.carreras.isNotEmpty()) {
                    TextButton(onClick = onLimpiarCarreras, enabled = !guardando) {
                        Text("Limpiar preferencias")
                    }
                }
                RecursosNativos(
                    formulario = formulario,
                    onCambio = onFormularioChange,
                    habilitado = !guardando,
                    onOcupado = { recursoOcupado = it },
                )
                MensajeCampo(errorGuardado)
                Button(
                    onClick = onGuardar,
                    enabled = !guardando && !recursoOcupado,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Icon(
                            IconosFormulario.Check,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (guardando) "Guardando…" else "Guardar prospecto",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                EtiquetaLocal(texto = "Los datos se guardan en este teléfono")
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TarjetaPreferencia(
    numero: Int,
    carrera: String?,
    habilitada: Boolean,
    onQuitar: () -> Unit,
) {
    val fondo by
        animateColorAsState(
            if (carrera != null) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            tween(200),
            label = "Preferencia elegida",
        )
    Surface(
        shape = RoundedCornerShape(17.dp),
        color = fondo,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    "0$numero",
                    Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "${numero}ª preferencia${if (numero > 1) " · opcional" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AnimatedContent(targetState = carrera, label = "Nombre de preferencia $numero") {
                    opcion ->
                    Text(
                        opcion ?: "Sin elegir",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (opcion != null) FontWeight.SemiBold else FontWeight.Normal,
                        color =
                            if (opcion != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (carrera != null) {
                TextButton(
                    onClick = onQuitar,
                    enabled = habilitada,
                    modifier =
                        Modifier.semantics { contentDescription = "Quitar preferencia $numero" },
                ) {
                    Text("Quitar")
                }
            }
        }
    }
}

@Preview(name = "Preferencias", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SelectorSedeScreenPreview() {
    TherionLabsAppTheme(darkTheme = false) {
        SelectorSedeContent(
            formulario =
                FormularioProspectos(
                    region = CatalogoRegional.region,
                    sede = "Viña del Mar",
                    carreras = listOf("Analista Programador"),
                ),
            errores = ErroresFormularioProspectos(),
            guardando = false,
            errorGuardado = null,
            maximoCarreras = 3,
            puedeAgregarCarreras = true,
            puedeElegirSede = true,
            onFormularioChange = {},
            onCarreraClick = {},
            onLimpiarCarreras = {},
            onGuardar = {},
            onAtras = {},
            onVerRegistros = {},
        )
    }
}
