package dev.edgepanel.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayStateMachineTest {
    @Test
    fun activateAttachesHandleOnce() {
        val first = OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.Activated)
        assertEquals(ServicePhase.Active, first.state.servicePhase)
        assertEquals(PanelPhase.Closed, first.state.panelPhase)
        assertEquals(listOf(OverlayEffect.AttachHandle), first.effects)

        val second = OverlayStateMachine.transition(first.state, OverlayEvent.Activated)
        assertTrue(second.effects.isEmpty())
        assertEquals(ServicePhase.Active, second.state.servicePhase)
    }

    @Test
    fun openAndClosePanel() {
        val active = OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.Activated).state
        val opening = OverlayStateMachine.transition(active, OverlayEvent.OpenRequested)
        assertEquals(PanelPhase.Opening, opening.state.panelPhase)
        assertEquals(listOf(OverlayEffect.AnimatePanelOpen), opening.effects)

        val open = OverlayStateMachine.transition(opening.state, OverlayEvent.OpenSettled)
        assertEquals(PanelPhase.Open, open.state.panelPhase)
        assertTrue(open.effects.isEmpty())

        val closing = OverlayStateMachine.transition(open.state, OverlayEvent.CloseRequested)
        assertEquals(PanelPhase.Closing, closing.state.panelPhase)
        assertEquals(listOf(OverlayEffect.AnimatePanelClose), closing.effects)

        val closed = OverlayStateMachine.transition(closing.state, OverlayEvent.CloseSettled)
        assertEquals(PanelPhase.Closed, closed.state.panelPhase)
    }

    @Test
    fun openIsIgnoredWhenInactive() {
        val result = OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.OpenRequested)
        assertEquals(PanelPhase.Closed, result.state.panelPhase)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun closeWhileOpeningReverses() {
        val active = OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.Activated).state
        val opening = OverlayStateMachine.transition(active, OverlayEvent.OpenRequested).state
        val closing = OverlayStateMachine.transition(opening, OverlayEvent.CloseRequested)
        assertEquals(PanelPhase.Closing, closing.state.panelPhase)
        assertEquals(listOf(OverlayEffect.AnimatePanelClose), closing.effects)
    }

    @Test
    fun openWhileClosingReverses() {
        val open = active().copy(panelPhase = PanelPhase.Open)
        val closing = OverlayStateMachine.transition(open, OverlayEvent.CloseRequested).state
        val opening = OverlayStateMachine.transition(closing, OverlayEvent.OpenRequested)
        assertEquals(PanelPhase.Opening, opening.state.panelPhase)
        assertEquals(listOf(OverlayEffect.AnimatePanelOpen), opening.effects)
    }

    @Test
    fun closeWhenClosedIsIgnored() {
        val result = OverlayStateMachine.transition(active(), OverlayEvent.CloseRequested)
        assertEquals(PanelPhase.Closed, result.state.panelPhase)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun settledEventsDoNotJumpPhases() {
        val active = active()
        assertEquals(PanelPhase.Closed, OverlayStateMachine.transition(active, OverlayEvent.OpenSettled).state.panelPhase)
        assertEquals(PanelPhase.Closed, OverlayStateMachine.transition(active, OverlayEvent.CloseSettled).state.panelPhase)
    }

    @Test
    fun stopRemovesWindowsAndClosesPanel() {
        val open = active().copy(panelPhase = PanelPhase.Open)
        val stopped = OverlayStateMachine.transition(open, OverlayEvent.Deactivated)
        assertEquals(ServicePhase.Inactive, stopped.state.servicePhase)
        assertEquals(PanelPhase.Closed, stopped.state.panelPhase)
        assertEquals(listOf(OverlayEffect.DetachAll), stopped.effects)
    }

    @Test
    fun permissionRevocationRemovesWindows() {
        val open = active().copy(panelPhase = PanelPhase.Open)
        val revoked = OverlayStateMachine.transition(open, OverlayEvent.PermissionRevoked)
        assertEquals(ServicePhase.Inactive, revoked.state.servicePhase)
        assertEquals(PanelPhase.Closed, revoked.state.panelPhase)
        assertEquals(OverlayNotice.PermissionRevoked, revoked.state.notice)
        assertEquals(listOf(OverlayEffect.DetachAll), revoked.effects)
    }

    @Test
    fun permissionRevocationWhileInactiveDoesNotDetach() {
        val revoked = OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.PermissionRevoked)
        assertTrue(revoked.effects.isEmpty())
        assertEquals(OverlayNotice.PermissionRevoked, revoked.state.notice)
    }

    @Test
    fun handleFractionIsClampedAndMovesOnlyWhileActive() {
        val inactive = OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.HandleMoved(-1f))
        assertEquals(0f, inactive.state.handleYFraction)
        assertTrue(inactive.effects.isEmpty())

        val active = OverlayStateMachine.transition(active(), OverlayEvent.HandleMoved(2f))
        assertEquals(1f, active.state.handleYFraction)
        assertEquals(listOf(OverlayEffect.ApplyPosition), active.effects)
    }

    @Test
    fun edgeChangeWhileActiveRequestsPositionUpdate() {
        val moved = OverlayStateMachine.transition(active(), OverlayEvent.EdgeChanged(EdgeSide.Left))
        assertEquals(EdgeSide.Left, moved.state.edge)
        assertEquals(listOf(OverlayEffect.ApplyPosition), moved.effects)
    }

    @Test
    fun failureDetachesAndKeepsTheMessage() {
        val failed = OverlayStateMachine.transition(active(), OverlayEvent.Failed("bad token"))
        assertEquals(ServicePhase.Failed, failed.state.servicePhase)
        assertEquals(PanelPhase.Closed, failed.state.panelPhase)
        assertEquals(OverlayNotice.WindowFailed, failed.state.notice)
        assertEquals("bad token", failed.state.failureDetail)
        assertEquals(listOf(OverlayEffect.DetachAll), failed.effects)
        assertFalse(failed.state.servicePhase == ServicePhase.Active)
    }

    private fun active(): OverlaySessionState {
        return OverlayStateMachine.transition(OverlaySessionState(), OverlayEvent.Activated).state
    }
}
