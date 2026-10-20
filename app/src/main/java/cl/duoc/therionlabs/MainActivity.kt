package cl.duoc.therionlabs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.therionlabs.data.viewmodel.ProspectoViewModel
import cl.duoc.therionlabs.ui.screens.FormScreen
import cl.duoc.therionlabs.ui.screens.HistoryScreen
import cl.duoc.therionlabs.ui.screens.SelectorSedeScreen
import cl.duoc.therionlabs.ui.theme.TherionLabsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TherionLabsAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val prospectoViewModel: ProspectoViewModel = viewModel(
        factory = ProspectoViewModel.Factory
    )
    val registroGuardadoId = prospectoViewModel.registroGuardadoId

    NavHost(
        navController = navController,
        startDestination = "formscreen",
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            fadeIn(tween(240)) + slideInHorizontally(tween(240)) { it / 14 }
        },
        exitTransition = { fadeOut(tween(140)) },
        popEnterTransition = {
            fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 14 }
        },
        popExitTransition = {
            fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { it / 14 }
        }
    ) {
        composable(route = "formscreen") {
            FormScreen(
                navController = navController,
                prospectoViewModel = prospectoViewModel
            )
        }
        composable(route = "selector_sede") {
            SelectorSedeScreen(
                navController = navController,
                prospectoViewModel = prospectoViewModel
            )
        }
        composable(route = "historyscreen") {
            HistoryScreen(
                navController = navController,
                prospectoViewModel = prospectoViewModel
            )
        }
    }

    // Se abre el historial después de que Room confirma el guardado.
    LaunchedEffect(registroGuardadoId) {
        if (registroGuardadoId != null) {
            navController.navigate("historyscreen") {
                popUpTo("formscreen") { inclusive = false }
                launchSingleTop = true
            }
            prospectoViewModel.consumirConfirmacionGuardado()
        }
    }
}
