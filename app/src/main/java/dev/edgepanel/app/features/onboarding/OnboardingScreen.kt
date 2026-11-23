package dev.edgepanel.app.features.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.core.permissions.OverlayPermissionEvaluation
import dev.edgepanel.app.core.permissions.OverlayPermissionGate
import dev.edgepanel.app.core.permissions.launchOverlaySettings

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    val settingsError by viewModel.settingsError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.samplePermission()
    }
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.notificationGranted() else viewModel.notificationDenied()
    }

    LaunchedEffect(finished) {
        if (finished) onFinished()
    }
    LaunchedEffect(state.settingsLaunchRequested) {
        if (!state.settingsLaunchRequested) return@LaunchedEffect
        viewModel.settingsLaunchDispatched()
        launchOverlaySettings(
            context = context,
            launch = settingsLauncher::launch,
            onFailure = viewModel::reportSettingsFailure,
        )
    }
    LaunchedEffect(state.step) {
        if (state.step != OnboardingStep.Activate || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.notificationGranted()
        } else {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.samplePermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    OnboardingScreen(
        state = state,
        settingsError = settingsError,
        onAdvance = viewModel::advance,
        onRequestSettings = viewModel::requestSettings,
        onUseExisting = viewModel::useExistingPermission,
        onContinue = viewModel::continueFromVerify,
        onActivate = viewModel::activate,
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingState,
    settingsError: String?,
    onAdvance: () -> Unit,
    onRequestSettings: () -> Unit,
    onUseExisting: () -> Unit,
    onContinue: () -> Unit,
    onActivate: () -> Unit,
) {
    val evaluation = OverlayPermissionGate.evaluate(state.canDrawOverlays, state.settingsWereOpened)
    val stepIndex = state.step.ordinal + 1
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = EdgeTokens.screenPaddingDp.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "EDGE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$stepIndex of 5",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = titleFor(state.step, evaluation),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = bodyFor(state, evaluation, settingsError),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state.step) {
                OnboardingStep.Welcome, OnboardingStep.Rationale -> {
                    Button(onClick = onAdvance, modifier = Modifier.width(220.dp)) { Text("Continue") }
                }
                OnboardingStep.OverlayAccess -> {
                    if (state.canDrawOverlays) {
                        Button(onClick = onUseExisting, modifier = Modifier.width(260.dp)) {
                            Text("Overlay access is already on")
                        }
                    } else {
                        Button(onClick = onRequestSettings, modifier = Modifier.width(260.dp)) {
                            Text("Open overlay settings")
                        }
                    }
                }
                OnboardingStep.Verify -> {
                    if (state.canDrawOverlays) {
                        Button(onClick = onContinue, modifier = Modifier.width(220.dp)) { Text("Continue") }
                    } else {
                        Button(onClick = onRequestSettings, modifier = Modifier.width(220.dp)) { Text("Try again") }
                    }
                }
                OnboardingStep.Activate -> {
                    Button(
                        onClick = onActivate,
                        enabled = state.canDrawOverlays,
                        modifier = Modifier.width(220.dp),
                    ) { Text("Activate EDGE") }
                    if (!state.canDrawOverlays) {
                        OutlinedButton(onClick = onRequestSettings) { Text("Review overlay access") }
                    }
                }
            }
        }
    }
}

private fun titleFor(step: OnboardingStep, evaluation: OverlayPermissionEvaluation): String {
    return when (step) {
        OnboardingStep.Welcome -> "Welcome to EDGE"
        OnboardingStep.Rationale -> "EDGE draws over other apps"
        OnboardingStep.OverlayAccess -> "Allow display over other apps"
        OnboardingStep.Verify -> when (evaluation) {
            OverlayPermissionEvaluation.Granted -> "Overlay access is on"
            OverlayPermissionEvaluation.DeniedAfterSettings -> "EDGE still can't draw over apps"
            OverlayPermissionEvaluation.NotYetRequested -> "Check overlay access"
        }
        OnboardingStep.Activate -> "Activate EDGE"
    }
}

private fun bodyFor(
    state: OnboardingState,
    evaluation: OverlayPermissionEvaluation,
    settingsError: String?,
): String {
    val errorSuffix = settingsError?.let { " $it" }.orEmpty()
    return when (state.step) {
        OnboardingStep.Welcome ->
            "A small handle sits on the edge of the screen. Tap or swipe it inward to open your panel while you use other apps."
        OnboardingStep.Rationale ->
            "Android only lets EDGE do that after you allow it to display over other apps. The handle and panel are normal overlay windows, not an accessibility service."
        OnboardingStep.OverlayAccess ->
            if (state.canDrawOverlays) {
                "This phone already allows EDGE to draw over other apps. The next step checks that again before anything starts."
            } else {
                "Open Android's setting for EDGE and turn on \"Allow display over other apps\". Opening that screen does not grant access by itself.$errorSuffix"
            }
        OnboardingStep.Verify -> when (evaluation) {
            OverlayPermissionEvaluation.Granted ->
                "EDGE checked the system setting after you returned. Display over other apps is allowed."
            OverlayPermissionEvaluation.DeniedAfterSettings ->
                "The settings screen was opened, and EDGE is still not allowed to draw over other apps. Turn the switch on for EDGE, then try again.$errorSuffix"
            OverlayPermissionEvaluation.NotYetRequested ->
                "EDGE has not confirmed the system setting yet.$errorSuffix"
        }
        OnboardingStep.Activate -> buildString {
            append("EDGE will show a small handle on the right edge and a notification while it is running. You can stop it from that notification.")
            if (state.notificationDenied) {
                append(" Notifications are off. Android can still show the required EDGE service notification while the overlay is running.")
            }
        }
    }
}
