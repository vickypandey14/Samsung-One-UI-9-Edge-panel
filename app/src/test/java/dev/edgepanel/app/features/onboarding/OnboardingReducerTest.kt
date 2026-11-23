package dev.edgepanel.app.features.onboarding

import dev.edgepanel.app.core.permissions.OverlayPermissionEvaluation
import dev.edgepanel.app.core.permissions.OverlayPermissionGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingReducerTest {
    @Test
    fun requestingSettingsDoesNotMarkPermissionGranted() {
        val requested = reduceOnboarding(OnboardingState(step = OnboardingStep.OverlayAccess), OnboardingAction.RequestOverlaySettings)
        assertFalse(requested.canDrawOverlays)
        assertTrue(requested.settingsWereOpened)
        assertTrue(requested.awaitingSettingsResult)
        assertEquals(
            OverlayPermissionEvaluation.DeniedAfterSettings,
            OverlayPermissionGate.evaluate(requested.canDrawOverlays, requested.settingsWereOpened),
        )
    }

    @Test
    fun returningWithoutPermissionLandsOnVerifyDenied() {
        val waiting = reduceOnboarding(
            OnboardingState(step = OnboardingStep.OverlayAccess),
            OnboardingAction.RequestOverlaySettings,
        )
        val returned = reduceOnboarding(waiting, OnboardingAction.PermissionSampled(canDrawOverlays = false))
        assertEquals(OnboardingStep.Verify, returned.step)
        assertFalse(returned.canDrawOverlays)
        assertFalse(returned.awaitingSettingsResult)
        val continued = reduceOnboarding(returned, OnboardingAction.ContinueFromVerify)
        assertEquals(OnboardingStep.Verify, continued.step)
    }

    @Test
    fun returningWithPermissionCanAdvanceOnlyAfterTheCheck() {
        val waiting = reduceOnboarding(
            OnboardingState(step = OnboardingStep.OverlayAccess),
            OnboardingAction.RequestOverlaySettings,
        )
        val returned = reduceOnboarding(waiting, OnboardingAction.PermissionSampled(canDrawOverlays = true))
        assertEquals(OnboardingStep.Verify, returned.step)
        assertTrue(returned.canDrawOverlays)
        val activated = reduceOnboarding(returned, OnboardingAction.ContinueFromVerify)
        assertEquals(OnboardingStep.Activate, activated.step)
    }

    @Test
    fun losingPermissionOnActivateReturnsToVerify() {
        val activate = OnboardingState(step = OnboardingStep.Activate, canDrawOverlays = true)
        val lost = reduceOnboarding(activate, OnboardingAction.PermissionSampled(canDrawOverlays = false))
        assertEquals(OnboardingStep.Verify, lost.step)
        assertFalse(lost.canDrawOverlays)
    }
}
