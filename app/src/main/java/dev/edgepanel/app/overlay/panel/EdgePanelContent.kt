package dev.edgepanel.app.overlay.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.edgepanel.app.R
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.domain.EdgeSide

@Composable
fun EdgePanelContent(
    edge: EdgeSide,
    onClose: () -> Unit,
) {
    val radius = EdgeTokens.panelCornerDp.dp
    val shape = when (edge) {
        EdgeSide.Right -> RoundedCornerShape(topStart = radius, bottomStart = radius)
        EdgeSide.Left -> RoundedCornerShape(topEnd = radius, bottomEnd = radius)
    }
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
                text = "EDGE",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.cd_close_panel))
            }
        }
        Text(
            text = if (edge == EdgeSide.Right) "Right edge" else "Left edge",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(28.dp))
        PlaceholderRow(title = "Shortcuts", detail = "Not in this version")
        PlaceholderRow(title = "Clipboard", detail = "Not in this version")
        PlaceholderRow(title = "Apps", detail = "Not in this version")
    }
}

@Composable
private fun PlaceholderRow(title: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 14.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
