package dev.edgepanel.app.features.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.edgepanel.app.R
import dev.edgepanel.app.core.datastore.MAX_BLUR
import dev.edgepanel.app.core.datastore.MAX_HANDLE_HEIGHT
import dev.edgepanel.app.core.datastore.MAX_PANEL_WIDTH
import dev.edgepanel.app.core.datastore.MIN_ALPHA
import dev.edgepanel.app.core.datastore.MIN_GLASS
import dev.edgepanel.app.core.datastore.MIN_HANDLE_HEIGHT
import dev.edgepanel.app.core.datastore.MIN_PANEL_WIDTH
import dev.edgepanel.app.core.designsystem.EdgeSymbol
import dev.edgepanel.app.core.designsystem.EdgeSymbolIcon
import dev.edgepanel.app.core.permissions.launchOverlaySettings
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.EdgeSkin
import dev.edgepanel.app.domain.OverlayNotice
import dev.edgepanel.app.domain.ServicePhase
import dev.edgepanel.app.features.apps.AppGlyph
import dev.edgepanel.app.features.apps.LaunchableApp
import dev.edgepanel.app.features.shortcuts.EdgeShortcut
import dev.edgepanel.app.features.shortcuts.panelLabel

private enum class DashTab(val label: String, val symbol: EdgeSymbol) {
    Home("Home", EdgeSymbol.Home),
    Visibility("Look", EdgeSymbol.Look),
    Style("Style", EdgeSymbol.Style),
    Apps("Apps", EdgeSymbol.Apps),
}

private val EdgeBlue = Color(0xFF2F6FE4)

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
    var tab by remember { mutableStateOf(DashTab.Home) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (tab) {
            DashTab.Home -> Page {
                Text("EDGE", style = MaterialTheme.typography.labelLarge, color = EdgeBlue)
                Text(status, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = if (permissionGranted) "Overlay permission is allowed." else "Overlay permission is off.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (notice != null) {
                    Text(notice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
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
                    ) { Text("Open overlay settings", color = MaterialTheme.colorScheme.onSurface) }
                }
                if (session.servicePhase == ServicePhase.Active) {
                    Button(
                        onClick = viewModel::openPanel,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EdgeBlue, contentColor = Color.White),
                    ) { Text("Open panel") }
                    OutlinedButton(
                        onClick = viewModel::stop,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("Stop EDGE", color = MaterialTheme.colorScheme.onSurface) }
                } else {
                    Button(
                        onClick = viewModel::activate,
                        enabled = permissionGranted,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EdgeBlue, contentColor = Color.White),
                    ) { Text("Activate EDGE") }
                }
                SettingsCard("Edge") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ChoiceButton("Right", prefs.edge == EdgeSide.Right) { viewModel.selectEdge(EdgeSide.Right) }
                        ChoiceButton("Left", prefs.edge == EdgeSide.Left) { viewModel.selectEdge(EdgeSide.Left) }
                    }
                    Text(
                        text = "Tap the handle, or swipe it inward, to open the panel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SettingsCard("Clipboard note") {
                    OutlinedTextField(
                        value = scratch,
                        onValueChange = { next ->
                            scratch = next.take(500)
                            viewModel.setScratch(scratch)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Shown in the panel", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        minLines = 3,
                        colors = fieldColors(),
                    )
                }
            }
            DashTab.Visibility -> Page {
                SettingsCard("Handle") {
                    SwitchRow("Show handle", prefs.handleVisible, viewModel::setHandleVisible)
                    ReleasedSlider(
                        value = prefs.handleAlpha,
                        valueRange = MIN_ALPHA..1f,
                        label = { "Opacity ${(it * 100).toInt()}%" },
                        onFinished = viewModel::setHandleAlpha,
                    )
                    ReleasedSlider(
                        value = prefs.handleHeightDp.toFloat(),
                        valueRange = MIN_HANDLE_HEIGHT.toFloat()..MAX_HANDLE_HEIGHT.toFloat(),
                        label = { "Length ${it.toInt()} dp" },
                        onFinished = { viewModel.setHandleHeight(it.toInt()) },
                    )
                    ReleasedSlider(
                        value = prefs.panelWidthDp.toFloat(),
                        valueRange = MIN_PANEL_WIDTH.toFloat()..MAX_PANEL_WIDTH.toFloat(),
                        label = { "Width ${it.toInt()} dp" },
                        onFinished = { viewModel.setPanelWidth(it.toInt()) },
                    )
                }
                SettingsCard("Panel glass") {
                    SwitchRow("Solid panel", prefs.panelSolid, viewModel::setPanelSolid)
                    Text(
                        text = "Solid turns transparency and blur off. Glass keeps a frosted panel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ReleasedSlider(
                        value = prefs.panelGlass,
                        valueRange = MIN_GLASS..1f,
                        enabled = !prefs.panelSolid,
                        label = { "Depth ${(it * 100).toInt()}%" },
                        onFinished = viewModel::setPanelGlass,
                    )
                    ReleasedSlider(
                        value = prefs.panelBlurDp.toFloat(),
                        valueRange = 0f..MAX_BLUR.toFloat(),
                        enabled = !prefs.panelSolid,
                        label = { "Blur ${it.toInt()} dp" },
                        onFinished = { viewModel.setPanelBlur(it.toInt()) },
                    )
                }
            }
            DashTab.Style -> Page {
                SettingsCard("Look") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EdgeSkin.entries.forEach { skin ->
                            ChoiceButton(skin.name, prefs.skin == skin) { viewModel.setSkin(skin) }
                        }
                    }
                    Text("Panel title", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { next ->
                            title = next.take(24)
                            viewModel.setTitle(title)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors(),
                    )
                    SwitchRow("Funny lines", prefs.showQuips, viewModel::setShowQuips)
                    Text(
                        text = "Boops ${prefs.boops}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = viewModel::resetBoops) {
                        Text("Reset boops", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                SettingsCard("Panel sections") {
                    SwitchRow("Shortcuts", prefs.showShortcuts, viewModel::setShowShortcuts)
                    SwitchRow("Clipboard", prefs.showClipboard, viewModel::setShowClipboard)
                    SwitchRow("Apps", prefs.showApps, viewModel::setShowApps)
                }
                SettingsCard("Shortcuts in the panel") {
                    SwitchRow(
                        label = "Icons only",
                        checked = prefs.shortcutIconsOnly,
                        onCheckedChange = viewModel::setShortcutIconsOnly,
                    )
                    Text(
                        text = "Show shortcut icons without their names.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    EdgeShortcut.entries.forEach { shortcut ->
                        SwitchRow(
                            label = shortcut.panelLabel(),
                            checked = shortcut.name !in prefs.disabledShortcuts,
                            onCheckedChange = { viewModel.setShortcutEnabled(shortcut.name, it) },
                        )
                    }
                }
            }
            DashTab.Apps -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 120.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text("Apps", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pin up to 16. Pinned apps stay at the top of the panel.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Find an app", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            colors = fieldColors(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(filtered, key = { it.component }) { app ->
                        val pinned = app.component in prefs.pinnedApps
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AppGlyph(component = app.component, label = app.label)
                                Text(app.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            ChoiceButton(
                                label = if (pinned) "Pinned" else "Pin",
                                selected = pinned,
                                onClick = { viewModel.setAppPinned(app.component, !pinned) },
                            )
                        }
                    }
                }
            }
        }
        FloatingEdgeBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content,
    )
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        content()
    }
}

@Composable
private fun FloatingEdgeBar(
    selected: DashTab,
    onSelect: (DashTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    val glass = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
    val hairline = Color.White.copy(alpha = 0.16f)
    BoxWithConstraints(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(glass)
            .border(1.dp, hairline, shape)
            .padding(4.dp),
    ) {
        val index = DashTab.entries.indexOf(selected).coerceAtLeast(0)
        val tabWidth = maxWidth / DashTab.entries.size
        val offset by animateDpAsState(
            targetValue = tabWidth * index,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "nav-pill",
        )
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(tabWidth)
                .height(58.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(EdgeBlue),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            DashTab.entries.forEach { tab ->
                val interaction = remember(tab) { MutableInteractionSource() }
                val color = if (tab == selected) Color.White else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clickable(interaction, indication = null) { onSelect(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EdgeSymbolIcon(
                            symbol = tab.symbol,
                            tint = color,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            color = color,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleasedSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    label: (Float) -> String,
    onFinished: (Float) -> Unit,
    enabled: Boolean = true,
) {
    var current by remember(value) { mutableFloatStateOf(value) }
    Text(label(current), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    Slider(
        value = current,
        onValueChange = { current = it },
        onValueChangeFinished = { onFinished(current) },
        valueRange = valueRange,
        enabled = enabled,
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
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = EdgeBlue,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
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
        Button(
            onClick = onClick,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = EdgeBlue, contentColor = Color.White),
        ) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, shape = shape) {
            Text(label, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = EdgeBlue,
    focusedBorderColor = EdgeBlue,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
)
