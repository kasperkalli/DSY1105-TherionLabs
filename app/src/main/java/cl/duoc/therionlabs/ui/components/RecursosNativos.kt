package cl.duoc.therionlabs.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cl.duoc.therionlabs.util.DireccionUbicacion
import cl.duoc.therionlabs.data.model.FormularioProspectos
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun RecursosNativos(
    formulario: FormularioProspectos,
    onCambio: (FormularioProspectos) -> Unit,
    habilitado: Boolean,
    onOcupado: (Boolean) -> Unit
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val actual by rememberUpdatedState(formulario)
    val cambiar by rememberUpdatedState(onCambio)
    var pendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var solicitandoPermiso by remember { mutableStateOf(false) }
    var mensaje by rememberSaveable { mutableStateOf<String?>(null) }
    val ocupado = pendiente != null || buscando || solicitandoPermiso

    LaunchedEffect(ocupado) {
        onOcupado(ocupado)
    }

    DisposableEffect(Unit) {
        onDispose { onOcupado(false) }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = pendiente
        pendiente = null
        if (exito && ruta != null && File(ruta).length() > 0) {
            cambiar(actual.copy(fotoRuta = ruta))
            mensaje = "Foto adjuntada. Se conservará al guardar el prospecto."
        } else {
            ruta?.let { File(it).delete() }
            mensaje = "No se tomó una foto. Puedes continuar sin ella."
        }
    }

    fun obtenerUbicacion() {
        buscando = true
        mensaje = null
        scope.launch {
            try {
                val ubicacion = withTimeoutOrNull(20000L) { ubicacionActual(contexto) }
                if (ubicacion == null) {
                    mensaje = "No se obtuvo ubicación en 20 segundos. Activa la ubicación e inténtalo otra vez."
                } else {
                    cambiar(
                        actual.copy(
                            latitud = ubicacion.latitude,
                            longitud = ubicacion.longitude,
                            precisionMetros = if (ubicacion.hasAccuracy()) ubicacion.accuracy else null,
                            fechaUbicacion = ubicacion.time,
                            direccion = null
                        )
                    )
                    mensaje = "Ubicación obtenida. Buscando dirección aproximada…"
                    val direccion = DireccionUbicacion.buscar(
                        contexto.applicationContext,
                        ubicacion.latitude,
                        ubicacion.longitude
                    )
                    cambiar(
                        actual.copy(
                            latitud = ubicacion.latitude,
                            longitud = ubicacion.longitude,
                            precisionMetros = if (ubicacion.hasAccuracy()) ubicacion.accuracy else null,
                            fechaUbicacion = ubicacion.time,
                            direccion = direccion
                        )
                    )
                    mensaje = if (direccion != null) {
                        "Dirección aproximada adjuntada junto a las coordenadas."
                    } else {
                        "No se encontró una dirección. Las coordenadas quedaron adjuntadas."
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                mensaje = "No fue posible obtener la ubicación. Revisa los permisos y la ubicación del dispositivo."
            } finally {
                buscando = false
            }
        }
    }

    val permisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        solicitandoPermiso = false
        if (tieneUbicacion(contexto)) {
            obtenerUbicacion()
        } else {
            mensaje = "Permiso de ubicación rechazado. Puedes guardar sin ubicación."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Foto y ubicación · opcionales",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Agrega una foto del registro y la ubicación de la captura solo si corresponde. " +
                "Se guardan en este dispositivo.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(
            enabled = habilitado && !ocupado,
            onClick = {
                mensaje = null
                var archivo: File? = null
                try {
                    val carpeta = File(contexto.filesDir, "fotos_prospectos").apply { mkdirs() }
                    archivo = File.createTempFile("captura_", ".jpg", carpeta)
                    val uri = FileProvider.getUriForFile(
                        contexto,
                        contexto.packageName + ".nativefiles",
                        archivo
                    )
                    pendiente = archivo.absolutePath
                    camara.launch(uri)
                } catch (_: Exception) {
                    pendiente = null
                    archivo?.delete()
                    mensaje = "No se pudo abrir una aplicación de cámara. Puedes continuar sin foto."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (formulario.fotoRuta == null) "Tomar foto" else "Reemplazar foto")
        }

        OutlinedButton(
            enabled = habilitado && !ocupado,
            onClick = {
                if (tieneUbicacion(contexto)) {
                    obtenerUbicacion()
                } else {
                    solicitandoPermiso = true
                    permisos.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (buscando) "Obteniendo ubicación…" else "Obtener ubicación actual")
        }

        if (buscando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        DetalleRecursosNativos(
            fotoRuta = formulario.fotoRuta,
            latitud = formulario.latitud,
            longitud = formulario.longitud,
            precision = formulario.precisionMetros,
            fecha = formulario.fechaUbicacion,
            direccion = formulario.direccion
        )
        if (formulario.fotoRuta != null || formulario.latitud != null) {
            TextButton(
                enabled = habilitado && !ocupado,
                onClick = {
                    cambiar(
                        actual.copy(
                            fotoRuta = null,
                            latitud = null,
                            longitud = null,
                            precisionMetros = null,
                            fechaUbicacion = null,
                            direccion = null
                        )
                    )
                    mensaje = "Adjuntos retirados del borrador."
                }
            ) {
                Text("Quitar foto y ubicación")
            }
        }
        mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun DetalleRecursosNativos(
    fotoRuta: String?,
    latitud: Double?,
    longitud: Double?,
    precision: Float?,
    fecha: Long?,
    direccion: String? = null
) {
    if (fotoRuta != null) {
        val foto by produceState<ImageBitmap?>(null, fotoRuta) {
            value = withContext(Dispatchers.IO) {
                try {
                    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(fotoRuta, medidas)
                    var escala = 1
                    while (maxOf(medidas.outWidth, medidas.outHeight) / escala > 1000) {
                        escala *= 2
                    }
                    val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
                    BitmapFactory.decodeFile(fotoRuta, opciones)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }
        }
        val imagen = foto
        if (imagen != null) {
            Image(
                bitmap = imagen,
                contentDescription = "Foto adjunta del registro",
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
        } else {
            Text("Foto adjunta no disponible", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (latitud != null && longitud != null) {
        Text(
            text = "Dirección aproximada: ${direccion ?: "No disponible; se conservan las coordenadas"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = String.format(Locale.ROOT, "Ubicación de captura: %.6f, %.6f", latitud, longitud),
            style = MaterialTheme.typography.bodySmall
        )
        precision?.let {
            Text("Precisión aproximada: ${it.toInt()} m", style = MaterialTheme.typography.bodySmall)
        }
        fecha?.let {
            Text(
                text = "Capturada: ${DateFormat.getDateTimeInstance().format(Date(it))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun tieneUbicacion(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(
        contexto, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        contexto, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private suspend fun ubicacionActual(contexto: Context): Location? = suspendCancellableCoroutine { continuacion ->
    val gestor = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val preciso = ContextCompat.checkSelfPermission(
        contexto, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val proveedores = listOfNotNull(
        LocationManager.GPS_PROVIDER.takeIf { preciso && gestor.isProviderEnabled(it) },
        LocationManager.NETWORK_PROVIDER.takeIf { gestor.isProviderEnabled(it) }
    )
    if (proveedores.isEmpty()) {
        continuacion.resume(null)
    } else {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (continuacion.isActive) {
                    gestor.removeUpdates(this)
                    continuacion.resume(location)
                }
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Compatibilidad con Android 8")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        continuacion.invokeOnCancellation { gestor.removeUpdates(listener) }
        try {
            proveedores.forEach { proveedor ->
                gestor.requestLocationUpdates(proveedor, 0L, 0f, listener, Looper.getMainLooper())
            }
        } catch (error: Exception) {
            gestor.removeUpdates(listener)
            if (continuacion.isActive) continuacion.resume(null)
        }
    }
}
