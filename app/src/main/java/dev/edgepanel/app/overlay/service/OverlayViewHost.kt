package dev.edgepanel.app.overlay.service

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Lifecycle, saved state, and ViewModel store for overlay ComposeViews.
 * The host is owned by [EdgeOverlayService], never by an Activity.
 */
class OverlayViewHost : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)
    private var restored = false

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    fun start() {
        if (!restored) {
            savedStateController.performRestore(null)
            restored = true
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED ||
            lifecycleRegistry.currentState == Lifecycle.State.DESTROYED
        ) {
            return
        }
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        savedStateController.performSave(Bundle())
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }
}
