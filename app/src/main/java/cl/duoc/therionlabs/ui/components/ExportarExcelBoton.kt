package cl.duoc.therionlabs.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.therionlabs.network.ExcelDownloader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ExportarExcelBoton(
    eventoId: String,
    ocupado: Boolean
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    var mostrarDialogo by remember {
        mutableStateOf(false)
    }

    var clave by remember {
        mutableStateOf("")
    }

    var descargando by remember {
        mutableStateOf(false)
    }

    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->

        if (uri == null) {
            clave = ""
        } else {
            descargando = true

            alcance.launch {
                try {
                    ExcelDownloader.descargarYGuardar(
                        contexto = contexto,
                        eventoId = eventoId,
                        claveAdministrativa = clave,
                        destino = uri
                    )

                    Toast.makeText(
                        contexto,
                        "Excel descargado correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: CancellationException) {
                    throw e

                } catch (e: Exception) {
                    Toast.makeText(
                        contexto,
                        e.message ?: "Error al descargar Excel",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {
                    clave = ""
                    descargando = false
                }
            }
        }
    }

    Button(
        onClick = {
            mostrarDialogo = true
        },
        enabled = !ocupado && !descargando,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (descargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text("Exportar Excel")
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                clave = ""
                mostrarDialogo = false
            },
            title = {
                Text("Exportar reporte Excel")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Ingresa tu clave administrativa para descargar el reporte."
                    )

                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = {
                            Text("Clave administrativa")
                        },
                        visualTransformation =
                            PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = clave.isNotBlank(),
                    onClick = {
                        mostrarDialogo = false

                        selectorArchivo.launch(
                            "TherionLabs_Evento_${eventoId}.xlsx"
                        )
                    }
                ) {
                    Text("Descargar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        clave = ""
                        mostrarDialogo = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
