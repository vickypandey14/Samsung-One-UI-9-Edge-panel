package dev.edgepanel.app.overlay.service

import dev.edgepanel.app.domain.OverlayEffect
import dev.edgepanel.app.domain.OverlayEvent
import dev.edgepanel.app.domain.OverlaySessionState
import dev.edgepanel.app.domain.OverlayStateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide overlay state. Hilt scopes one instance so the activity and
 * the foreground service observe the same session without a global object.
 */
@Singleton
class OverlaySessionRepository @Inject constructor() {
    private val _state = MutableStateFlow(OverlaySessionState())
    val state: StateFlow<OverlaySessionState> = _state.asStateFlow()

    fun dispatch(event: OverlayEvent): List<OverlayEffect> {
        val transition = OverlayStateMachine.transition(_state.value, event)
        _state.value = transition.state
        return transition.effects
    }
}
