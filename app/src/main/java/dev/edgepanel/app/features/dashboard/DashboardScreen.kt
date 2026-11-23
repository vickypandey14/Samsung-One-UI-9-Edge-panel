package dev.edgepanel.app.features.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.edgepanel.app.R
import dev.edgepanel.app.core.datastore.MAX_HANDLE_HEIGHT
import dev.edgepanel.app.core.datastore.MAX_PANEL_WIDTH
import dev.edgepanel.app.core.datastore.MIN_ALPHA
import dev.edgepanel.app.core.datastore.MIN_HANDLE_HEIGHT
import dev.edgepanel.app.core.datastore.MIN_PANEL_WIDTH
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.core.permissions.launchOverlaySettings
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.EdgeSkin
import dev.edgepanel.app.domain.OverlayNotice
import dev.edgepanel.app.domain.ServicePhase
import dev.edgepanel.app.features.apps.AppGlyph
import dev.edgepanel.app.features.apps.LaunchableApp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val session by viewModel.sessionState.collectAsStateWithLifecycle()
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val permissionGranted by viewModel.permissionGranted.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refreshPermission() }
    var query by remember { mutableStateOf("") }
    var title by remember { mutableStateOf(prefs.title) }
    var scratch by remember { mutableStateOf(prefs.scratch) }
    LaunchedEffect(prefs.title) { if (title != prefs.title) title = prefs.title }
    LaunchedEffect(prefs.scratch) { if (scratch != prefs.scratch) scratch = prefs.scratch }

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
    val filtered = apps.filter { app ->
        query.isBlank() || app.label.contains(query, ignoreCase = true)
    }.sortedWith(compareByDescending<LaunchableApp> { it.component in prefs.pinnedApps }.thenBy { it.label.lowercase() })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = EdgeTokens.screenPaddingDp.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "EDGE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = if (permissionGranted) "Overlay permission is allowed." else "Overlay permission is off.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (notice != null) {
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        item {
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
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (session.servicePhase == ServicePhase.Active) {
                Button(
                    onClick = viewModel::openPanel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Open panel") }
                Spacer(modifier = Modifier.height(8.dp))
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
        item {
            SectionLabel("Visibility")
            SwitchRow(
                label = "Show handle",
                checked = prefs.handleVisible,
                onCheckedChange = viewModel::setHandleVisible,
            )
            SliderLabel("Opacity ${(prefs.handleAlpha * 100).toInt()}%")
            Slider(
                value = prefs.handleAlpha,
                onValueChange = viewModel::setHandleAlpha,
                valueRange = MIN_ALPHA..1f,
            )
            SliderLabel("Handle length ${prefs.handleHeightDp}dp")
            Slider(
                value = prefs.handleHeightDp.toFloat(),
                onValueChange = { viewModel.setHandleHeight(it.toInt()) },
                valueRange = MIN_HANDLE_HEIGHT.toFloat()..MAX_HANDLE_HEIGHT.toFloat(),
            )
            SliderLabel("Panel width ${prefs.panelWidthDp}dp")
            Slider(
                value = prefs.panelWidthDp.toFloat(),
                onValueChange = { viewModel.setPanelWidth(it.toInt()) },
                valueRange = MIN_PANEL_WIDTH.toFloat()..MAX_PANEL_WIDTH.toFloat(),
            )
        }
        item {
            SectionLabel("Look")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EdgeSkin.entries.forEach { skin ->
                    ChoiceButton(
                        label = skin.name,
                        selected = prefs.skin == skin,
                        onClick = { viewModel.setSkin(skin) },
                    )
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { next ->
                    title = next.take(24)
                    viewModel.setTitle(title)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Panel title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            SwitchRow(
                label = "Funny lines",
                checked = prefs.showQuips,
                onCheckedChange = viewModel::setShowQuips,
            )
            Text(
                text = "Boops ${prefs.boops}. Each open adds one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = viewModel::resetBoops) { Text("Reset boops") }
        }
        item {
            SectionLabel("Edge")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceButton("Right", prefs.edge == EdgeSide.Right) { viewModel.selectEdge(EdgeSide.Right) }
                ChoiceButton("Left", prefs.edge == EdgeSide.Left) { viewModel.selectEdge(EdgeSide.Left) }
            }
            Text(
                text = "Tap the handle or swipe it inward. Drag it to move it. The outermost bezel still belongs to the system gesture.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            SectionLabel("Clipboard note")
            OutlinedTextField(
                value = scratch,
                onValueChange = { next ->
                    scratch = next.take(500)
                    viewModel.setScratch(scratch)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Shown in the panel") },
                minLines = 2,
            )
        }
        item {
            SectionLabel("Apps")
            Text(
                text = "Pin up to 16. Pinned apps replace the short list in the panel. Leave none pinned and the panel shows the first few launchers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Find an app") },
                singleLine = true,
            )
        }
        items(filtered, key = { it.component }) { app ->
            val pinned = app.component in prefs.pinnedApps
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppGlyph(component = app.component, label = app.label)
                    Text(text = app.label, style = MaterialTheme.typography.bodyLarge)
                }
                ChoiceButton(
                    label = if (pinned) "Pinned" else "Pin",
                    selected = pinned,
                    onClick = { viewModel.setAppPinned(app.component, !pinned) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun SliderLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ChoiceButton(
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
