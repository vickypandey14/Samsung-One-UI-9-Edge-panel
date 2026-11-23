package dev.edgepanel.app.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.edgepanel.app.features.shortcuts.EdgeShortcut

enum class EdgeSymbol {
    Home,
    Look,
    Style,
    Apps,
    Torch,
    Wifi,
    Bluetooth,
    Display,
    Sound,
    Location,
    Battery,
    Notifications,
    Settings,
    Clock,
    Flip,
    Airplane,
    Data,
    Wallpaper,
    Camera,
    Phone,
    Messages,
}

fun EdgeShortcut.symbol(): EdgeSymbol {
    return when (this) {
        EdgeShortcut.Torch -> EdgeSymbol.Torch
        EdgeShortcut.Wifi -> EdgeSymbol.Wifi
        EdgeShortcut.Bluetooth -> EdgeSymbol.Bluetooth
        EdgeShortcut.Display -> EdgeSymbol.Display
        EdgeShortcut.Sound -> EdgeSymbol.Sound
        EdgeShortcut.Location -> EdgeSymbol.Location
        EdgeShortcut.Battery -> EdgeSymbol.Battery
        EdgeShortcut.Notifications -> EdgeSymbol.Notifications
        EdgeShortcut.Settings -> EdgeSymbol.Settings
        EdgeShortcut.Clock -> EdgeSymbol.Clock
        EdgeShortcut.Flip -> EdgeSymbol.Flip
        EdgeShortcut.Airplane -> EdgeSymbol.Airplane
        EdgeShortcut.Data -> EdgeSymbol.Data
        EdgeShortcut.Wallpaper -> EdgeSymbol.Wallpaper
        EdgeShortcut.Camera -> EdgeSymbol.Camera
        EdgeShortcut.Phone -> EdgeSymbol.Phone
        EdgeShortcut.Messages -> EdgeSymbol.Messages
    }
}

@Composable
fun EdgeSymbolIcon(
    symbol: EdgeSymbol,
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Canvas(modifier.size(24.dp)) {
        val unit = size.minDimension / 24f
        val stroke = Stroke(width = 1.7f * unit, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        fun at(x: Float, y: Float) = Offset(x * unit, y * unit)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(tint, at(x1, y1), at(x2, y2), strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
        fun circle(cx: Float, cy: Float, radius: Float, filled: Boolean = false) {
            drawCircle(
                color = tint,
                radius = radius * unit,
                center = at(cx, cy),
                style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else stroke,
            )
        }
        fun roundBox(left: Float, top: Float, right: Float, bottom: Float, radius: Float, filled: Boolean = false) {
            drawRoundRect(
                color = tint,
                topLeft = at(left, top),
                size = Size((right - left) * unit, (bottom - top) * unit),
                cornerRadius = CornerRadius(radius * unit, radius * unit),
                style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else stroke,
            )
        }
        fun arc(left: Float, top: Float, right: Float, bottom: Float, start: Float, sweep: Float) {
            drawArc(
                color = tint,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = at(left, top),
                size = Size((right - left) * unit, (bottom - top) * unit),
                style = stroke,
            )
        }
        when (symbol) {
            EdgeSymbol.Home -> {
                line(4f, 11f, 12f, 4.5f)
                line(12f, 4.5f, 20f, 11f)
                line(7f, 10f, 7f, 19.5f)
                line(17f, 10f, 17f, 19.5f)
                line(7f, 19.5f, 17f, 19.5f)
                line(10.2f, 19.5f, 10.2f, 14.5f)
                line(10.2f, 14.5f, 13.8f, 14.5f)
                line(13.8f, 14.5f, 13.8f, 19.5f)
            }
            EdgeSymbol.Look -> {
                arc(3f, 7f, 21f, 17f, 200f, 140f)
                arc(3f, 7f, 21f, 17f, 20f, 140f)
                circle(12f, 12f, 2.2f, filled = true)
            }
            EdgeSymbol.Style -> {
                line(12f, 3.5f, 12f, 20.5f)
                line(3.5f, 12f, 20.5f, 12f)
                line(6.2f, 6.2f, 17.8f, 17.8f)
                line(17.8f, 6.2f, 6.2f, 17.8f)
                circle(12f, 12f, 2f, filled = true)
            }
            EdgeSymbol.Apps -> {
                roundBox(4f, 4f, 10.2f, 10.2f, 1.6f, filled = true)
                roundBox(13.8f, 4f, 20f, 10.2f, 1.6f, filled = true)
                roundBox(4f, 13.8f, 10.2f, 20f, 1.6f, filled = true)
                roundBox(13.8f, 13.8f, 20f, 20f, 1.6f, filled = true)
            }
            EdgeSymbol.Torch -> {
                roundBox(9f, 8f, 15f, 20f, 2f)
                line(10f, 8f, 10f, 4.5f)
                line(14f, 8f, 14f, 4.5f)
                line(10f, 4.5f, 14f, 4.5f)
                line(12f, 11.5f, 12f, 16f)
            }
            EdgeSymbol.Wifi -> {
                circle(12f, 18.2f, 1.3f, filled = true)
                arc(8.2f, 12.4f, 15.8f, 20f, 210f, 120f)
                arc(5.2f, 9f, 18.8f, 22.2f, 215f, 110f)
                arc(2.4f, 5.6f, 21.6f, 24.4f, 220f, 100f)
            }
            EdgeSymbol.Bluetooth -> {
                line(12f, 3.5f, 12f, 20.5f)
                line(12f, 3.5f, 17.2f, 8.2f)
                line(17.2f, 8.2f, 7.2f, 15.4f)
                line(12f, 20.5f, 17.2f, 15.8f)
                line(17.2f, 15.8f, 7.2f, 8.6f)
            }
            EdgeSymbol.Display -> {
                circle(12f, 12f, 3.3f)
                line(12f, 3.2f, 12f, 5.4f)
                line(12f, 18.6f, 12f, 20.8f)
                line(3.2f, 12f, 5.4f, 12f)
                line(18.6f, 12f, 20.8f, 12f)
                line(5.6f, 5.6f, 7.2f, 7.2f)
                line(16.8f, 16.8f, 18.4f, 18.4f)
                line(18.4f, 5.6f, 16.8f, 7.2f)
                line(7.2f, 16.8f, 5.6f, 18.4f)
            }
            EdgeSymbol.Sound -> {
                line(5.5f, 10f, 5.5f, 14f)
                line(5.5f, 10f, 9f, 10f)
                line(9f, 10f, 14.5f, 6f)
                line(14.5f, 6f, 14.5f, 18f)
                line(14.5f, 18f, 9f, 14f)
                line(9f, 14f, 5.5f, 14f)
                arc(14f, 8.5f, 20f, 15.5f, 300f, 120f)
            }
            EdgeSymbol.Location -> {
                circle(12f, 10f, 3.4f)
                line(8.4f, 12.2f, 12f, 20.5f)
                line(15.6f, 12.2f, 12f, 20.5f)
            }
            EdgeSymbol.Battery -> {
                roundBox(6f, 7f, 18f, 19.5f, 2f)
                line(10f, 7f, 10f, 4.8f)
                line(14f, 7f, 14f, 4.8f)
                line(10f, 4.8f, 14f, 4.8f)
                roundBox(8.2f, 13.4f, 15.8f, 17.4f, 1f, filled = true)
            }
            EdgeSymbol.Notifications -> {
                arc(6f, 5f, 18f, 17f, 200f, 140f)
                line(6.4f, 11f, 6.4f, 15.5f)
                line(17.6f, 11f, 17.6f, 15.5f)
                line(6.4f, 15.5f, 17.6f, 15.5f)
                arc(10f, 15.2f, 14f, 19.2f, 20f, 140f)
            }
            EdgeSymbol.Settings -> {
                line(4f, 8f, 20f, 8f)
                line(4f, 12f, 20f, 12f)
                line(4f, 16f, 20f, 16f)
                circle(9f, 8f, 1.7f, filled = true)
                circle(15f, 12f, 1.7f, filled = true)
                circle(8f, 16f, 1.7f, filled = true)
            }
            EdgeSymbol.Clock -> {
                circle(12f, 12f, 7.2f)
                line(12f, 12f, 12f, 7.4f)
                line(12f, 12f, 16f, 14f)
            }
            EdgeSymbol.Flip -> {
                line(5f, 8f, 18f, 8f)
                line(14.2f, 4.6f, 18.4f, 8f)
                line(14.2f, 11.4f, 18.4f, 8f)
                line(19f, 16f, 6f, 16f)
                line(9.8f, 12.6f, 5.6f, 16f)
                line(9.8f, 19.4f, 5.6f, 16f)
            }
            EdgeSymbol.Airplane -> {
                line(3.5f, 14.5f, 20.5f, 8f)
                line(8f, 13.2f, 11.5f, 5.5f)
                line(8.4f, 13.4f, 15f, 17.5f)
                line(16.2f, 9.6f, 19.2f, 6.2f)
            }
            EdgeSymbol.Data -> {
                roundBox(5.5f, 13f, 8.5f, 19.5f, 1f, filled = true)
                roundBox(10.5f, 9f, 13.5f, 19.5f, 1f, filled = true)
                roundBox(15.5f, 5f, 18.5f, 19.5f, 1f, filled = true)
            }
            EdgeSymbol.Wallpaper -> {
                roundBox(4f, 5f, 20f, 19f, 2f)
                circle(8.2f, 9f, 1.3f, filled = true)
                line(4.8f, 15.5f, 9f, 11.2f)
                line(9f, 11.2f, 13f, 15.2f)
                line(13f, 15.2f, 16f, 12.4f)
                line(16f, 12.4f, 19.2f, 15.5f)
            }
            EdgeSymbol.Camera -> {
                roundBox(3.5f, 8f, 20.5f, 18.5f, 2.2f)
                line(9f, 8f, 10f, 5.5f)
                line(15f, 8f, 14f, 5.5f)
                line(10f, 5.5f, 14f, 5.5f)
                circle(12f, 13.2f, 2.6f)
            }
            EdgeSymbol.Phone -> {
                roundBox(8f, 3.2f, 16f, 20.8f, 2.2f)
                line(10.4f, 6f, 13.6f, 6f)
                circle(12f, 17.6f, 0.9f, filled = true)
            }
            EdgeSymbol.Messages -> {
                roundBox(3.5f, 4.5f, 20.5f, 15.5f, 3f)
                line(8f, 15.2f, 6.2f, 19.5f)
                line(6.2f, 19.5f, 12.5f, 15.2f)
            }
        }
    }
}
