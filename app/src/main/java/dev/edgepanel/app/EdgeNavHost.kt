package dev.edgepanel.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.edgepanel.app.core.datastore.EdgePreferencesRepository
import dev.edgepanel.app.features.dashboard.DashboardRoute
import dev.edgepanel.app.features.onboarding.OnboardingRoute
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private object Routes {
    const val LOADING = "loading"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
}

@Composable
fun EdgeNavHost(gate: AppGateViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.LOADING,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        composable(Routes.LOADING) {
            LaunchedEffect(Unit) {
                val destination = gate.destination()
                navController.navigate(destination) {
                    popUpTo(Routes.LOADING) { inclusive = true }
                }
            }
            Box(modifier = Modifier.fillMaxSize())
        }
        composable(Routes.ONBOARDING) {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardRoute()
        }
    }
}

@HiltViewModel
class AppGateViewModel @Inject constructor(
    private val preferences: EdgePreferencesRepository,
) : ViewModel() {
    suspend fun destination(): String {
        val complete = preferences.preferences.first().onboardingComplete
        return if (complete) Routes.DASHBOARD else Routes.ONBOARDING
    }
}
