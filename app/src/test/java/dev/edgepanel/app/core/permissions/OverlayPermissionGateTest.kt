package dev.edgepanel.app.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayPermissionGateTest {
    @Test
    fun openingSettingsDoesNotCountAsGranted() {
        val evaluation = OverlayPermissionGate.evaluate(
            canDrawOverlays = false,
            settingsWereOpened = true,
        )
        assertEquals(OverlayPermissionEvaluation.DeniedAfterSettings, evaluation)
    }

    @Test
    fun onlyThePlatformFlagGrantsAccess() {
        assertEquals(
            OverlayPermissionEvaluation.Granted,
            OverlayPermissionGate.evaluate(canDrawOverlays = true, settingsWereOpened = false),
        )
        assertEquals(
            OverlayPermissionEvaluation.Granted,
            OverlayPermissionGate.evaluate(canDrawOverlays = true, settingsWereOpened = true),
        )
    }

    @Test
    fun untouchedPermissionStaysUnrequested() {
        assertEquals(
            OverlayPermissionEvaluation.NotYetRequested,
            OverlayPermissionGate.evaluate(canDrawOverlays = false, settingsWereOpened = false),
        )
    }
}
