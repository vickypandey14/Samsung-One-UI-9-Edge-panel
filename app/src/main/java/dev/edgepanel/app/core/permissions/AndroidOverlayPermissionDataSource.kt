package dev.edgepanel.app.core.permissions

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidOverlayPermissionDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlayPermissionDataSource {
    override fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)
}
