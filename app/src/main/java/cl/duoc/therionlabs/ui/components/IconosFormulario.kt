package cl.duoc.therionlabs.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Íconos vectoriales propios. No requieren imágenes ni dependencias adicionales.
object IconosFormulario {
    val Persona = icono("Persona") {
        moveTo(8f, 7f); curveTo(8f, 2f, 16f, 2f, 16f, 7f)
        curveTo(16f, 12f, 8f, 12f, 8f, 7f)
        moveTo(4f, 21f); lineTo(4f, 18f); curveTo(4f, 11f, 20f, 11f, 20f, 18f); lineTo(20f, 21f)
    }
    val Identidad = icono("Identidad") {
        moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close()
        moveTo(6f, 10f); curveTo(6f, 7f, 11f, 7f, 11f, 10f); curveTo(11f, 13f, 6f, 13f, 6f, 10f)
        moveTo(5f, 16f); curveTo(6f, 13f, 11f, 13f, 12f, 16f)
        moveTo(15f, 9f); lineTo(18f, 9f); moveTo(15f, 13f); lineTo(18f, 13f)
    }
    val Calendario = icono("Calendario") {
        moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 21f); lineTo(4f, 21f); close()
        moveTo(8f, 3f); lineTo(8f, 7f); moveTo(16f, 3f); lineTo(16f, 7f)
        moveTo(4f, 10f); lineTo(20f, 10f); moveTo(8f, 14f); lineTo(10f, 14f)
        moveTo(14f, 14f); lineTo(16f, 14f); moveTo(8f, 17f); lineTo(10f, 17f)
    }
    val Telefono = icono("Telefono") {
        moveTo(8f, 3f); lineTo(16f, 3f); quadTo(18f, 3f, 18f, 5f)
        lineTo(18f, 19f); quadTo(18f, 21f, 16f, 21f); lineTo(8f, 21f)
        quadTo(6f, 21f, 6f, 19f); lineTo(6f, 5f); quadTo(6f, 3f, 8f, 3f)
        moveTo(10f, 6f); lineTo(14f, 6f); moveTo(11f, 18f); lineTo(13f, 18f)
    }
    val Correo = icono("Correo") {
        moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close()
        moveTo(3f, 6f); lineTo(12f, 13f); lineTo(21f, 6f)
    }
    val Ubicacion = icono("Ubicacion") {
        moveTo(12f, 22f); curveTo(8f, 17f, 4f, 13f, 4f, 9f)
        curveTo(4f, -1f, 20f, -1f, 20f, 9f); curveTo(20f, 13f, 16f, 17f, 12f, 22f)
        moveTo(9f, 9f); curveTo(9f, 5f, 15f, 5f, 15f, 9f); curveTo(15f, 13f, 9f, 13f, 9f, 9f)
    }
    val Estudios = icono("Estudios") {
        moveTo(2f, 8f); lineTo(12f, 3f); lineTo(22f, 8f); lineTo(12f, 13f); close()
        moveTo(6f, 10f); lineTo(6f, 17f); quadTo(12f, 22f, 18f, 17f); lineTo(18f, 10f)
        moveTo(22f, 8f); lineTo(22f, 16f)
    }
    val Edificio = icono("Edificio") {
        moveTo(5f, 21f); lineTo(5f, 3f); lineTo(19f, 3f); lineTo(19f, 21f)
        moveTo(3f, 21f); lineTo(21f, 21f); moveTo(9f, 7f); lineTo(10f, 7f)
        moveTo(14f, 7f); lineTo(15f, 7f); moveTo(9f, 11f); lineTo(10f, 11f)
        moveTo(14f, 11f); lineTo(15f, 11f); moveTo(10f, 21f); lineTo(10f, 16f); lineTo(14f, 16f); lineTo(14f, 21f)
    }
    val Reloj = icono("Reloj") {
        moveTo(12f, 3f); curveTo(24f, 3f, 24f, 21f, 12f, 21f); curveTo(0f, 21f, 0f, 3f, 12f, 3f)
        moveTo(12f, 7f); lineTo(12f, 12f); lineTo(16f, 14f)
    }
    val Nota = icono("Nota") {
        moveTo(5f, 3f); lineTo(15f, 3f); lineTo(20f, 8f); lineTo(20f, 21f); lineTo(5f, 21f); close()
        moveTo(15f, 3f); lineTo(15f, 8f); lineTo(20f, 8f)
        moveTo(9f, 12f); lineTo(16f, 12f); moveTo(9f, 16f); lineTo(14f, 16f)
    }
    val Interes = icono("Interes") {
        moveTo(12f, 21f); lineTo(4f, 13f); curveTo(-2f, 5f, 7f, 0f, 12f, 7f)
        curveTo(17f, 0f, 26f, 5f, 20f, 13f); close()
    }
    val Check = icono("Check") { moveTo(5f, 12f); lineTo(10f, 17f); lineTo(20f, 6f) }
    val Adelante = icono("Adelante") {
        moveTo(4f, 12f); lineTo(20f, 12f); moveTo(14f, 6f); lineTo(20f, 12f); lineTo(14f, 18f)
    }
    val Atras = icono("Atras") {
        moveTo(20f, 12f); lineTo(4f, 12f); moveTo(10f, 6f); lineTo(4f, 12f); lineTo(10f, 18f)
    }
    val Abajo = icono("Abajo") { moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f) }
    val Registros = icono("Registros") {
        moveTo(5f, 3f); lineTo(19f, 3f); lineTo(19f, 21f); lineTo(5f, 21f); close()
        moveTo(9f, 8f); lineTo(15f, 8f); moveTo(9f, 12f); lineTo(15f, 12f); moveTo(9f, 16f); lineTo(13f, 16f)
    }
    val Aviso = icono("Aviso") {
        moveTo(12f, 3f); lineTo(22f, 21f); lineTo(2f, 21f); close()
        moveTo(12f, 9f); lineTo(12f, 14f); moveTo(12f, 17f); lineTo(12f, 17.2f)
    }
    val Candado = icono("Candado") {
        moveTo(5f, 10f); lineTo(19f, 10f); lineTo(19f, 21f); lineTo(5f, 21f); close()
        moveTo(8f, 10f); lineTo(8f, 6f); curveTo(8f, 1f, 16f, 1f, 16f, 6f); lineTo(16f, 10f)
        moveTo(12f, 14f); lineTo(12f, 17f)
    }

    private fun icono(nombre: String, dibujo: PathBuilder.() -> Unit): ImageVector {
        return ImageVector.Builder(nombre, 24.dp, 24.dp, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) { dibujo() }
        }.build()
    }
}
