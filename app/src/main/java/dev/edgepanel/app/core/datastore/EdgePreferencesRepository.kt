package dev.edgepanel.app.core.datastore

import androidx.datastore.preferences.core.edit
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.EdgeSkin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EdgePreferencesRepository @Inject constructor(
    private val store: EdgePreferenceStore,
    private val logger: EdgeLogger,
) {
    val preferences: Flow<EdgePreferences> = store.dataStore.data
        .map { prefs -> readEdgePreferences(prefs) { message -> logger.error(message) } }
        .catch { error ->
            if (error is IOException) {
                logger.error("EDGE preferences could not be read. Using defaults.", error)
                emit(EdgePreferences())
            } else {
                throw error
            }
        }

    suspend fun setOnboardingComplete(complete: Boolean) {
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.onboarding] = complete }
    }

    suspend fun setEdge(edge: EdgeSide) {
        store.dataStore.edit { prefs ->
            prefs[EdgePreferenceKeys.edge] = if (edge == EdgeSide.Left) EDGE_LEFT else EDGE_RIGHT
        }
    }

    suspend fun setHandleYFraction(fraction: Float) {
        val stored = if (fraction.isFinite()) fraction.coerceIn(0f, 1f) else DEFAULT_FRACTION
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.fraction] = stored }
    }

    suspend fun setHandleVisible(visible: Boolean) {
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.handleVisible] = visible }
    }

    suspend fun setHandleAlpha(alpha: Float) {
        val stored = if (alpha.isFinite()) alpha.coerceIn(MIN_ALPHA, 1f) else DEFAULT_ALPHA
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.handleAlpha] = stored }
    }

    suspend fun setHandleHeightDp(heightDp: Int) {
        store.dataStore.edit { prefs ->
            prefs[EdgePreferenceKeys.handleHeight] = heightDp.coerceIn(MIN_HANDLE_HEIGHT, MAX_HANDLE_HEIGHT)
        }
    }

    suspend fun setPanelWidthDp(widthDp: Int) {
        store.dataStore.edit { prefs ->
            prefs[EdgePreferenceKeys.panelWidth] = widthDp.coerceIn(MIN_PANEL_WIDTH, MAX_PANEL_WIDTH)
        }
    }

    suspend fun setSkin(skin: EdgeSkin) {
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.skin] = skin.name }
    }

    suspend fun setTitle(title: String) {
        val stored = title.trim().take(MAX_TITLE).ifEmpty { DEFAULT_TITLE }
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.title] = stored }
    }

    suspend fun setShowQuips(show: Boolean) {
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.showQuips] = show }
    }

    suspend fun setScratch(scratch: String) {
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.scratch] = scratch.take(MAX_SCRATCH) }
    }

    suspend fun incrementBoops() {
        store.dataStore.edit { prefs ->
            val next = (prefs[EdgePreferenceKeys.boops] ?: 0).coerceAtLeast(0) + 1
            prefs[EdgePreferenceKeys.boops] = next
        }
    }

    suspend fun resetBoops() {
        store.dataStore.edit { prefs -> prefs[EdgePreferenceKeys.boops] = 0 }
    }

    suspend fun setAppPinned(component: String, pinned: Boolean) {
        if (!component.contains('/') || component.contains(' ')) return
        store.dataStore.edit { prefs ->
            val current = prefs[EdgePreferenceKeys.pinnedApps].orEmpty().toMutableSet()
            if (pinned) {
                if (current.size < MAX_PINS || component in current) current.add(component)
            } else {
                current.remove(component)
            }
            prefs[EdgePreferenceKeys.pinnedApps] = current
        }
    }
}
