package dev.edgepanel.app.overlay.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.edgepanel.app.core.permissions.OverlayPermissionDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EdgeOverlayStarter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: OverlayPermissionDataSource,
) {
    fun start(): Boolean {
        if (!permissions.canDrawOverlays()) return false
        val intent = Intent(context, EdgeOverlayService::class.java).setAction(EdgeOverlayService.ACTION_START)
        ContextCompat.startForegroundService(context, intent)
        return true
    }

    fun open(): Boolean {
        if (!permissions.canDrawOverlays()) return false
        val intent = Intent(context, EdgeOverlayService::class.java).setAction(EdgeOverlayService.ACTION_OPEN)
        ContextCompat.startForegroundService(context, intent)
        return true
    }

    fun stop() {
        val intent = Intent(context, EdgeOverlayService::class.java).setAction(EdgeOverlayService.ACTION_STOP)
        context.startService(intent)
    }
}
