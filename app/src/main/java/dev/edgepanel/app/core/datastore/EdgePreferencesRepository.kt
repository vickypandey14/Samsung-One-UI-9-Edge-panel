package dev.edgepanel.app.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.domain.EdgeSide
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
        .map { prefs -> mapPreferences(prefs) }
        .catch { error ->
            if (error is IOException) {
                logger.error("EDGE preferences could not be read. Using defaults.", error)
                emit(EdgePreferences())
            } else {
                throw error
            }
        }

    suspend fun setOnboardingComplete(complete: Boolean) {
        store.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING] = complete
        }
    }

    suspend fun setEdge(edge: EdgeSide) {
        store.dataStore.edit { prefs ->
            prefs[Keys.EDGE] = if (edge == EdgeSide.Left) EDGE_LEFT else EDGE_RIGHT
        }
    }

    suspend fun setHandleYFraction(fraction: Float) {
        val stored = if (fraction.isFinite()) fraction.coerceIn(0f, 1f) else 0.5f
        store.dataStore.edit { prefs ->
            prefs[Keys.FRACTION] = stored
        }
    }

    private fun mapPreferences(prefs: androidx.datastore.preferences.core.Preferences): EdgePreferences {
        val rawEdge = prefs[Keys.EDGE]
        if (rawEdge != null && rawEdge != EDGE_LEFT && rawEdge != EDGE_RIGHT) {
            logger.error("Stored edge '$rawEdge' is not recognized. Using the right edge.")
        }
        val rawFraction = prefs[Keys.FRACTION] ?: DEFAULT_FRACTION
        val fraction = if (rawFraction.isFinite()) rawFraction.coerceIn(0f, 1f) else DEFAULT_FRACTION
        return EdgePreferences(
            onboardingComplete = prefs[Keys.ONBOARDING] == true,
            edge = if (rawEdge == EDGE_LEFT) EdgeSide.Left else EdgeSide.Right,
            handleYFraction = fraction,
        )
    }

    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val EDGE = stringPreferencesKey("edge")
        val FRACTION = floatPreferencesKey("handle_y_fraction")
    }

    private companion object {
        const val EDGE_LEFT = "left"
        const val EDGE_RIGHT = "right"
        const val DEFAULT_FRACTION = 0.5f
    }
}
