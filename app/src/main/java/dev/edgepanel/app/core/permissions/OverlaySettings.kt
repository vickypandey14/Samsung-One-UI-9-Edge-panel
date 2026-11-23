package dev.edgepanel.app.core.permissions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun overlayPermissionIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    )
}

fun applicationDetailsIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:${context.packageName}"),
    )
}

/**
 * Tries the overlay screen first. [resolveActivity] is not used: on API 30+
 * package visibility can hide Settings activities even when they can be launched.
 */
fun launchOverlaySettings(
    context: Context,
    launch: (Intent) -> Unit,
    onFailure: (ActivityNotFoundException) -> Unit,
) {
    try {
        launch(overlayPermissionIntent(context))
    } catch (first: ActivityNotFoundException) {
        try {
            launch(applicationDetailsIntent(context))
        } catch (second: ActivityNotFoundException) {
            first.addSuppressed(second)
            onFailure(first)
        }
    }
}
