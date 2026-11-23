package dev.edgepanel.app.features.apps

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AppGlyph(
    component: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val painter = remember(component) {
        val name = ComponentName.unflattenFromString(component) ?: return@remember null
        val drawable = try {
            context.packageManager.getActivityIcon(name)
        } catch (error: RuntimeException) {
            null
        } ?: return@remember null
        BitmapPainter(drawable.toBitmap(width = 96, height = 96).asImageBitmap())
    }
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = label,
            modifier = modifier.size(28.dp),
        )
    }
}
