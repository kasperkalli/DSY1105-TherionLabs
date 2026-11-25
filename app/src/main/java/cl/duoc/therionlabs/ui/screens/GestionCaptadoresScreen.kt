
package cl.duoc.therionlabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import cl.duoc.therionlabs.network.CaptadorRegistradoResponse
import cl.duoc.therionlabs.network.CrearCaptadorRequest
import cl.duoc.therionlabs.network.RetrofitClient

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun GestionCaptadoresScreen(
    onVolver: () -> Unit
) {
    // Datos del nuevo captador
    var nombre by remember { mutableStateOf("") }
    var identificador by remember { mutableStateOf("") }
    var claveAdmin by remember { mutableStateOf("") }

    // Estados de carga
    var registrando by remember { mutableStateOf(false) }
    var cargandoLista by remember { mutableStateOf(false) }

    // Captadores registrados
    var captadores by remember {
        mutableStateOf(
            emptyList<CaptadorRegistradoResponse>()
        )
    }

    // Mensajes
    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeExito by remember {
        mutableStateOf<String?>(null)
    }

    var errorLista by remember {
        mutableStateOf<String?>(null)
    }

    // Captador seleccionado para generar token
    var captadorParaToken by remember {
        mutableStateOf<CaptadorRegistradoResponse?>(null)
    }

    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Gestión de captadores",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Registra captadores, consulta sus datos " +
                    "y administra sus credenciales."
        )

        HorizontalDivider()

        Text(
            text = "Registrar nuevo captador",
            style = MaterialTheme.typography.titleMedium
        )

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            enabled = !registrando && !cargandoLista,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Código CAP-XX
        OutlinedTextField(
            value = identificador,
            onValueChange = {
                identificador = it.uppercase().trim()
            },
            label = {
                Text("Identificador (ej: CAP-05)")
            },
            enabled = !registrando && !cargandoLista,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Clave administrativa
        OutlinedTextField(
            value = claveAdmin,
            onValueChange = { claveAdmin = it },
            label = { Text("Clave administrativa") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !registrando && !cargandoLista,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensajeError = null
                mensajeExito = null

                scope.launch {
                    registrando = true

                    var captadorCreado: String? = null

                    try {
                        val api = RetrofitClient.captadorAdminApi

                        val respuesta = api.registrarCaptador(
                            clave = claveAdmin,
                            datos = CrearCaptadorRequest(
                                identificadorLocal = identificador,
                                nombre = nombre.trim()
                            )
                        )

                        if (!respuesta.isSuccessful) {
                            error(
                                when (respuesta.code()) {
                                    403 -> "Clave administrativa incorrecta."
                                    409 -> "Este captador ya está registrado."
                                    400 -> "Revisa el nombre y el identificador."
                                    else -> "Error HTTP ${respuesta.code()}"
                                }
                            )
                        }

                        val captador = respuesta.body()
                            ?: error(
                                "El servidor no devolvió el captador."
                            )

                        captadorCreado = captador.id

                        // Generar token
                        val respuestaToken = api.generarToken(
                            clave = claveAdmin,
                            captadorId = captador.id
                        )

                        if (!respuestaToken.isSuccessful) {
                            error(
                                "Captador creado, pero falló el token. " +
                                        "HTTP ${respuestaToken.code()}"
                            )
                        }

                        val tokenNuevo = respuestaToken.body()
                            ?.credencial
                            ?.takeIf { it.isNotBlank() }
                            ?: error("No se recibió el token.")

                        clipboard.setText(
                            AnnotatedString(tokenNuevo)
                        )

                        // Actualizar lista visible
                        captadores = (
                                captadores.filterNot {
                                    it.id == captador.id
                                } + captador
                                ).sortedBy {
                                it.identificadorLocal
                            }

                        mensajeExito =
                            "¡Captador registrado correctamente!\n\n" +
                                    "Nombre: ${captador.nombre}\n" +
                                    "Código: ${captador.identificadorLocal}\n\n" +
                                    "Token copiado al portapapeles.\n" +
                                    "Ya puedes entregarlo al captador."

                        nombre = ""
                        identificador = ""

                    } catch (e: CancellationException) {
                        throw e

                    } catch (e: Exception) {
                        mensajeError =
                            if (captadorCreado != null) {
                                "El captador fue creado en MariaDB, " +
                                        "pero no se completó la generación " +
                                        "o copia del token.\n\n" +
                                        "No lo registres nuevamente.\n" +
                                        "Puedes buscarlo en la lista y " +
                                        "generar otra credencial.\n\n" +
                                        "Detalle: ${e.message}"
                            } else {
                                e.message
                                    ?: "No se pudo registrar el captador."
                            }

                    } finally {
                        claveAdmin = ""
                        registrando = false
                    }
                }
            },
            enabled = !registrando &&
                    !cargandoLista &&
                    nombre.isNotBlank() &&
                    identificador.isNotBlank() &&
                    claveAdmin.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (registrando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Registrar y generar token")
            }
        }

        HorizontalDivider()

        Text(
            text = "Captadores registrados",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = {
                scope.launch {
                    cargandoLista = true
                    errorLista = null

                    try {
                        val respuesta =
                            RetrofitClient.captadorAdminApi
                                .listarCaptadores(
                                    clave = claveAdmin
                                )

                        if (respuesta.isSuccessful) {
                            captadores = respuesta.body()
                                .orEmpty()
                                .sortedBy {
                                    it.identificadorLocal
                                }
                        } else {
                            errorLista = when (respuesta.code()) {
                                403 -> "Clave administrativa incorrecta."
                                else -> "Error HTTP ${respuesta.code()}"
                            }
                        }

                    } catch (e: CancellationException) {
                        throw e

                    } catch (e: Exception) {
                        errorLista = e.message
                            ?: "No se pudieron cargar los captadores."

                    } finally {
                        cargandoLista = false
                    }
                }
            },
            enabled = !registrando &&
                    !cargandoLista &&
                    claveAdmin.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cargandoLista)
                    "Cargando captadores..."
                else
                    "Actualizar lista de captadores"
            )
        }

        errorLista?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        captadores.forEach { captador ->

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = captador.identificadorLocal,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = captador.nombre,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = if (captador.activo)
                            "Activo"
                        else
                            "Inactivo",
                        color = if (captador.activo)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )

                    if (captador.activo) {

                        OutlinedButton(
                            onClick = {
                                captadorParaToken = captador
                            },
                            enabled = !registrando &&
                                    !cargandoLista &&
                                    claveAdmin.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Renovar y copiar credencial")
                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = onVolver,
            enabled = !registrando && !cargandoLista,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }

    captadorParaToken?.let { captador ->
        AlertDialog(
            onDismissRequest = {
                if (!registrando) {
                    captadorParaToken = null
                }
            },
            title = {
                Text("Renovar y copiar credencial")
            },

            text = {
                Text(
                    "¿Deseas RENOVAR la credencial de " +
                            "${captador.identificadorLocal}?\n\n" +
                            "Captador: ${captador.nombre}\n\n" +
                            "ATENCIÓN:\n" +
                            "El token anterior dejará de funcionar " +
                            "inmediatamente.\n\n" +
                            "Antes de continuar, verifica en el celular " +
                            "del captador que no tenga prospectos " +
                            "pendientes de sincronización.\n\n" +
                            "El dispositivo deberá vincularse " +
                            "nuevamente con el token generado."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !registrando &&
                            claveAdmin.isNotBlank(),

                    onClick = {

                        captadorParaToken = null
                        mensajeError = null
                        mensajeExito = null

                        scope.launch {
                            registrando = true

                            try {
                                val respuesta =
                                    RetrofitClient.captadorAdminApi
                                        .renovarCredencial(
                                            clave = claveAdmin,
                                            captadorId = captador.id
                                        )


                                if (!respuesta.isSuccessful) {
                                    error(
                                        when (respuesta.code()) {
                                            403 ->
                                                "Clave administrativa incorrecta."

                                            else ->
                                                "Error HTTP ${respuesta.code()}"
                                        }
                                    )
                                }

                                val tokenNuevo = respuesta.body()
                                    ?.credencial
                                    ?.takeIf { it.isNotBlank() }
                                    ?: error(
                                        "No se recibió la credencial."
                                    )
                                clipboard.setText(
                                    AnnotatedString(tokenNuevo)
                                )
                                mensajeExito =
                                    "Nueva credencial generada.\n\n" +
                                            "Captador: " +
                                            "${captador.identificadorLocal}\n\n" +
                                            "Token copiado al portapapeles.\n\n" +
                                            "Ahora puedes entregarlo " +
                                            "manualmente al captador."
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                mensajeError = e.message
                                    ?: "No se pudo generar el token."
                            } finally {
                                claveAdmin = ""
                                registrando = false
                            }
                        }
                    }
                ) {
                    Text("Confirmar renovación")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !registrando,
                    onClick = {
                        captadorParaToken = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    mensajeExito?.let { mensaje ->
        AlertDialog(
            onDismissRequest = {
                mensajeExito = null
            },
            title = {
                Text("¡Operación exitosa!")
            },
            text = {
                Text(mensaje)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensajeExito = null
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
    mensajeError?.let { mensaje ->
        AlertDialog(
            onDismissRequest = {
                mensajeError = null
            },
            title = {
                Text("Aviso de Administración")
            },
            text = {
                Text(mensaje)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensajeError = null
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}
