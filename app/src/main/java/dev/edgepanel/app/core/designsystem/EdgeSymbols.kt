package dev.edgepanel.app.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private fun EdgeSymbol.vector(): ImageVector {
    return when (this) {
        EdgeSymbol.Home -> Icons.Rounded.Home
        EdgeSymbol.Look -> Icons.Rounded.Visibility
        EdgeSymbol.Style -> Icons.Rounded.Palette
        EdgeSymbol.Apps -> Icons.Rounded.Apps
        EdgeSymbol.Torch -> Icons.Rounded.FlashlightOn
        EdgeSymbol.Wifi -> Icons.Rounded.Wifi
        EdgeSymbol.Bluetooth -> Icons.Rounded.Bluetooth
        EdgeSymbol.Display -> Icons.Rounded.LightMode
        EdgeSymbol.Sound -> Icons.AutoMirrored.Rounded.VolumeUp
        EdgeSymbol.Location -> Icons.Rounded.LocationOn
        EdgeSymbol.Battery -> Icons.Rounded.BatteryFull
        EdgeSymbol.Notifications -> Icons.Rounded.Notifications
        EdgeSymbol.Settings -> Icons.Rounded.Settings
        EdgeSymbol.Clock -> Icons.Rounded.Schedule
        EdgeSymbol.Flip -> Icons.Rounded.SwapHoriz
        EdgeSymbol.Airplane -> Icons.Rounded.AirplanemodeActive
        EdgeSymbol.Data -> Icons.Rounded.DataUsage
        EdgeSymbol.Wallpaper -> Icons.Rounded.Wallpaper
        EdgeSymbol.Camera -> Icons.Rounded.PhotoCamera
        EdgeSymbol.Phone -> Icons.Rounded.Phone
        EdgeSymbol.Messages -> Icons.AutoMirrored.Rounded.Chat
    }
}

@Composable
fun EdgeSymbolIcon(
    symbol: EdgeSymbol,
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Icon(
        imageVector = symbol.vector(),
        contentDescription = null,
        modifier = modifier,
        tint = tint,
    )
}
