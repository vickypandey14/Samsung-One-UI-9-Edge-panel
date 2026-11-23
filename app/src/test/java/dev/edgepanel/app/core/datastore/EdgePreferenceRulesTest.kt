package dev.edgepanel.app.core.datastore

import androidx.datastore.preferences.core.preferencesOf
import dev.edgepanel.app.domain.EdgeQuips
import dev.edgepanel.app.domain.EdgeSkin
import org.junit.Assert.assertEquals
import org.junit.Test

class EdgePreferenceRulesTest {
    @Test
    fun unknownValuesFallBackToSafeDefaults() {
        val stored = preferencesOf(
            EdgePreferenceKeys.skin to "nope",
            EdgePreferenceKeys.handleAlpha to 0.01f,
            EdgePreferenceKeys.handleHeight to 1,
            EdgePreferenceKeys.panelWidth to 9_000,
            EdgePreferenceKeys.pinnedApps to setOf("bad", "com.example/app.Main"),
            EdgePreferenceKeys.title to "   ",
        )
        val read = readEdgePreferences(stored)
        assertEquals(EdgeSkin.Classic, read.skin)
        assertEquals(MIN_ALPHA, read.handleAlpha)
        assertEquals(MIN_HANDLE_HEIGHT, read.handleHeightDp)
        assertEquals(MAX_PANEL_WIDTH, read.panelWidthDp)
        assertEquals(setOf("com.example/app.Main"), read.pinnedApps)
        assertEquals(DEFAULT_TITLE, read.title)
        assertEquals(true, read.handleVisible)
    }

    @Test
    fun quipsStayInsideTheList() {
        assertEquals(EdgeQuips.line(0), EdgeQuips.line(8))
        assertEquals("Boop accepted.", EdgeQuips.line(3))
    }
}
