package dev.edgepanel.app.overlay.handle

import dev.edgepanel.app.domain.DisplayFrame
import dev.edgepanel.app.domain.EdgeSide
import org.junit.Assert.assertEquals
import org.junit.Test

class HandleGeometryTest {
    private val frame = DisplayFrame(
        widthPx = 1080,
        heightPx = 2400,
        density = 3f,
        statusTopPx = 80,
        navBottomPx = 40,
        gestureLeftPx = 80,
        gestureRightPx = 80,
        gestureTopPx = 80,
        gestureBottomPx = 120,
        cutoutTopPx = 0,
        cutoutBottomPx = 0,
        cutoutLeftPx = 0,
        cutoutRightPx = 0,
    )

    @Test
    fun verticalCenterUsesTheUsableBand() {
        val range = HandleGeometry.verticalRange(frame, handleHeightPx = 200)
        assertEquals(80, range.minTop)
        assertEquals(2080, range.maxTop)
        val top = HandleGeometry.topForFraction(0.5f, range)
        assertEquals(1080, top)
        assertEquals(1180, top + 100)
    }

    @Test
    fun clampKeepsTheHandleOutOfStatusAndGestureBars() {
        val range = HandleGeometry.verticalRange(frame, handleHeightPx = 200)
        assertEquals(80, HandleGeometry.clampTop(-40, range))
        assertEquals(2080, HandleGeometry.clampTop(9_000, range))
    }

    @Test
    fun fractionRoundTripStaysInsideTheRange() {
        val range = HandleGeometry.verticalRange(frame, handleHeightPx = 200)
        val top = HandleGeometry.topForFraction(0.25f, range)
        val fraction = HandleGeometry.fractionForTop(top, range)
        assertEquals(0.25f, fraction, 0.001f)
    }

    @Test
    fun collapsedRangeDoesNotThrow() {
        val tiny = frame.copy(heightPx = 100, statusTopPx = 20, gestureTopPx = 20, navBottomPx = 20, gestureBottomPx = 20)
        val range = HandleGeometry.verticalRange(tiny, handleHeightPx = 200)
        assertEquals(range.minTop, HandleGeometry.clampTop(50, range))
        assertEquals(0f, HandleGeometry.fractionForTop(50, range))
    }

    @Test
    fun handleSitsJustInsideTheSideGestureZone() {
        assertEquals(980, HandleGeometry.restX(EdgeSide.Right, frame, handleWidthPx = 20))
        assertEquals(80, HandleGeometry.restX(EdgeSide.Left, frame, handleWidthPx = 20))
        val cutout = frame.copy(cutoutRightPx = 120, cutoutLeftPx = 100)
        assertEquals(940, HandleGeometry.restX(EdgeSide.Right, cutout, handleWidthPx = 20))
        assertEquals(100, HandleGeometry.restX(EdgeSide.Left, cutout, handleWidthPx = 20))
    }

    @Test
    fun missingGestureInsetStillClearsTheEdgeGrab() {
        val flush = frame.copy(gestureLeftPx = 0, gestureRightPx = 0)
        assertEquals(60, HandleGeometry.restX(EdgeSide.Left, flush, handleWidthPx = 20))
        assertEquals(1000, HandleGeometry.restX(EdgeSide.Right, flush, handleWidthPx = 20))
    }

    @Test
    fun touchWindowStartsOnThePhysicalEdge() {
        val touchWidth = HandleGeometry.touchWidthPx(visualWidthPx = 20, density = 3f, gestureInsetPx = 80)
        assertEquals(164, touchWidth)
        assertEquals(0, HandleGeometry.touchX(EdgeSide.Left, frame, visualWidthPx = 20, touchWidthPx = touchWidth))
        assertEquals(1080 - 164, HandleGeometry.touchX(EdgeSide.Right, frame, visualWidthPx = 20, touchWidthPx = touchWidth))
    }
}
