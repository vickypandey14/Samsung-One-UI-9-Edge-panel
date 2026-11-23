package dev.edgepanel.app.core.permissions

/**
 * Opening the system settings screen is not a grant. Only [canDrawOverlays]
 * from the platform decides that.
 */
enum class OverlayPermissionEvaluation {
    NotYetRequested,
    Granted,
    DeniedAfterSettings,
}

object OverlayPermissionGate {
    fun evaluate(
        canDrawOverlays: Boolean,
        settingsWereOpened: Boolean,
    ): OverlayPermissionEvaluation {
        return when {
            canDrawOverlays -> OverlayPermissionEvaluation.Granted
            settingsWereOpened -> OverlayPermissionEvaluation.DeniedAfterSettings
            else -> OverlayPermissionEvaluation.NotYetRequested
        }
    }
}
