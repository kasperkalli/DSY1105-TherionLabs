
package cl.duoc.therionlabs.ui.components

import android.util.Log

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import cl.duoc.therionlabs.network.DispositivoTokenStore
import cl.duoc.therionlabs.network.VinculadorDispositivo
import cl.duoc.therionlabs.worker.SyncScheduler

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun VincularDispositivoSection(
    usuarioId: String,
    onVinculacionExitosa: () -> Unit = {}
) {
    val contexto = LocalContext.current

    val vinculador = remember(contexto) {
        VinculadorDispositivo(contexto)
    }

    val tokenStore = remember(contexto) {
        DispositivoTokenStore(contexto)
    }

    val scope = rememberCoroutineScope()

    var codigoVinculado by remember(usuarioId) {
        mutableStateOf(
            tokenStore.obtenerCodigoCaptador(usuarioId)
        )
    }

    var token by remember(usuarioId) {
        mutableStateOf("")
    }

    var verificando by remember {
        mutableStateOf(false)
    }

    var desvinculando by remember {
        mutableStateOf(false)
    }

    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    // -----------------------------------------
    // PANTALLA PRINCIPAL
    // -----------------------------------------

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Vincular dispositivo",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Ingresa la credencial asignada a $usuarioId.",
            style = MaterialTheme.typography.bodyMedium
        )

        // Mostrar dispositivo vinculado
        codigoVinculado?.let { codigo ->

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Dispositivo vinculado",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = "Captador: $codigo"
                    )

                    Text(
                        text = "La vinculación permanece guardada " +
                                "aunque cierres la aplicación.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // -----------------------------------------
        // INGRESAR TOKEN
        // -----------------------------------------

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = {
                Text("Token del dispositivo")
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !verificando && !desvinculando,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                verificando = true
                mensaje = null

                scope.launch {
                    try {
                        vinculador.vincular(
                            usuarioId = usuarioId,
                            token = token
                        )

                        val codigoCaptador =
                            tokenStore.obtenerCodigoCaptador(usuarioId)

                        codigoVinculado = codigoCaptador

                        // Actualizar ViewModel
                        onVinculacionExitosa()

                        // Mantener sincronización automática
                        try {
                            SyncScheduler.solicitarSincronizacion(
                                contexto
                            )
                        } catch (e: Exception) {
                            Log.e(
                                "TherionLabsSync",
                                "No se pudo programar la sincronización",
                                e
                            )
                        }

                        token = ""

                        mensaje = if (codigoCaptador != null) {
                            "Dispositivo vinculado correctamente " +
                                    "como $codigoCaptador."
                        } else {
                            "Dispositivo vinculado, pero no se pudo " +
                                    "recuperar el código del captador."
                        }

                    } catch (e: CancellationException) {
                        throw e

                    } catch (e: Exception) {
                        mensaje = e.message
                            ?: "No se pudo vincular el dispositivo."

                    } finally {
                        verificando = false
                    }
                }
            },
            enabled = token.isNotBlank() &&
                    !verificando &&
                    !desvinculando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (verificando)
                    "Verificando..."
                else
                    "Vincular dispositivo"
            )
        }

        // -----------------------------------------
        // DESVINCULAR DISPOSITIVO
        // -----------------------------------------

        codigoVinculado?.let { codigo ->

            HorizontalDivider()

            Text(
                text = "Administrar vinculación",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = "Captador actual: $codigo"
            )

            OutlinedButton(
                onClick = {
                    mensaje = null
                    mostrarConfirmacion = true
                },
                enabled = !verificando && !desvinculando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Desvincular dispositivo")
            }
        }

        // -----------------------------------------
        // MENSAJES
        // -----------------------------------------

        mensaje?.let { texto ->
            Text(
                text = texto,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // -----------------------------------------
    // CONFIRMACIÓN DE DESVINCULACIÓN
    // -----------------------------------------

    if (mostrarConfirmacion) {

        AlertDialog(
            onDismissRequest = {
                if (!desvinculando) {
                    mostrarConfirmacion = false
                }
            },
            title = {
                Text("Desvincular dispositivo")
            },
            text = {
                Text(
                    "¿Deseas desvincular este dispositivo?\n\n" +
                            "Captador actual: " +
                            "${codigoVinculado ?: "No identificado"}\n\n" +
                            "Se comprobará que no existan " +
                            "prospectos pendientes de sincronización.\n\n" +
                            "Los registros existentes NO serán eliminados.\n\n" +
                            "Esta operación elimina la vinculación local, " +
                            "pero NO revoca el token en Spring Boot."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !desvinculando,
                    onClick = {
                        scope.launch {
                            desvinculando = true
                            mensaje = null

                            try {
                                vinculador.desvincular(usuarioId)

                                codigoVinculado =
                                    tokenStore.obtenerCodigoCaptador(
                                        usuarioId
                                    )

                                token = ""
                                mostrarConfirmacion = false

                                onVinculacionExitosa()

                                mensaje =
                                    "Dispositivo desvinculado correctamente."

                            } catch (e: CancellationException) {
                                throw e

                            } catch (e: Exception) {
                                mostrarConfirmacion = false

                                mensaje = e.message
                                    ?: "No se pudo desvincular el dispositivo."

                            } finally {
                                desvinculando = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (desvinculando)
                            "Desvinculando..."
                        else
                            "Confirmar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !desvinculando,
                    onClick = {
                        mostrarConfirmacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
