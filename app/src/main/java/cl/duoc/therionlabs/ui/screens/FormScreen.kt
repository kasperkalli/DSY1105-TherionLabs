package cl.duoc.therionlabs.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cl.duoc.therionlabs.data.model.ErroresFormularioProspectos
import cl.duoc.therionlabs.data.model.FormularioProspectos
import cl.duoc.therionlabs.data.viewmodel.ProspectoViewModel
import cl.duoc.therionlabs.ui.components.*
import cl.duoc.therionlabs.ui.theme.TherionLabsAppTheme


@Composable
fun FormScreen(navController: NavHostController, prospectoViewModel: ProspectoViewModel) {
    val foco = LocalFocusManager.current
    FormScreenContent(
        formulario = prospectoViewModel.formulario,
        errores = prospectoViewModel.erroresFormulario,
        onFormularioChange = prospectoViewModel::actualizarFormulario,
        onSiguiente = {
            foco.clearFocus()
            if (prospectoViewModel.validarDatosPersonales()) {
                navController.navigate("selector_sede") { launchSingleTop = true }
            }
        },
        onVerRegistros = {
            foco.clearFocus()
            navController.navigate("historyscreen") { launchSingleTop = true }
        }
    )
}

@Composable
private fun FormScreenContent(
    formulario: FormularioProspectos,
    errores: ErroresFormularioProspectos,
    onFormularioChange: (FormularioProspectos) -> Unit,
    onSiguiente: () -> Unit,
    onVerRegistros: () -> Unit
) {
    val scroll = rememberScrollState()
    LaunchedEffect(Unit) {
        if (formulario == FormularioProspectos()) scroll.scrollTo(0)
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior("Nuevo prospecto", "Registros", onVerRegistros) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(scroll)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                CabeceraFormulario("Cuéntanos sobre ti", "Completa tus datos y da el primer paso hacia lo que quieres estudiar.", 1)
                TituloSeccion("01", "Datos personales", "Los campos con * son obligatorios.")
                CampoTextoTarjeta("RUT", formulario.rut,
                    { onFormularioChange(formulario.copy(rut = it)) }, IconosFormulario.Identidad,
                    mensajeError = errores.rut, ejemplo = "12.345.678-5", tipoTeclado = KeyboardType.Ascii)
                CampoTextoTarjeta("Nombre", formulario.nombre,
                    { onFormularioChange(formulario.copy(nombre = it)) }, IconosFormulario.Persona,
                    mensajeError = errores.nombre, ejemplo = "Tu nombre")
                CampoTextoTarjeta("Apellidos", formulario.apellidos,
                    { onFormularioChange(formulario.copy(apellidos = it)) }, IconosFormulario.Persona,
                    mensajeError = errores.apellidos, ejemplo = "Tus apellidos")
                CampoTextoTarjeta("Fecha de nacimiento", formulario.fechaNacimiento,
                    { onFormularioChange(formulario.copy(fechaNacimiento = it)) }, IconosFormulario.Calendario,
                    mensajeError = errores.fechaNacimiento, ejemplo = "DD/MM/AAAA")

                TituloSeccion("02", "Cómo contactarte", "Revisa que tu celular y correo estén bien escritos.")
                CampoTextoTarjeta("Celular", formulario.celular,
                    { onFormularioChange(formulario.copy(celular = it)) }, IconosFormulario.Telefono,
                    mensajeError = errores.celular, ejemplo = "+56 9 1234 5678", tipoTeclado = KeyboardType.Phone)
                CampoTextoTarjeta("Correo electrónico", formulario.email,
                    { onFormularioChange(formulario.copy(email = it)) }, IconosFormulario.Correo,
                    mensajeError = errores.email, ejemplo = "nombre@ejemplo.cl", tipoTeclado = KeyboardType.Email)
                CampoTextoTarjeta("Comuna de residencia", formulario.comunaResidencia,
                    { onFormularioChange(formulario.copy(comunaResidencia = it)) }, IconosFormulario.Ubicacion,
                    mensajeError = errores.comunaResidencia, ejemplo = "Comuna donde vives")

                TituloSeccion("03", "Tus estudios", "Queremos conocer tu situación actual.")
                CampoTextoTarjeta("Establecimiento", formulario.establecimiento,
                    { onFormularioChange(formulario.copy(establecimiento = it)) }, IconosFormulario.Edificio,
                    mensajeError = errores.establecimiento, ejemplo = "Nombre de tu establecimiento")
                CampoTextoTarjeta("Comuna del establecimiento", formulario.comunaEstablecimiento,
                    { onFormularioChange(formulario.copy(comunaEstablecimiento = it)) }, IconosFormulario.Ubicacion,
                    mensajeError = errores.comunaEstablecimiento, ejemplo = "Comuna de tu establecimiento")
                CampoSeleccionTarjeta("Actividad actual", formulario.actividadActual,
                    listOf("4to Medio", "Egresado enseñanza media", "Otra"),
                    { onFormularioChange(formulario.copy(actividadActual = it)) }, IconosFormulario.Estudios,
                    mensajeError = errores.actividadActual)
                CampoOpcionesTarjeta("Jornada de estudio", formulario.jornada, listOf("Diurna", "Vespertina"),
                    { onFormularioChange(formulario.copy(jornada = it)) }, IconosFormulario.Reloj,
                    mensajeError = errores.jornada)

                TituloSeccion("04", "Tus preferencias", "Elige cómo estudiar y cómo recibir información.")
                CampoSeleccionTarjeta("Modalidad", formulario.modalidad, listOf("Online", "Presencial"),
                    { onFormularioChange(formulario.copy(modalidad = it)) }, IconosFormulario.Estudios,
                    mensajeError = errores.modalidad)
                CampoSeleccionTarjeta("Canal de contacto", formulario.canalContacto,
                    listOf("Correo electrónico", "Llamada telefónica", "WhatsApp"),
                    { onFormularioChange(formulario.copy(canalContacto = it)) }, IconosFormulario.Correo,
                    mensajeError = errores.canalContacto)

                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .toggleable(value = formulario.autorizaContacto, role = Role.Checkbox,
                            onValueChange = { onFormularioChange(formulario.copy(autorizaContacto = it)) })
                        .padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Checkbox(checked = formulario.autorizaContacto, onCheckedChange = null)
                        Text("Autorizo que me contacten para recibir información académica.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
                MensajeCampo(if (errores.hayErroresDatosPersonales) "Revisa los campos marcados antes de continuar." else null)
                Button(onClick = onSiguiente, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(16.dp)) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(10.dp))
                    Icon(IconosFormulario.Adelante, contentDescription = null, modifier = Modifier.size(21.dp))
                }
                Text("En el siguiente paso elegirás tu sede y tus carreras.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Preview(name = "Registro claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun FormScreenPreview() {
    var formulario by remember { mutableStateOf(FormularioProspectos()) }
    TherionLabsAppTheme(darkTheme = false) {
        FormScreenContent(formulario, ErroresFormularioProspectos(), { formulario = it }, {}, {})
    }
}

@Preview(name = "Registro oscuro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun FormScreenDarkPreview() {
    TherionLabsAppTheme(darkTheme = true) {
        FormScreenContent(FormularioProspectos(), ErroresFormularioProspectos(), {}, {}, {})
    }
}
