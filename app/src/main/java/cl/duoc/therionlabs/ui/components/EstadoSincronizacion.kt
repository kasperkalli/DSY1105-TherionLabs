package cl.duoc.therionlabs.ui.components

import android.util.Log

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import cl.duoc.therionlabs.data.local.AppDatabase
import cl.duoc.therionlabs.network.DispositivoTokenStore

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun EstadoSincronizacionSection(usuarioId: String) {

    val contexto = LocalContext.current

    // Contadores locales
    var pendientes by remember(usuarioId) {
        mutableIntStateOf(0)
    }

    var sincronizados by remember(usuarioId) {
        mutableIntStateOf(0)
    }

    var rechazados by remember(usuarioId) {
        mutableIntStateOf(0)
    }

    var cargando by remember(usuarioId) {
        mutableStateOf(true)
    }

    // -----------------------------------------
    // ACTUALIZAR CONTADORES DESDE ROOM
    // -----------------------------------------

    LaunchedEffect(contexto, usuarioId) {

        val dao = AppDatabase
            .getDatabase(contexto)
            .prospectoDao()

        val tokenStore = DispositivoTokenStore(contexto)

        while (isActive) {

            try {
                val codigoCaptador =
                    tokenStore.obtenerCodigoCaptador(usuarioId)

                if (codigoCaptador != null) {

                    pendientes = dao.contarPendientes(
                        codigoCaptador
                    )

                    sincronizados = dao.contarSincronizados(
                        codigoCaptador
                    )

                    rechazados = dao.contarRechazados(
                        codigoCaptador
                    )

                } else {

                    pendientes = 0
                    sincronizados = 0
                    rechazados = 0
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                Log.e(
                    "TherionLabsSync",
                    "Error al actualizar contadores",
                    error
                )
            }

            cargando = false

            // Actualizar cada 3 segundos
            delay(3000)
        }
    }

    // -----------------------------------------
    // INTERFAZ DE ESTADO
    // -----------------------------------------

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Estado de sincronización",
            style = MaterialTheme.typography.titleMedium
        )

        if (cargando) {

            CircularProgressIndicator()

        } else {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                // Pendientes
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Pendientes",
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = "$pendientes",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                // Sincronizados
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Sincronizados",
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = "$sincronizados",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                // Rechazados
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Rechazados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "$rechazados",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "Los estados corresponden a los prospectos " +
                        "guardados en este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
