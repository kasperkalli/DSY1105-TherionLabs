package cl.duoc.therionlabs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.therionlabs.ui.theme.Amarillo
import cl.duoc.therionlabs.ui.theme.AzulMarino

@Composable
fun AccesoScreen(ocupado: Boolean, error: String?, ingresar: (String, String) -> Unit) {
    var usuario by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var mostrarClave by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Surface(color = AzulMarino, shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    Modifier.fillMaxWidth().padding(26.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "THERIONLABS",
                        color = Amarillo,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "Cada encuentro, una oportunidad",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        "Captación y seguimiento · Región de Valparaíso",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
            Text("Ingresa para acceder a las funciones de tu perfil.")
            OutlinedTextField(
                usuario,
                { usuario = it },
                label = { Text("Usuario") },
                singleLine = true,
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                clave,
                { clave = it },
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation =
                    if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { mostrarClave = !mostrarClave }) {
                        Text(if (mostrarClave) "Ocultar" else "Mostrar")
                    }
                },
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { ingresar(usuario, clave) },
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            ) {
                if (ocupado) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(if (ocupado) "Ingresando…" else "Ingresar")
            }
            Text(
                "Los eventos y registros se comparten entre los perfiles de este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
