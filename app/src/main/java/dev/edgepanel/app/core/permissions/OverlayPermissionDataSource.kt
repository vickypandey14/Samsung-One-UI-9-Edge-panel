package dev.edgepanel.app.core.permissions

interface OverlayPermissionDataSource {
    fun canDrawOverlays(): Boolean
}
