package dev.edgepanel.app.overlay.panel

import dev.edgepanel.app.domain.DisplayFrame
import dev.edgepanel.app.domain.EdgeSide
import org.junit.Assert.assertEquals
import org.junit.Test

class PanelGeometryTest {
    private val frame = DisplayFrame(
        widthPx = 1080,
        heightPx = 2400,
        density = 3f,
        statusTopPx = 90,
        navBottomPx = 48,
        gestureLeftPx = 72,
        gestureRightPx = 72,
        gestureTopPx = 40,
        gestureBottomPx = 140,
        cutoutTopPx = 20,
        cutoutBottomPx = 0,
        cutoutLeftPx = 0,
        cutoutRightPx = 0,
    )

    @Test
    fun rightPanelRestsOnTheEdgeAndLeavesFromTheRight() {
        assertEquals(780, PanelGeometry.restX(EdgeSide.Right, frame, panelWidthPx = 300))
        assertEquals(1080, PanelGeometry.offscreenX(EdgeSide.Right, frame, panelWidthPx = 300))
        assertEquals(760, PanelGeometry.dockedHandleX(EdgeSide.Right, panelRestX = 780, panelWidthPx = 300, handleWidthPx = 20))
    }

    @Test
    fun leftPanelRestsOnTheEdgeAndLeavesFromTheLeft() {
        assertEquals(0, PanelGeometry.restX(EdgeSide.Left, frame, panelWidthPx = 300))
        assertEquals(-300, PanelGeometry.offscreenX(EdgeSide.Left, frame, panelWidthPx = 300))
        assertEquals(300, PanelGeometry.dockedHandleX(EdgeSide.Left, panelRestX = 0, panelWidthPx = 300, handleWidthPx = 20))
    }

    @Test
    fun panelHeightRespectsTheLargerSafeInset() {
        assertEquals(90, PanelGeometry.topPx(frame))
        assertEquals(2400 - 140 - 90, PanelGeometry.heightPx(frame))
    }

    @Test
    fun panelLeavesRoomForTheHandle() {
        assertEquals(1000, PanelGeometry.widthPx(desiredPx = 1200, frame = frame, handleWidthPx = 80))
        assertEquals(300, PanelGeometry.widthPx(desiredPx = 300, frame = frame, handleWidthPx = 80))
    }
}
