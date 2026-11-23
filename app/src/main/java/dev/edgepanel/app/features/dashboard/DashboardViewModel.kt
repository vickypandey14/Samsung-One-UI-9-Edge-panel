package dev.edgepanel.app.features.dashboard

import android.content.ActivityNotFoundException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.core.datastore.EdgePreferences
import dev.edgepanel.app.core.datastore.EdgePreferencesRepository
import dev.edgepanel.app.core.permissions.OverlayPermissionDataSource
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.OverlaySessionState
import dev.edgepanel.app.overlay.service.EdgeOverlayStarter
import dev.edgepanel.app.overlay.service.OverlaySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val session: OverlaySessionRepository,
    private val preferences: EdgePreferencesRepository,
    private val permissions: OverlayPermissionDataSource,
    private val starter: EdgeOverlayStarter,
    private val logger: EdgeLogger,
) : ViewModel() {
    val sessionState: StateFlow<OverlaySessionState> = session.state
    val preferencesState: StateFlow<EdgePreferences> = preferences.preferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        EdgePreferences(),
    )

    private val _permissionGranted = MutableStateFlow(permissions.canDrawOverlays())
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun refreshPermission() {
        _permissionGranted.value = permissions.canDrawOverlays()
    }

    fun activate() {
        refreshPermission()
        if (!_permissionGranted.value) {
            _message.value = "Allow EDGE to draw over other apps, then try again."
            return
        }
        _message.value = null
        if (!starter.start()) {
            _message.value = "EDGE didn't start because overlay permission is off."
        }
    }

    fun stop() {
        _message.value = null
        starter.stop()
    }

    fun selectEdge(edge: EdgeSide) {
        viewModelScope.launch { preferences.setEdge(edge) }
    }

    fun reportSettingsFailure(error: ActivityNotFoundException) {
        logger.error("EDGE could not open the overlay permission screen.", error)
        _message.value = "Android didn't open the overlay settings screen."
    }
}
