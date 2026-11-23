package dev.edgepanel.app.overlay.service

import android.app.AppOpsManager
import android.app.Service
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.edgepanel.app.core.common.DispatcherProvider
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.core.datastore.EdgePreferences
import dev.edgepanel.app.core.datastore.EdgePreferencesRepository
import dev.edgepanel.app.core.permissions.OverlayPermissionDataSource
import dev.edgepanel.app.domain.OverlayEffect
import dev.edgepanel.app.features.apps.LaunchableApp
import dev.edgepanel.app.features.apps.LaunchableAppCatalog
import dev.edgepanel.app.features.clipboard.EdgeClipboard
import dev.edgepanel.app.features.shortcuts.EdgeShortcut
import dev.edgepanel.app.features.shortcuts.EdgeShortcutActions
import dev.edgepanel.app.domain.ServicePhase
import dev.edgepanel.app.domain.OverlayEvent
import dev.edgepanel.app.domain.PanelPhase
import dev.edgepanel.app.overlay.handle.HandleGeometry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class EdgeOverlayService : Service() {
    @Inject lateinit var session: OverlaySessionRepository
    @Inject lateinit var preferences: EdgePreferencesRepository
    @Inject lateinit var permissions: OverlayPermissionDataSource
    @Inject lateinit var logger: EdgeLogger
    @Inject lateinit var dispatchers: DispatcherProvider
    @Inject lateinit var notifier: OverlayNotifier
    @Inject lateinit var apps: LaunchableAppCatalog
    @Inject lateinit var clipboard: EdgeClipboard
    @Inject lateinit var shortcuts: EdgeShortcutActions

    private val serviceJob = SupervisorJob()
    private lateinit var scope: CoroutineScope
    private lateinit var windows: OverlayWindows
    private var observeJob: Job? = null
    private var running: Boolean = false
    private var stopping: Boolean = false
    private var handleDragActive: Boolean = false
    private var dragTop: Float? = null
    private var openAfterStart: Boolean = false
    private var watchersRegistered: Boolean = false

    private val appOps: AppOpsManager by lazy { getSystemService(AppOpsManager::class.java) }

    private val opListener = AppOpsManager.OnOpChangedListener { _, changedPackage ->
        if (changedPackage != packageName) return@OnOpChangedListener
        scope.launch {
            if (running && !permissions.canDrawOverlays()) {
                logger.error("Overlay permission was revoked while EDGE was active.")
                revokeAndStop()
            }
        }
    }

    private val componentCallbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
            if (running) execute(session.dispatch(OverlayEvent.DisplayChanged))
        }

        override fun onLowMemory() = Unit

        override fun onTrimMemory(level: Int) = Unit
    }

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(serviceJob + dispatchers.main)
        windows = OverlayWindows(
            context = this,
            windowManager = getSystemService(WindowManager::class.java),
            logger = logger,
            session = session,
            host = OverlayViewHost(),
            onOpenSettled = { execute(session.dispatch(OverlayEvent.OpenSettled)) },
            onCloseSettled = { execute(session.dispatch(OverlayEvent.CloseSettled)) },
            onTap = ::onHandleTap,
            onInwardSwipe = ::onInwardSwipe,
            onDrag = ::onDrag,
            onDragFinished = ::onDragFinished,
            onWindowError = ::onWindowError,
            preferences = preferences,
            apps = apps,
            clipboard = clipboard,
            shortcuts = shortcuts,
            onScratch = ::onScratch,
            onCopyScratch = ::onCopyScratch,
            onReadClipboard = ::onReadClipboard,
            onShortcut = ::onShortcut,
            onLaunchApp = ::onLaunchApp,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopOverlay()
            ACTION_OPEN -> startOverlay(openPanel = true)
            ACTION_START -> startOverlay(openPanel = false)
            else -> if (!running) stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (::shortcuts.isInitialized) shortcuts.turnTorchOff()
        unregisterWatchers()
        if (::windows.isInitialized) windows.detachAll()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun startOverlay(openPanel: Boolean) {
        if (running || stopping) {
            if (openPanel && running) requestOpen()
            return
        }
        openAfterStart = openPanel
        enterForeground()
        if (!permissions.canDrawOverlays()) {
            logger.error("Refusing to draw EDGE because overlay permission is not granted.")
            exitForeground()
            stopSelf()
            return
        }
        running = true
        registerWatchers()
        scope.launch {
            try {
                if (stopping) return@launch
                val initial = preferences.preferences.first()
                if (stopping) return@launch
                session.dispatch(OverlayEvent.EdgeChanged(initial.edge))
                session.dispatch(OverlayEvent.HandleMoved(initial.handleYFraction))
                windows.updateLook(initial)
                if (stopping) return@launch
                execute(session.dispatch(OverlayEvent.Activated))
                if (openAfterStart) {
                    openAfterStart = false
                    requestOpen()
                }
                if (stopping) {
                    execute(session.dispatch(OverlayEvent.Deactivated))
                    return@launch
                }
                observePreferences()
            } catch (error: Throwable) {
                logger.error("EDGE failed while starting the overlay.", error)
                failAndStop(error.message ?: "EDGE failed while starting the overlay.")
            }
        }
    }

    private fun stopOverlay() {
        if (stopping) return
        stopping = true
        handleDragActive = false
        dragTop = null
        if (session.state.value.servicePhase == ServicePhase.Active) {
            execute(session.dispatch(OverlayEvent.Deactivated))
        } else if (::windows.isInitialized) {
            windows.detachAll()
        }
        unregisterWatchers()
        exitForeground()
        running = false
        stopSelf()
    }

    private fun revokeAndStop() {
        if (stopping) return
        stopping = true
        execute(session.dispatch(OverlayEvent.PermissionRevoked))
        unregisterWatchers()
        exitForeground()
        running = false
        stopSelf()
    }

    private fun failAndStop(message: String) {
        if (stopping) return
        stopping = true
        execute(session.dispatch(OverlayEvent.Failed(message)))
        unregisterWatchers()
        exitForeground()
        running = false
        stopSelf()
    }

    private fun onWindowError(error: Throwable) {
        failAndStop(error.message ?: "Overlay window failed")
    }

    private fun onHandleTap(panelWasOpen: Boolean) {
        if (panelWasOpen) {
            execute(session.dispatch(OverlayEvent.CloseRequested))
        } else {
            requestOpen()
        }
    }

    private fun onInwardSwipe() {
        if (session.state.value.panelPhase != PanelPhase.Closed) return
        requestOpen()
    }

    private fun requestOpen() {
        val effects = session.dispatch(OverlayEvent.OpenRequested)
        if (effects.isEmpty()) return
        scope.launch { preferences.incrementBoops() }
        execute(effects)
    }

    private fun onScratch(text: String) {
        scope.launch { preferences.setScratch(text) }
    }

    private fun onCopyScratch() {
        scope.launch {
            val note = preferences.preferences.first().scratch
            if (note.isBlank()) {
                clipboard.report("The note is empty.")
            } else {
                clipboard.write(note)
            }
        }
    }

    private fun onReadClipboard() {
        windows.withPanelFocus {
            val text = clipboard.read()
            scope.launch {
                if (text == null) {
                    clipboard.report("One UI didn't share the clipboard. Focus the note and try Read again.")
                } else {
                    preferences.setScratch(text)
                    clipboard.report("Clipboard replaced the note.")
                }
            }
        }
    }

    private fun onShortcut(shortcut: EdgeShortcut) {
        if (shortcut == EdgeShortcut.Torch) {
            shortcuts.toggleTorch()
            return
        }
        val intent = shortcuts.settingsIntent(shortcut) ?: return
        try {
            startActivity(intent)
            execute(session.dispatch(OverlayEvent.CloseRequested))
        } catch (error: android.content.ActivityNotFoundException) {
            logger.error("EDGE could not open that settings screen.", error)
            clipboard.report("That settings screen didn't open.")
        }
    }

    private fun onLaunchApp(app: LaunchableApp) {
        val component = android.content.ComponentName.unflattenFromString(app.component) ?: return
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            .setComponent(component)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            execute(session.dispatch(OverlayEvent.CloseRequested))
        } catch (error: android.content.ActivityNotFoundException) {
            logger.error("EDGE could not open ${app.label}.", error)
            clipboard.report("${app.label} didn't open.")
        }
    }

    private fun onDrag(deltaY: Float) {
        val phase = session.state.value.panelPhase
        if (phase == PanelPhase.Opening || phase == PanelPhase.Closing) return
        handleDragActive = true
        val current = dragTop ?: windows.handleTopPx().toFloat()
        val next = current + deltaY
        dragTop = next
        val range = HandleGeometry.verticalRange(windows.displayFrame(), windows.handleHeightPx())
        val fraction = HandleGeometry.fractionForTop(next.roundToInt(), range)
        execute(session.dispatch(OverlayEvent.HandleMoved(fraction)))
    }

    private fun onDragFinished() {
        val fraction = session.state.value.handleYFraction
        handleDragActive = false
        dragTop = null
        scope.launch { preferences.setHandleYFraction(fraction) }
    }

    private fun observePreferences() {
        observeJob?.cancel()
        observeJob = preferences.preferences
            .drop(1)
            .onEach { prefs -> withContext(dispatchers.main) { applyPreferences(prefs) } }
            .launchIn(scope)
    }

    private fun applyPreferences(prefs: EdgePreferences) {
        val state = session.state.value
        if (prefs.edge != state.edge) {
            execute(session.dispatch(OverlayEvent.EdgeChanged(prefs.edge)))
        }
        if (!handleDragActive && prefs.handleYFraction != state.handleYFraction) {
            execute(session.dispatch(OverlayEvent.HandleMoved(prefs.handleYFraction)))
        }
        windows.updateLook(prefs)
    }

    private fun execute(effects: List<OverlayEffect>) {
        effects.forEach { effect ->
            when (effect) {
                OverlayEffect.AttachHandle -> windows.attachHandle()
                OverlayEffect.AnimatePanelOpen -> windows.animatePanel(open = true)
                OverlayEffect.AnimatePanelClose -> windows.animatePanel(open = false)
                OverlayEffect.ApplyPosition -> windows.applyPosition()
                OverlayEffect.RefreshDisplay -> windows.refreshDisplay()
                OverlayEffect.DetachAll -> windows.detachAll()
            }
        }
    }

    private fun registerWatchers() {
        if (watchersRegistered) return
        appOps.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, packageName, opListener)
        registerComponentCallbacks(componentCallbacks)
        watchersRegistered = true
    }

    private fun unregisterWatchers() {
        observeJob?.cancel()
        observeJob = null
        if (!watchersRegistered) return
        appOps.stopWatchingMode(opListener)
        unregisterComponentCallbacks(componentCallbacks)
        watchersRegistered = false
    }

    private fun enterForeground() {
        val notification = notifier.buildActive()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                OverlayNotifier.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(OverlayNotifier.NOTIFICATION_ID, notification)
        }
    }

    private fun exitForeground() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    companion object {
        const val ACTION_START = "dev.edgepanel.app.action.START"
        const val ACTION_OPEN = "dev.edgepanel.app.action.OPEN"
        const val ACTION_STOP = "dev.edgepanel.app.action.STOP"
    }
}
