package dev.edgepanel.app.domain

enum class ServicePhase {
    Inactive,
    Active,
    Failed,
}

enum class PanelPhase {
    Closed,
    Opening,
    Open,
    Closing,
}

enum class OverlayNotice {
    None,
    PermissionRevoked,
    WindowFailed,
}

data class OverlaySessionState(
    val servicePhase: ServicePhase = ServicePhase.Inactive,
    val panelPhase: PanelPhase = PanelPhase.Closed,
    val edge: EdgeSide = EdgeSide.Right,
    val handleYFraction: Float = 0.5f,
    val notice: OverlayNotice = OverlayNotice.None,
    val failureDetail: String? = null,
)

sealed interface OverlayEvent {
    data object Activated : OverlayEvent
    data object Deactivated : OverlayEvent
    data object OpenRequested : OverlayEvent
    data object CloseRequested : OverlayEvent
    data object OpenSettled : OverlayEvent
    data object CloseSettled : OverlayEvent
    data class HandleMoved(val yFraction: Float) : OverlayEvent
    data class EdgeChanged(val edge: EdgeSide) : OverlayEvent
    data object DisplayChanged : OverlayEvent
    data object PermissionRevoked : OverlayEvent
    data class Failed(val message: String) : OverlayEvent
}

sealed interface OverlayEffect {
    data object AttachHandle : OverlayEffect
    data object AnimatePanelOpen : OverlayEffect
    data object AnimatePanelClose : OverlayEffect
    data object ApplyPosition : OverlayEffect
    data object RefreshDisplay : OverlayEffect
    data object DetachAll : OverlayEffect
}

data class OverlayTransition(
    val state: OverlaySessionState,
    val effects: List<OverlayEffect>,
)
