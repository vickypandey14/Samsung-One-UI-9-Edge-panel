package dev.edgepanel.app.overlay.service

import android.graphics.Insets
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import dev.edgepanel.app.domain.DisplayFrame

class DisplayFrameReader {
    fun read(windowManager: WindowManager, density: Float): DisplayFrame {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            frameFromInsets(
                width = bounds.width(),
                height = bounds.height(),
                density = density,
                insets = metrics.windowInsets,
            )
        } else {
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            DisplayFrame(
                widthPx = metrics.widthPixels,
                heightPx = metrics.heightPixels,
                density = density,
                statusTopPx = (24f * density).toInt(),
                navBottomPx = (48f * density).toInt(),
                gestureLeftPx = (24f * density).toInt(),
                gestureRightPx = (24f * density).toInt(),
                gestureTopPx = (24f * density).toInt(),
                gestureBottomPx = (48f * density).toInt(),
                cutoutTopPx = 0,
                cutoutBottomPx = 0,
                cutoutLeftPx = 0,
                cutoutRightPx = 0,
            )
        }
    }

    fun fromViewInsets(current: DisplayFrame, insets: WindowInsets): DisplayFrame {
        return frameFromInsets(current.widthPx, current.heightPx, current.density, insets)
    }

    private fun frameFromInsets(
        width: Int,
        height: Int,
        density: Float,
        insets: WindowInsets,
    ): DisplayFrame {
        val snapshot = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            InsetSnapshot(
                statusTop = insets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars()).top,
                navBottom = insets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()).bottom,
                gestureLeft = insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemGestures()).left,
                gestureRight = insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemGestures()).right,
                gestureTop = insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemGestures()).top,
                gestureBottom = insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemGestures()).bottom,
                cutout = insets.getInsetsIgnoringVisibility(WindowInsets.Type.displayCutout()),
            )
        } else {
            @Suppress("DEPRECATION")
            val gesture = insets.systemGestureInsets
            @Suppress("DEPRECATION")
            val system = insets.systemWindowInsets
            val cutout = insets.displayCutout
            InsetSnapshot(
                statusTop = system.top,
                navBottom = system.bottom,
                gestureLeft = gesture.left,
                gestureRight = gesture.right,
                gestureTop = gesture.top,
                gestureBottom = gesture.bottom,
                cutoutLeft = cutout?.safeInsetLeft ?: 0,
                cutoutTop = cutout?.safeInsetTop ?: 0,
                cutoutRight = cutout?.safeInsetRight ?: 0,
                cutoutBottom = cutout?.safeInsetBottom ?: 0,
            )
        }
        return DisplayFrame(
            widthPx = width,
            heightPx = height,
            density = density,
            statusTopPx = snapshot.statusTop,
            navBottomPx = snapshot.navBottom,
            gestureLeftPx = snapshot.gestureLeft,
            gestureRightPx = snapshot.gestureRight,
            gestureTopPx = snapshot.gestureTop,
            gestureBottomPx = snapshot.gestureBottom,
            cutoutTopPx = snapshot.cutoutTop,
            cutoutBottomPx = snapshot.cutoutBottom,
            cutoutLeftPx = snapshot.cutoutLeft,
            cutoutRightPx = snapshot.cutoutRight,
        )
    }

    private class InsetSnapshot(
        val statusTop: Int,
        val navBottom: Int,
        val gestureLeft: Int,
        val gestureRight: Int,
        val gestureTop: Int,
        val gestureBottom: Int,
        val cutoutLeft: Int,
        val cutoutTop: Int,
        val cutoutRight: Int,
        val cutoutBottom: Int,
    ) {
        constructor(
            statusTop: Int,
            navBottom: Int,
            gestureLeft: Int,
            gestureRight: Int,
            gestureTop: Int,
            gestureBottom: Int,
            cutout: Insets,
        ) : this(
            statusTop = statusTop,
            navBottom = navBottom,
            gestureLeft = gestureLeft,
            gestureRight = gestureRight,
            gestureTop = gestureTop,
            gestureBottom = gestureBottom,
            cutoutLeft = cutout.left,
            cutoutTop = cutout.top,
            cutoutRight = cutout.right,
            cutoutBottom = cutout.bottom,
        )
    }
}
