package dev.edgepanel.app.overlay.handle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.edgepanel.app.core.designsystem.EdgePalette
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.domain.EdgeSide

@Composable
fun EdgeHandleFace(edge: EdgeSide) {
    val shape = RoundedCornerShape(EdgeTokens.handleCornerDp.dp)
    val color = if (isSystemInDarkTheme()) EdgePalette.handleDark else EdgePalette.handleLight
    val stroke = if (isSystemInDarkTheme()) EdgePalette.handleStrokeDark else EdgePalette.handleStrokeLight
    val alignment = if (edge == EdgeSide.Left) Alignment.CenterStart else Alignment.CenterEnd
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .width(EdgeTokens.handleWidthDp.dp)
                .fillMaxHeight()
                .clip(shape)
                .background(color)
                .border(1.dp, stroke, shape),
        )
    }
}
