package dev.edgepanel.app.core.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class EdgeTokenSpecTest {
    @Test
    fun handleAndPanelMatchTheFoundationSpec() {
        assertEquals(7, EdgeTokens.handleWidthDp)
        assertEquals(68, EdgeTokens.handleHeightDp)
        assertEquals(12, EdgeTokens.handleCornerDp)
        assertEquals(0.85f, EdgeTokens.handleAlpha)
        assertEquals(300, EdgeTokens.panelWidthDp)
    }
}
