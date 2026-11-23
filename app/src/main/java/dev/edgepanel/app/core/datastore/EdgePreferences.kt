package dev.edgepanel.app.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.edgepanel.app.domain.EdgeSide
import dev.edgepanel.app.domain.EdgeSkin

data class EdgePreferences(
    val onboardingComplete: Boolean = false,
    val edge: EdgeSide = EdgeSide.Right,
    val handleYFraction: Float = 0.5f,
    val handleVisible: Boolean = true,
    val handleAlpha: Float = DEFAULT_ALPHA,
    val handleHeightDp: Int = DEFAULT_HANDLE_HEIGHT,
    val panelWidthDp: Int = DEFAULT_PANEL_WIDTH,
    val skin: EdgeSkin = EdgeSkin.Classic,
    val title: String = DEFAULT_TITLE,
    val showQuips: Boolean = true,
    val boops: Int = 0,
    val scratch: String = "",
    val pinnedApps: Set<String> = emptySet(),
)

internal object EdgePreferenceKeys {
    val onboarding = booleanPreferencesKey("onboarding_complete")
    val edge = stringPreferencesKey("edge")
    val fraction = floatPreferencesKey("handle_y_fraction")
    val handleVisible = booleanPreferencesKey("handle_visible")
    val handleAlpha = floatPreferencesKey("handle_alpha")
    val handleHeight = intPreferencesKey("handle_height_dp")
    val panelWidth = intPreferencesKey("panel_width_dp")
    val skin = stringPreferencesKey("skin")
    val title = stringPreferencesKey("title")
    val showQuips = booleanPreferencesKey("show_quips")
    val boops = intPreferencesKey("boops")
    val scratch = stringPreferencesKey("scratch")
    val pinnedApps = stringSetPreferencesKey("pinned_apps")
}

fun readEdgePreferences(
    prefs: Preferences,
    onError: (String) -> Unit = {},
): EdgePreferences {
    val rawEdge = prefs[EdgePreferenceKeys.edge]
    if (rawEdge != null && rawEdge != EDGE_LEFT && rawEdge != EDGE_RIGHT) {
        onError("Stored edge '$rawEdge' is not recognized. Using the right edge.")
    }
    val rawFraction = prefs[EdgePreferenceKeys.fraction] ?: DEFAULT_FRACTION
    val fraction = if (rawFraction.isFinite()) rawFraction.coerceIn(0f, 1f) else DEFAULT_FRACTION
    val rawAlpha = prefs[EdgePreferenceKeys.handleAlpha] ?: DEFAULT_ALPHA
    val alpha = if (rawAlpha.isFinite()) rawAlpha.coerceIn(MIN_ALPHA, 1f) else DEFAULT_ALPHA
    val rawSkin = prefs[EdgePreferenceKeys.skin]
    if (rawSkin != null && EdgeSkin.fromStored(rawSkin).name != rawSkin &&
        EdgeSkin.entries.none { it.name.equals(rawSkin, ignoreCase = true) }
    ) {
        onError("Stored skin '$rawSkin' is not recognized. Using Classic.")
    }
    val title = prefs[EdgePreferenceKeys.title]
        ?.trim()
        ?.take(MAX_TITLE)
        ?.takeIf { it.isNotEmpty() }
        ?: DEFAULT_TITLE
    val scratch = prefs[EdgePreferenceKeys.scratch].orEmpty().take(MAX_SCRATCH)
    val pinned = prefs[EdgePreferenceKeys.pinnedApps]
        .orEmpty()
        .filter { it.contains('/') && !it.contains(' ') }
        .take(MAX_PINS)
        .toSet()
    return EdgePreferences(
        onboardingComplete = prefs[EdgePreferenceKeys.onboarding] == true,
        edge = if (rawEdge == EDGE_LEFT) EdgeSide.Left else EdgeSide.Right,
        handleYFraction = fraction,
        handleVisible = prefs[EdgePreferenceKeys.handleVisible] != false,
        handleAlpha = alpha,
        handleHeightDp = (prefs[EdgePreferenceKeys.handleHeight] ?: DEFAULT_HANDLE_HEIGHT)
            .coerceIn(MIN_HANDLE_HEIGHT, MAX_HANDLE_HEIGHT),
        panelWidthDp = (prefs[EdgePreferenceKeys.panelWidth] ?: DEFAULT_PANEL_WIDTH)
            .coerceIn(MIN_PANEL_WIDTH, MAX_PANEL_WIDTH),
        skin = EdgeSkin.fromStored(rawSkin),
        title = title,
        showQuips = prefs[EdgePreferenceKeys.showQuips] != false,
        boops = (prefs[EdgePreferenceKeys.boops] ?: 0).coerceAtLeast(0),
        scratch = scratch,
        pinnedApps = pinned,
    )
}

internal const val EDGE_LEFT = "left"
internal const val EDGE_RIGHT = "right"
internal const val DEFAULT_FRACTION = 0.5f
internal const val DEFAULT_ALPHA = 0.85f
internal const val MIN_ALPHA = 0.35f
internal const val DEFAULT_HANDLE_HEIGHT = 68
internal const val MIN_HANDLE_HEIGHT = 48
internal const val MAX_HANDLE_HEIGHT = 140
internal const val DEFAULT_PANEL_WIDTH = 300
internal const val MIN_PANEL_WIDTH = 220
internal const val MAX_PANEL_WIDTH = 380
internal const val DEFAULT_TITLE = "EDGE"
internal const val MAX_TITLE = 24
internal const val MAX_SCRATCH = 500
internal const val MAX_PINS = 16
