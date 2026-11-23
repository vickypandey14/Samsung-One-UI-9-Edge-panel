package dev.edgepanel.app.overlay.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.PathInterpolator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.edgepanel.app.R
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.core.datastore.EdgePreferences
import dev.edgepanel.app.core.datastore.EdgePreferencesRepository
import dev.edgepanel.app.core.designsystem.EdgeTokens
import dev.edgepanel.app.domain.DisplayFrame
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.PanelPhase
import dev.edgepanel.app.features.apps.LaunchableApp
import dev.edgepanel.app.features.apps.LaunchableAppCatalog
import dev.edgepanel.app.features.clipboard.EdgeClipboard
import dev.edgepanel.app.features.shortcuts.EdgeShortcut
import dev.edgepanel.app.features.shortcuts.EdgeShortcutActions
import dev.edgepanel.app.overlay.gestures.HandleTouchBinder
import dev.edgepanel.app.overlay.gestures.HandleTouchCallbacks
import dev.edgepanel.app.overlay.handle.EdgeHandleFace
import dev.edgepanel.app.overlay.handle.HandleGeometry
import dev.edgepanel.app.overlay.panel.EdgePanelContent
import dev.edgepanel.app.overlay.panel.PanelGeometry
import kotlin.math.roundToInt

/**
 * Owns the two overlay windows. Touch targets are the handle and the panel
 * only. There is no full-screen interceptor.
 */
class OverlayWindows(
    private val context: Context,
    private val windowManager: WindowManager,
    private val logger: EdgeLogger,
    private val session: OverlaySessionRepository,
    private val host: OverlayViewHost,
    private val onOpenSettled: () -> Unit,
    private val onCloseSettled: () -> Unit,
    private val onTap: (panelWasOpen: Boolean) -> Unit,
    private val onInwardSwipe: () -> Unit,
    private val onDrag: (Float) -> Unit,
    private val onDragFinished: () -> Unit,
    private val onWindowError: (Throwable) -> Unit,
    private val preferences: EdgePreferencesRepository,
    private val apps: LaunchableAppCatalog,
    private val clipboard: EdgeClipboard,
    private val shortcuts: EdgeShortcutActions,
    private val onScratch: (String) -> Unit,
    private val onCopyScratch: () -> Unit,
    private val onReadClipboard: () -> Unit,
    private val onShortcut: (EdgeShortcut) -> Unit,
    private val onLaunchApp: (LaunchableApp) -> Unit,
) {
    private val reader = DisplayFrameReader()
    private var frame: DisplayFrame = reader.read(windowManager, context.resources.displayMetrics.density)
    private var handleView: ComposeView? = null
    private var handleParams: WindowManager.LayoutParams? = null
    private var panelView: ComposeView? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var animator: ValueAnimator? = null
    private var motionGeneration: Int = 0
    private var loggedFrame: Boolean = false
    private var shuttingDown: Boolean = false
    private var ignoreOutsideUntilMs: Long = 0L
    private var look: EdgePreferences = EdgePreferences()
    private val touchBinder: HandleTouchBinder

    init {
        val slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        val swipe = dp(OPEN_SWIPE_DP).toFloat()
        touchBinder = HandleTouchBinder(
            callbacks = HandleTouchCallbacks(
                panelWasOpenAtDown = { session.state.value.panelPhase != PanelPhase.Closed },
                onDown = { logger.info("Handle received a touch.") },
                onTap = onTap,
                onInwardSwipe = onInwardSwipe,
                onDrag = onDrag,
                onDragFinished = onDragFinished,
                onLongPress = { handleView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) },
            ),
            longPressTimeoutMs = ViewConfiguration.getLongPressTimeout().toLong(),
        )
        touchBinder.updateConfig(session.state.value.edge, slop, swipe)
    }

    fun displayFrame(): DisplayFrame = frame

    fun handleHeightPx(): Int = handleHeight()

    fun handleTopPx(): Int = handleParams?.y ?: HandleGeometry.topForFraction(
        session.state.value.handleYFraction,
        HandleGeometry.verticalRange(frame, handleHeight()),
    )

    fun updateLook(prefs: EdgePreferences) {
        val previous = look
        look = prefs
        val glass = previous.panelSolid != prefs.panelSolid ||
            previous.panelBlurDp != prefs.panelBlurDp
        val visual = previous.handleVisible != prefs.handleVisible ||
            previous.handleAlpha != prefs.handleAlpha ||
            previous.handleHeightDp != prefs.handleHeightDp ||
            previous.panelWidthDp != prefs.panelWidthDp
        if (!visual && !glass) return
        runWindow {
            if (glass) applyPanelGlass()
            if (!visual) return@runWindow
            val active = session.state.value.servicePhase == dev.edgepanel.app.domain.ServicePhase.Active
            if (!active) return@runWindow
            if (prefs.handleVisible && handleView == null) {
                attachHandle()
                return@runWindow
            }
            if (!prefs.handleVisible) {
                removeHandle()
                return@runWindow
            }
            val params = handleParams ?: return@runWindow
            val view = handleView ?: return@runWindow
            params.alpha = prefs.handleAlpha
            val docked = session.state.value.panelPhase == PanelPhase.Open
            placeHandle(params, docked)
            windowManager.updateViewLayout(view, params)
            if (docked) layoutPanel(animated = false, open = true)
        }
    }

    fun setPanelFocusable(focusable: Boolean) {
        val params = panelParams ?: return
        val view = panelView ?: return
        try {
            params.flags = if (focusable) {
                params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            } else {
                params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            windowManager.updateViewLayout(view, params)
            if (focusable) view.requestFocus()
        } catch (error: RuntimeException) {
            logger.error("EDGE could not change panel focus.", error)
        }
    }

    fun withPanelFocus(block: () -> Unit) {
        val params = panelParams ?: return
        val view = panelView ?: return
        val previous = params.flags
        params.flags = previous and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        windowManager.updateViewLayout(view, params)
        view.requestFocus()
        try {
            block()
        } finally {
            params.flags = previous
            try {
                windowManager.updateViewLayout(view, params)
            } catch (error: RuntimeException) {
                logger.error("EDGE could not restore the panel flags.", error)
            }
        }
    }

    fun attachHandle() {
        runWindow {
            if (!look.handleVisible) return@runWindow
            if (handleView != null) {
                applyPosition()
                return@runWindow
            }
            host.start()
            frame = reader.read(windowManager, context.resources.displayMetrics.density)
            logFrame()
            val size = handleSize()
            val params = baseParams(size.first, size.second, watchOutside = false).also {
                it.alpha = look.handleAlpha
            }
            placeHandle(params, docked = false)
            val view = composeView(context.getString(R.string.handle_description)) {
                val edge by session.state.collectAsStateWithLifecycle()
                val style by preferences.preferences.collectAsStateWithLifecycle(initialValue = look)
                EdgeHandleFace(edge.edge, style.skin)
            }
            view.setOnTouchListener(touchBinder)
            view.setOnApplyWindowInsetsListener { _, insets ->
                val updated = reader.fromViewInsets(frame, insets)
                if (updated != frame) {
                    frame = updated
                    logFrame()
                    applyPosition()
                }
                insets
            }
            windowManager.addView(view, params)
            handleView = view
            handleParams = params
            excludeSystemGestures(view)
            ViewCompat.requestApplyInsets(view)
            syncTouchConfig()
        }
    }

    fun applyPosition() {
        runWindow {
            val params = handleParams ?: return@runWindow
            val view = handleView ?: return@runWindow
            val phase = session.state.value.panelPhase
            if (phase == PanelPhase.Opening || phase == PanelPhase.Closing) return@runWindow
            placeHandle(params, docked = phase == PanelPhase.Open)
            windowManager.updateViewLayout(view, params)
            if (phase == PanelPhase.Open) {
                layoutPanel(animated = false, open = true)
            }
            syncTouchConfig()
        }
    }

    fun refreshDisplay() {
        runWindow {
            cancelMotion()
            frame = reader.read(windowManager, context.resources.displayMetrics.density)
            logFrame()
            when (session.state.value.panelPhase) {
                PanelPhase.Opening -> layoutPanel(animated = false, open = true, settleOpen = true)
                PanelPhase.Open -> layoutPanel(animated = false, open = true)
                PanelPhase.Closing -> {
                    removePanel()
                    positionHandle(docked = false)
                    onCloseSettled()
                }
                PanelPhase.Closed -> positionHandle(docked = false)
            }
            handleView?.let { view ->
                view.dispatchConfigurationChanged(context.resources.configuration)
            }
            panelView?.dispatchConfigurationChanged(context.resources.configuration)
        }
    }

    fun animatePanel(open: Boolean) {
        runWindow {
            if (open) {
                ignoreOutsideUntilMs = SystemClock.uptimeMillis() + OUTSIDE_GRACE_MS
                ensurePanel()
                animateTo(open = true)
            } else if (panelView == null) {
                positionHandle(docked = false)
                onCloseSettled()
            } else {
                animateTo(open = false)
            }
        }
    }

    fun detachAll() {
        shuttingDown = true
        cancelMotion()
        touchBinder.detach()
        removePanel()
        removeHandle()
        host.destroy()
        shuttingDown = false
    }

    private fun animateTo(open: Boolean) {
        val panel = panelParams ?: return
        val view = panelView ?: return
        val width = panel.width
        val target = if (open) {
            PanelGeometry.restX(session.state.value.edge, frame, width)
        } else {
            PanelGeometry.offscreenX(session.state.value.edge, frame, width)
        }
        val handle = handleParams
        val handleView = this.handleView
        val handleStart = handle?.x ?: 0
        val handleEnd = if (open) {
            PanelGeometry.dockedHandleX(session.state.value.edge, target, width, handleWidth())
        } else {
            HandleGeometry.restX(session.state.value.edge, frame, handleWidth())
        }
        val token = ++motionGeneration
        animator?.cancel()
        if (panel.x == target) {
            if (handle != null && handleView != null) {
                handle.x = handleEnd
                updateOrReport(handleView, handle)
            }
            if (open) onOpenSettled() else finishClose()
            return
        }
        val from = panel.x
        val span = (target - from).takeIf { it != 0 } ?: 1
        val next = ValueAnimator.ofInt(from, target).apply {
            duration = EdgeTokens.panelAnimationMs
            interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                panel.x = value
                updateOrReport(view, panel)
                if (handle != null && handleView != null) {
                    val fraction = (value - from).toFloat() / span.toFloat()
                    handle.x = (handleStart + (handleEnd - handleStart) * fraction).roundToInt()
                    updateOrReport(handleView, handle)
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false

                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (cancelled || token != motionGeneration) return
                    if (open) onOpenSettled() else finishClose()
                }
            })
        }
        animator = next
        next.start()
    }

    private fun finishClose() {
        removePanel()
        positionHandle(docked = false)
        onCloseSettled()
    }

    private fun ensurePanel() {
        if (panelView != null) return
        val width = PanelGeometry.widthPx(dp(look.panelWidthDp), frame, handleWidth())
        val height = PanelGeometry.heightPx(frame)
        val params = baseParams(width, height, watchOutside = true)
        params.x = PanelGeometry.offscreenX(session.state.value.edge, frame, width)
        params.y = PanelGeometry.topPx(frame)
        val view = composeView(contentDescription = null) {
            val uiState = session.state.collectAsStateWithLifecycle()
            val style by preferences.preferences.collectAsStateWithLifecycle(initialValue = look)
            val status by clipboard.status.collectAsStateWithLifecycle()
            var loaded by remember { mutableStateOf(emptyList<LaunchableApp>()) }
            val torch by shortcuts.torchOn.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { loaded = apps.load() }
            EdgePanelContent(
                edge = uiState.value.edge,
                look = style,
                apps = loaded,
                clipboardStatus = status,
                torchOn = torch,
                onClose = { onTap(true) },
                onScratch = onScratch,
                onScratchFocus = ::setPanelFocusable,
                onCopyScratch = onCopyScratch,
                onReadClipboard = onReadClipboard,
                onShortcut = onShortcut,
                onLaunchApp = onLaunchApp,
            )
        }
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                if (SystemClock.uptimeMillis() < ignoreOutsideUntilMs) {
                    true
                } else {
                    onTap(true)
                    true
                }
            } else {
                false
            }
        }
        windowManager.addView(view, params)
        panelView = view
        panelParams = params
        applyPanelGlass()
    }

    private fun applyPanelGlass() {
        val params = panelParams ?: return
        val view = panelView ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blur = if (look.panelSolid) 0 else dp(look.panelBlurDp)
                if (blur > 0) {
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                    params.blurBehindRadius = blur
                } else {
                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
                    params.blurBehindRadius = 0
                }
            }
            windowManager.updateViewLayout(view, params)
        } catch (error: RuntimeException) {
            logger.error("EDGE could not update the panel glass.", error)
        }
    }

    private fun layoutPanel(animated: Boolean, open: Boolean, settleOpen: Boolean = false) {
        if (!open) return
        ensurePanel()
        val params = panelParams ?: return
        val view = panelView ?: return
        params.width = PanelGeometry.widthPx(dp(look.panelWidthDp), frame, handleWidth())
        params.height = PanelGeometry.heightPx(frame)
        params.y = PanelGeometry.topPx(frame)
        params.x = PanelGeometry.restX(session.state.value.edge, frame, params.width)
        windowManager.updateViewLayout(view, params)
        positionHandle(docked = true)
        if (!animated && settleOpen) onOpenSettled()
    }

    private fun positionHandle(docked: Boolean) {
        val params = handleParams ?: return
        val view = handleView ?: return
        placeHandle(params, docked)
        windowManager.updateViewLayout(view, params)
    }

    private fun placeHandle(params: WindowManager.LayoutParams, docked: Boolean) {
        val edge = session.state.value.edge
        val width = handleWidth()
        val height = handleHeight()
        val touchWidth = if (docked) {
            width
        } else {
            HandleGeometry.touchWidthPx(
                width,
                frame.density,
                if (edge == EdgeSide.Left) frame.gestureLeftPx else frame.gestureRightPx,
            )
        }
        params.width = touchWidth
        params.height = height
        val range = HandleGeometry.verticalRange(frame, height)
        params.y = HandleGeometry.topForFraction(session.state.value.handleYFraction, range)
        params.x = if (docked) {
            val panelWidth = panelParams?.width ?: PanelGeometry.widthPx(dp(look.panelWidthDp), frame, width)
            val panelX = panelParams?.x ?: PanelGeometry.restX(edge, frame, panelWidth)
            PanelGeometry.dockedHandleX(edge, panelX, panelWidth, width)
        } else {
            HandleGeometry.touchX(edge, frame, width, touchWidth)
        }
        handleView?.let { excludeSystemGestures(it) }
    }

    private fun composeView(
        contentDescription: String?,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ): ComposeView {
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)
            this.contentDescription = contentDescription
            importantForAccessibility = if (contentDescription == null) {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
            setContent(content)
        }
    }

    private fun baseParams(width: Int, height: Int, watchOutside: Boolean): WindowManager.LayoutParams {
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
            if (watchOutside) {
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                    } else {
                        0
                    }
            } else {
                0
            }
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            windowAnimations = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            if (watchOutside && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurBehindRadius = (28f * frame.density).roundToInt().coerceAtLeast(1)
            }
        }
    }

    private fun updateOrReport(view: View, params: WindowManager.LayoutParams) {
        try {
            windowManager.updateViewLayout(view, params)
        } catch (error: IllegalArgumentException) {
            logger.error("EDGE could not update an overlay window.", error)
        } catch (error: RuntimeException) {
            report(error)
        }
    }

    private fun removePanel() {
        panelView?.let { view ->
            view.setOnTouchListener(null)
            removeView(view)
        }
        panelView = null
        panelParams = null
    }

    private fun removeHandle() {
        handleView?.let { view ->
            view.setOnTouchListener(null)
            view.setOnApplyWindowInsetsListener(null)
            removeView(view)
        }
        handleView = null
        handleParams = null
    }

    private fun removeView(view: View) {
        try {
            windowManager.removeView(view)
        } catch (error: IllegalArgumentException) {
            logger.info("Overlay view was already detached: ${error.message}")
        }
    }

    private fun cancelMotion() {
        motionGeneration += 1
        animator?.cancel()
        animator = null
    }

    private fun syncTouchConfig() {
        val slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        touchBinder.updateConfig(
            edge = session.state.value.edge,
            touchSlopPx = slop,
            swipeThresholdPx = dp(OPEN_SWIPE_DP).toFloat(),
        )
    }

    private fun handleSize(): Pair<Int, Int> = handleWidth() to handleHeight()

    private fun handleWidth(): Int = dp(EdgeTokens.handleWidthDp).coerceAtLeast(1)

    private fun handleHeight(): Int = dp(look.handleHeightDp).coerceAtLeast(1)

    private fun dp(value: Int): Int = (value * frame.density).roundToInt()

    private fun logFrame() {
        if (loggedFrame) return
        loggedFrame = true
        logger.info(
            "Display ${frame.widthPx}x${frame.heightPx} density ${frame.density} " +
                "gesture L${frame.gestureLeftPx} R${frame.gestureRightPx} " +
                "T${frame.gestureTopPx} B${frame.gestureBottomPx}. " +
                "Handle touch area extends inward of the visible pill.",
        )
    }

    private fun excludeSystemGestures(view: View) {
        view.doOnLayout { laidOut ->
            if (laidOut.width <= 0 || laidOut.height <= 0) return@doOnLayout
            ViewCompat.setSystemGestureExclusionRects(
                laidOut,
                listOf(Rect(0, 0, laidOut.width, laidOut.height)),
            )
        }
    }

    private inline fun runWindow(block: () -> Unit) {
        if (shuttingDown) return
        try {
            block()
        } catch (error: SecurityException) {
            report(error)
        } catch (error: android.view.WindowManager.BadTokenException) {
            report(error)
        } catch (error: IllegalArgumentException) {
            report(error)
        } catch (error: RuntimeException) {
            report(error)
        }
    }

    private companion object {
        const val OUTSIDE_GRACE_MS = 450L
        const val OPEN_SWIPE_DP = 18
    }

    private fun report(error: Throwable) {
        if (shuttingDown) return
        shuttingDown = true
        logger.error("EDGE overlay window failed.", error)
        onWindowError(error)
    }
}
