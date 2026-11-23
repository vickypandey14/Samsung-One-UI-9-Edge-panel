package dev.edgepanel.app.features.dashboard

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.edgepanel.app.R
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.core.permissions.launchOverlaySettings
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.OverlayNotice
import dev.edgepanel.app.domain.ServicePhase

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val session by viewModel.sessionState.collectAsStateWithLifecycle()
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val permissionGranted by viewModel.permissionGranted.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refreshPermission() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notice = when (session.notice) {
        OverlayNotice.PermissionRevoked -> stringResource(R.string.permission_revoked)
        OverlayNotice.WindowFailed -> buildString {
            append(stringResource(R.string.window_failed))
            session.failureDetail?.let { append(" ").append(it) }
        }
        OverlayNotice.None -> message
    }
    val status = when (session.servicePhase) {
        ServicePhase.Active -> "EDGE is active"
        ServicePhase.Inactive -> "EDGE is stopped"
        ServicePhase.Failed -> "EDGE stopped after an error"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(EdgeTokens.screenPaddingDp.dp),
    ) {
        Text(
            text = "EDGE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (permissionGranted) {
                "Overlay permission is allowed."
            } else {
                "Overlay permission is off."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (notice != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = notice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Handle edge",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EdgeButton(
                label = "Right",
                selected = prefs.edge == EdgeSide.Right,
                onClick = { viewModel.selectEdge(EdgeSide.Right) },
            )
            EdgeButton(
                label = "Left",
                selected = prefs.edge == EdgeSide.Left,
                onClick = { viewModel.selectEdge(EdgeSide.Left) },
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Leave this screen and use the handle on the screen edge. Tap or swipe it inward to open the panel. Drag it up or down to move it. Android's back gesture still starts from the screen edge outside the handle.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!permissionGranted) {
            OutlinedButton(
                onClick = {
                    launchOverlaySettings(
                        context = context,
                        launch = settingsLauncher::launch,
                        onFailure = viewModel::reportSettingsFailure,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Open overlay settings") }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (session.servicePhase == ServicePhase.Active) {
            OutlinedButton(
                onClick = viewModel::stop,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Stop EDGE") }
        } else {
            Button(
                onClick = viewModel::activate,
                enabled = permissionGranted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Activate EDGE") }
        }
    }
}

@Composable
private fun EdgeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    if (selected) {
        Button(onClick = onClick, shape = shape) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, shape = shape) { Text(label) }
    }
}
