package dev.edgepanel.app.features.shortcuts

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
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
            EdgeShortcut.Torch -> return null
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
