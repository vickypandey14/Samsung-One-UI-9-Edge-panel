package dev.edgepanel.app.domain

/**
 * Pure session rules for the overlay. Window creation stays in the service.
 */
object OverlayStateMachine {
    fun transition(state: OverlaySessionState, event: OverlayEvent): OverlayTransition {
        return when (event) {
            OverlayEvent.Activated -> activate(state)
            OverlayEvent.Deactivated -> deactivate(state)
            OverlayEvent.OpenRequested -> requestOpen(state)
            OverlayEvent.CloseRequested -> requestClose(state)
            OverlayEvent.OpenSettled -> settleOpen(state)
            OverlayEvent.CloseSettled -> settleClose(state)
            is OverlayEvent.HandleMoved -> moveHandle(state, event.yFraction)
            is OverlayEvent.EdgeChanged -> changeEdge(state, event.edge)
            OverlayEvent.DisplayChanged -> refresh(state)
            OverlayEvent.PermissionRevoked -> permissionRevoked(state)
            is OverlayEvent.Failed -> fail(state, event.message)
        }
    }

    private fun activate(state: OverlaySessionState): OverlayTransition {
        if (state.servicePhase == ServicePhase.Active) {
            return OverlayTransition(state, emptyList())
        }
        return OverlayTransition(
            state.copy(
                servicePhase = ServicePhase.Active,
                panelPhase = PanelPhase.Closed,
                notice = OverlayNotice.None,
                failureDetail = null,
            ),
            listOf(OverlayEffect.AttachHandle),
        )
    }

    private fun deactivate(state: OverlaySessionState): OverlayTransition {
        val effects = if (state.servicePhase == ServicePhase.Active) {
            listOf(OverlayEffect.DetachAll)
        } else {
            emptyList()
        }
        return OverlayTransition(
            state.copy(
                servicePhase = ServicePhase.Inactive,
                panelPhase = PanelPhase.Closed,
                notice = OverlayNotice.None,
                failureDetail = null,
            ),
            effects,
        )
    }

    private fun requestOpen(state: OverlaySessionState): OverlayTransition {
        if (state.servicePhase != ServicePhase.Active) {
            return OverlayTransition(state, emptyList())
        }
        return when (state.panelPhase) {
            PanelPhase.Closed, PanelPhase.Closing -> OverlayTransition(
                state.copy(panelPhase = PanelPhase.Opening, notice = OverlayNotice.None),
                listOf(OverlayEffect.AnimatePanelOpen),
            )
            PanelPhase.Opening, PanelPhase.Open -> OverlayTransition(state, emptyList())
        }
    }

    private fun requestClose(state: OverlaySessionState): OverlayTransition {
        return when (state.panelPhase) {
            PanelPhase.Open, PanelPhase.Opening -> OverlayTransition(
                state.copy(panelPhase = PanelPhase.Closing),
                listOf(OverlayEffect.AnimatePanelClose),
            )
            PanelPhase.Closing, PanelPhase.Closed -> OverlayTransition(state, emptyList())
        }
    }

    private fun settleOpen(state: OverlaySessionState): OverlayTransition {
        if (state.panelPhase != PanelPhase.Opening) return OverlayTransition(state, emptyList())
        return OverlayTransition(state.copy(panelPhase = PanelPhase.Open), emptyList())
    }

    private fun settleClose(state: OverlaySessionState): OverlayTransition {
        if (state.panelPhase != PanelPhase.Closing) return OverlayTransition(state, emptyList())
        return OverlayTransition(state.copy(panelPhase = PanelPhase.Closed), emptyList())
    }

    private fun moveHandle(state: OverlaySessionState, yFraction: Float): OverlayTransition {
        val fraction = yFraction.coerceIn(0f, 1f)
        val effects = if (state.servicePhase == ServicePhase.Active) {
            listOf(OverlayEffect.ApplyPosition)
        } else {
            emptyList()
        }
        return OverlayTransition(state.copy(handleYFraction = fraction), effects)
    }

    private fun changeEdge(state: OverlaySessionState, edge: EdgeSide): OverlayTransition {
        val effects = if (state.servicePhase == ServicePhase.Active) {
            listOf(OverlayEffect.ApplyPosition)
        } else {
            emptyList()
        }
        return OverlayTransition(state.copy(edge = edge), effects)
    }

    private fun refresh(state: OverlaySessionState): OverlayTransition {
        val effects = if (state.servicePhase == ServicePhase.Active) {
            listOf(OverlayEffect.RefreshDisplay)
        } else {
            emptyList()
        }
        return OverlayTransition(state, effects)
    }

    private fun permissionRevoked(state: OverlaySessionState): OverlayTransition {
        val detach = state.servicePhase == ServicePhase.Active || state.panelPhase != PanelPhase.Closed
        return OverlayTransition(
            state.copy(
                servicePhase = ServicePhase.Inactive,
                panelPhase = PanelPhase.Closed,
                notice = OverlayNotice.PermissionRevoked,
                failureDetail = null,
            ),
            if (detach) listOf(OverlayEffect.DetachAll) else emptyList(),
        )
    }

    private fun fail(state: OverlaySessionState, message: String): OverlayTransition {
        val detach = state.servicePhase == ServicePhase.Active
        return OverlayTransition(
            state.copy(
                servicePhase = ServicePhase.Failed,
                panelPhase = PanelPhase.Closed,
                notice = OverlayNotice.WindowFailed,
                failureDetail = message,
            ),
            if (detach) listOf(OverlayEffect.DetachAll) else emptyList(),
        )
    }
}
