package dev.edgepanel.app.features.onboarding

import android.content.ActivityNotFoundException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.core.datastore.EdgePreferencesRepository
import dev.edgepanel.app.core.permissions.OverlayPermissionDataSource
import dev.edgepanel.app.overlay.service.EdgeOverlayStarter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val permissions: OverlayPermissionDataSource,
    private val preferences: EdgePreferencesRepository,
    private val starter: EdgeOverlayStarter,
    private val logger: EdgeLogger,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private val _settingsError = MutableStateFlow<String?>(null)
    val settingsError: StateFlow<String?> = _settingsError.asStateFlow()

    init {
        samplePermission()
    }

    fun advance() = reduce(OnboardingAction.Advance)

    fun requestSettings() {
        _settingsError.value = null
        reduce(OnboardingAction.RequestOverlaySettings)
    }

    fun settingsLaunchDispatched() = reduce(OnboardingAction.SettingsLaunchDispatched)

    fun samplePermission() = reduce(OnboardingAction.PermissionSampled(permissions.canDrawOverlays()))

    fun useExistingPermission() = reduce(OnboardingAction.UseExistingPermission)

    fun continueFromVerify() = reduce(OnboardingAction.ContinueFromVerify)

    fun notificationGranted() = reduce(OnboardingAction.NotificationGranted)

    fun notificationDenied() = reduce(OnboardingAction.NotificationDenied)

    fun activate() {
        samplePermission()
        if (!_state.value.canDrawOverlays) return
        if (!starter.start()) return
        viewModelScope.launch {
            preferences.setOnboardingComplete(true)
            _finished.value = true
        }
    }

    fun reportSettingsFailure(error: ActivityNotFoundException) {
        logger.error("EDGE could not open the overlay permission screen.", error)
        _settingsError.value = "Android didn't open the overlay settings screen."
    }

    private fun reduce(action: OnboardingAction) {
        _state.update { reduceOnboarding(it, action) }
    }
}
