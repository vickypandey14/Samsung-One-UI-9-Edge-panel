package dev.edgepanel.app.features.shortcuts

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.edgepanel.app.core.common.EdgeLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class EdgeShortcut {
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

fun EdgeShortcut.panelLabel(torchOn: Boolean = false): String {
    return when (this) {
        EdgeShortcut.Torch -> if (torchOn) "Light off" else "Light"
        EdgeShortcut.Wifi -> "Wi-Fi"
        EdgeShortcut.Bluetooth -> "Bluetooth"
        EdgeShortcut.Display -> "Display"
        EdgeShortcut.Sound -> "Sound"
        EdgeShortcut.Location -> "Location"
        EdgeShortcut.Battery -> "Battery"
        EdgeShortcut.Notifications -> "Alerts"
        EdgeShortcut.Settings -> "Settings"
        EdgeShortcut.Clock -> "Clock"
        EdgeShortcut.Flip -> "Flip edge"
        EdgeShortcut.Airplane -> "Airplane"
        EdgeShortcut.Data -> "Data use"
        EdgeShortcut.Wallpaper -> "Wallpaper"
        EdgeShortcut.Camera -> "Camera"
        EdgeShortcut.Phone -> "Phone"
        EdgeShortcut.Messages -> "Messages"
    }
}

@Singleton
class EdgeShortcutActions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: EdgeLogger,
) {
    private val camera = context.getSystemService(CameraManager::class.java)
    private var torchCameraId: String? = null
    private val _torchOn = MutableStateFlow(false)
    val torchOn: StateFlow<Boolean> = _torchOn.asStateFlow()

    fun toggleTorch(): Boolean {
        val id = torchCameraId ?: findTorchCamera()
        if (id == null) {
            logger.error("No flashlight is available for EDGE to toggle.")
            return _torchOn.value
        }
        torchCameraId = id
        val next = !_torchOn.value
        return try {
            camera.setTorchMode(id, next)
            _torchOn.value = next
            next
        } catch (error: RuntimeException) {
            logger.error("EDGE could not toggle the flashlight.", error)
            _torchOn.value
        }
    }

    fun turnTorchOff() {
        val id = torchCameraId ?: return
        if (!_torchOn.value) return
        try {
            camera.setTorchMode(id, false)
        } catch (error: RuntimeException) {
            logger.error("EDGE could not turn the flashlight off.", error)
        } finally {
            _torchOn.value = false
        }
    }

    fun settingsIntent(shortcut: EdgeShortcut): Intent? {
        val action = when (shortcut) {
            EdgeShortcut.Wifi -> Settings.ACTION_WIFI_SETTINGS
            EdgeShortcut.Bluetooth -> Settings.ACTION_BLUETOOTH_SETTINGS
            EdgeShortcut.Display -> Settings.ACTION_DISPLAY_SETTINGS
            EdgeShortcut.Sound -> Settings.ACTION_SOUND_SETTINGS
            EdgeShortcut.Location -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            EdgeShortcut.Battery -> Intent.ACTION_POWER_USAGE_SUMMARY
            EdgeShortcut.Notifications -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS
            } else {
                "android.settings.NOTIFICATION_SETTINGS"
            }
            EdgeShortcut.Settings -> Settings.ACTION_SETTINGS
            EdgeShortcut.Clock -> AlarmClock.ACTION_SHOW_ALARMS
            EdgeShortcut.Airplane -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            EdgeShortcut.Data -> Settings.ACTION_DATA_USAGE_SETTINGS
            EdgeShortcut.Wallpaper -> return Intent(Intent.ACTION_SET_WALLPAPER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            EdgeShortcut.Camera -> return Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            EdgeShortcut.Phone -> return Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            EdgeShortcut.Messages -> return Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_APP_MESSAGING)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            EdgeShortcut.Torch, EdgeShortcut.Flip -> return null
        }
        return Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun findTorchCamera(): String? {
        return camera.cameraIdList.firstOrNull { id ->
            camera.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }
}
