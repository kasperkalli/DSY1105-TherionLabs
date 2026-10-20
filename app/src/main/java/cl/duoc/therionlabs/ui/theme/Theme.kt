package cl.duoc.therionlabs.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val ColoresClaros = lightColorScheme(
    primary = AzulMarino,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulProfundo,
    secondary = Amarillo,
    onSecondary = AzulProfundo,
    secondaryContainer = AmarilloSuave,
    onSecondaryContainer = AzulProfundo,
    tertiary = VerdeConfirmacion,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCF4E9),
    onTertiaryContainer = Color(0xFF0B5145),
    background = FondoClaro,
    onBackground = AzulProfundo,
    surface = Color.White,
    onSurface = AzulProfundo,
    surfaceVariant = AzulSuave,
    onSurfaceVariant = TextoSecundario,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEAF1FA),
    surfaceContainer = Color(0xFFE5EDF7),
    surfaceContainerHigh = Color(0xFFDBE6F3),
    outline = Color(0xFF8495AA),
    outlineVariant = BordeClaro,
    error = Color(0xFFAF2925),
    onError = Color.White,
    errorContainer = Color(0xFFFFE9E5),
    onErrorContainer = Color(0xFF7A1E1B)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFFB4D1F5),
    onPrimary = AzulProfundo,
    primaryContainer = Color(0xFF233F60),
    onPrimaryContainer = Color(0xFFE1EEFF),
    secondary = Amarillo,
    onSecondary = AzulProfundo,
    secondaryContainer = Color(0xFF514323),
    onSecondaryContainer = Color(0xFFFFE5A0),
    tertiary = Color(0xFF9AD8C7),
    onTertiary = Color(0xFF073B31),
    tertiaryContainer = Color(0xFF174E43),
    onTertiaryContainer = Color(0xFFBFF3E3),
    background = Color(0xFF0D1928),
    onBackground = Color(0xFFE5EEFA),
    surface = Color(0xFF14253A),
    onSurface = Color(0xFFE5EEFA),
    surfaceVariant = Color(0xFF203650),
    onSurfaceVariant = Color(0xFFB8C8DD),
    surfaceContainerLowest = Color(0xFF102034),
    surfaceContainerLow = Color(0xFF1B3049),
    surfaceContainer = Color(0xFF203650),
    surfaceContainerHigh = Color(0xFF2A4260),
    outline = Color(0xFF8396AE),
    outlineVariant = Color(0xFF354D69),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF650A0A),
    errorContainer = Color(0xFF512C30),
    onErrorContainer = Color(0xFFFFDAD5)
)

@Composable
fun TherionLabsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colores = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val contexto = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(contexto) else dynamicLightColorScheme(contexto)
        }
        darkTheme -> ColoresOscuros
        else -> ColoresClaros
    }

    MaterialTheme(
        colorScheme = colores,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(30.dp)
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = colores.background) {
            content()
        }
    }
}
