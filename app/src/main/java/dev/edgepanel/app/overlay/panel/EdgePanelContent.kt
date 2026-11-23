package dev.edgepanel.app.overlay.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.edgepanel.app.R
import dev.edgepanel.app.core.datastore.EdgePreferences
import dev.edgepanel.app.core.designsystem.EdgeSymbolIcon
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.core.designsystem.edgeColorScheme
import dev.edgepanel.app.core.designsystem.symbol
import dev.edgepanel.app.domain.EdgeQuips
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.features.apps.AppGlyph
import dev.edgepanel.app.features.apps.LaunchableApp
import dev.edgepanel.app.features.shortcuts.EdgeShortcut
import dev.edgepanel.app.features.shortcuts.panelLabel

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
    val glass = if (look.panelSolid) 1f else look.panelGlass
    MaterialTheme(colorScheme = edgeColorScheme(look.skin, isSystemInDarkTheme())) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = glass))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), shape)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
            if (look.showShortcuts) {
                val shortcuts = EdgeShortcut.entries.filter { it.name !in look.disabledShortcuts }
                if (shortcuts.isNotEmpty()) {
                    SectionTitle("Shortcuts")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        shortcuts.forEach { shortcut ->
                            ShortcutTile(
                                label = shortcut.panelLabel(torchOn),
                                iconsOnly = look.shortcutIconsOnly,
                                onClick = { onShortcut(shortcut) },
                                icon = { EdgeSymbolIcon(shortcut.symbol(), tint = MaterialTheme.colorScheme.primary) },
                            )
                        }
                    }
                }
            }
            if (look.showClipboard) {
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                ),
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
            }
            if (look.showApps) {
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
}

@Composable
private fun ShortcutTile(
    label: String,
    iconsOnly: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(if (iconsOnly) 56.dp else 76.dp)
            .semantics { if (iconsOnly) contentDescription = label }
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        if (!iconsOnly) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
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

