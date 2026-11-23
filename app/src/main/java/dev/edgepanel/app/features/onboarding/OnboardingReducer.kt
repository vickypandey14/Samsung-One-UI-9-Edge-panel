package dev.edgepanel.app.features.onboarding

enum class OnboardingStep {
    Welcome,
    Rationale,
    OverlayAccess,
    Verify,
    Activate,
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val canDrawOverlays: Boolean = false,
    val settingsWereOpened: Boolean = false,
    val awaitingSettingsResult: Boolean = false,
    val settingsLaunchRequested: Boolean = false,
    val notificationDenied: Boolean = false,
)

sealed interface OnboardingAction {
    data object Advance : OnboardingAction
    data object RequestOverlaySettings : OnboardingAction
    data object SettingsLaunchDispatched : OnboardingAction
    data class PermissionSampled(val canDrawOverlays: Boolean) : OnboardingAction
    data object UseExistingPermission : OnboardingAction
    data object ContinueFromVerify : OnboardingAction
    data object NotificationGranted : OnboardingAction
    data object NotificationDenied : OnboardingAction
}

fun reduceOnboarding(state: OnboardingState, action: OnboardingAction): OnboardingState {
    return when (action) {
        OnboardingAction.Advance -> when (state.step) {
            OnboardingStep.Welcome -> state.copy(step = OnboardingStep.Rationale)
            OnboardingStep.Rationale -> state.copy(step = OnboardingStep.OverlayAccess)
            else -> state
        }

        OnboardingAction.RequestOverlaySettings -> state.copy(
            settingsLaunchRequested = true,
            settingsWereOpened = true,
            awaitingSettingsResult = true,
        )

        OnboardingAction.SettingsLaunchDispatched -> state.copy(settingsLaunchRequested = false)

        is OnboardingAction.PermissionSampled -> applySample(state, action.canDrawOverlays)

        OnboardingAction.UseExistingPermission ->
            if (state.canDrawOverlays && state.step == OnboardingStep.OverlayAccess) {
                state.copy(step = OnboardingStep.Verify)
            } else {
                state
            }

        OnboardingAction.ContinueFromVerify ->
            if (state.canDrawOverlays) {
                state.copy(step = OnboardingStep.Activate)
            } else {
                state
            }

        OnboardingAction.NotificationGranted -> state.copy(notificationDenied = false)
        OnboardingAction.NotificationDenied -> state.copy(notificationDenied = true)
    }
}

private fun applySample(state: OnboardingState, canDrawOverlays: Boolean): OnboardingState {
    val sampled = state.copy(canDrawOverlays = canDrawOverlays)
    return when {
        sampled.awaitingSettingsResult && sampled.step == OnboardingStep.OverlayAccess ->
            sampled.copy(step = OnboardingStep.Verify, awaitingSettingsResult = false)

        sampled.step == OnboardingStep.Verify && sampled.awaitingSettingsResult ->
            sampled.copy(awaitingSettingsResult = false)

        sampled.step == OnboardingStep.Activate && !canDrawOverlays ->
            sampled.copy(step = OnboardingStep.Verify, awaitingSettingsResult = false)

        else -> sampled
    }
}
