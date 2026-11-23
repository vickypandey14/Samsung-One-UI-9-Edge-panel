package dev.edgepanel.app.overlay.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.edgepanel.app.R
import dev.edgepanel.app.core.datastore.EdgePreferences
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.core.designsystem.edgeColorScheme
import dev.edgepanel.app.domain.EdgeQuips
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.features.apps.AppGlyph
import dev.edgepanel.app.features.apps.LaunchableApp
import dev.edgepanel.app.features.shortcuts.EdgeShortcut

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EdgePanelContent(
    edge: EdgeSide,
    look: EdgePreferences,
    apps: List<LaunchableApp>,
    clipboardStatus: String?,
    torchOn: Boolean,
    onClose: () -> Unit,
    onScratch: (String) -> Unit,
    onScratchFocus: (Boolean) -> Unit,
    onCopyScratch: () -> Unit,
    onReadClipboard: () -> Unit,
    onShortcut: (EdgeShortcut) -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
) {
    val radius = EdgeTokens.panelCornerDp.dp
    val shape = when (edge) {
        EdgeSide.Right -> RoundedCornerShape(topStart = radius, bottomStart = radius)
        EdgeSide.Left -> RoundedCornerShape(topEnd = radius, bottomEnd = radius)
    }
    val pinned = apps.filter { it.component in look.pinnedApps }
    val shownApps = if (pinned.isNotEmpty()) pinned else apps.take(8)
    var scratch by remember { mutableStateOf(look.scratch) }
    LaunchedEffect(look.scratch) {
        if (scratch != look.scratch) scratch = look.scratch
    }
    MaterialTheme(colorScheme = edgeColorScheme(look.skin, isSystemInDarkTheme())) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(EdgeTokens.contentPaddingDp.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = look.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onClose) {
                    Text(stringResource(R.string.cd_close_panel))
                }
            }
            if (look.showQuips) {
                Text(
                    text = EdgeQuips.line(look.boops),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Boops ${look.boops}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Shortcuts")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShortcutButton(if (torchOn) "Light off" else "Light") { onShortcut(EdgeShortcut.Torch) }
                ShortcutButton("Wi-Fi") { onShortcut(EdgeShortcut.Wifi) }
                ShortcutButton("Bluetooth") { onShortcut(EdgeShortcut.Bluetooth) }
                ShortcutButton("Display") { onShortcut(EdgeShortcut.Display) }
                ShortcutButton("Sound") { onShortcut(EdgeShortcut.Sound) }
            }
            Spacer(modifier = Modifier.height(18.dp))
            SectionTitle("Clipboard")
            OutlinedTextField(
                value = scratch,
                onValueChange = { next ->
                    scratch = next.take(500)
                    onScratch(scratch)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onScratchFocus(it.isFocused) },
                label = { Text("Note") },
                minLines = 2,
            )
            Row {
                TextButton(onClick = onCopyScratch) { Text("Copy") }
                TextButton(onClick = onReadClipboard) { Text("Read") }
            }
            if (clipboardStatus != null) {
                Text(
                    text = clipboardStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(if (pinned.isNotEmpty()) "Pinned apps" else "Apps")
            if (shownApps.isEmpty()) {
                Text(
                    text = "No launcher apps were visible to EDGE.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            shownApps.forEach { app ->
                TextButton(
                    onClick = { onLaunchApp(app) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AppGlyph(component = app.component, label = app.label)
                        Text(text = app.label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            if (pinned.isEmpty() && apps.size > shownApps.size) {
                Text(
                    text = "Pin apps in EDGE to keep them here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ShortcutButton(
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick) { Text(label) }
}
