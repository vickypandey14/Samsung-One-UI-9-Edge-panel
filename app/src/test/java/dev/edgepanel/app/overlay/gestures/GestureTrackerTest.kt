package dev.edgepanel.app.overlay.gestures

import dev.edgepanel.app.domain.EdgeSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureTrackerTest {
    @Test
    fun tapWhenThePointerBarelyMoves() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(0f, 100f, 0L)
        val signals = tracker.onUp(3f, 102f, 80L)
        assertEquals(listOf(GestureSignal.Tap), signals)
    }

    @Test
    fun inwardSwipeOnTheRightEdgeOpens() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(400f, 200f, 0L)
        val signals = tracker.onMove(340f, 204f, 40L)
        assertEquals(listOf(GestureSignal.InwardSwipe), signals)
        assertTrue(tracker.onUp(340f, 204f, 50L).isEmpty())
    }

    @Test
    fun inwardSwipeOnTheLeftEdgeOpens() {
        val tracker = tracker(EdgeSide.Left)
        tracker.onDown(10f, 200f, 0L)
        val signals = tracker.onMove(80f, 198f, 40L)
        assertEquals(listOf(GestureSignal.InwardSwipe), signals)
    }

    @Test
    fun shortInwardMoveOpensWhenTheFingerLifts() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(400f, 200f, 0L)
        assertTrue(tracker.onMove(380f, 202f, 30L).isEmpty())
        assertEquals(listOf(GestureSignal.InwardSwipe), tracker.onUp(380f, 202f, 60L))
    }

    @Test
    fun cancelledPressStillOpens() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(400f, 200f, 0L)
        assertEquals(listOf(GestureSignal.Tap), tracker.onCancel())
    }

    @Test
    fun outwardSwipeDoesNotOpen() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(400f, 200f, 0L)
        assertTrue(tracker.onMove(480f, 202f, 40L).isEmpty())
        assertTrue(tracker.onUp(480f, 202f, 60L).isEmpty())
    }

    @Test
    fun verticalDragReportsDeltas() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(400f, 200f, 0L)
        val first = tracker.onMove(404f, 250f, 30L)
        assertEquals(listOf(GestureSignal.VerticalDrag(50f)), first)
        val second = tracker.onMove(406f, 270f, 45L)
        assertEquals(listOf(GestureSignal.VerticalDrag(20f)), second)
    }

    @Test
    fun longPressDoesNotAlsoCountAsATap() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(20f, 20f, 0L)
        assertEquals(listOf(GestureSignal.LongPress), tracker.onLongPressTimeout(400L))
        assertTrue(tracker.onUp(22f, 21f, 450L).isEmpty())
    }

    @Test
    fun longPressThenVerticalMovementDrags() {
        val tracker = tracker(EdgeSide.Right)
        tracker.onDown(20f, 20f, 0L)
        tracker.onLongPressTimeout(400L)
        val drag = tracker.onMove(22f, 40f, 460L)
        assertEquals(listOf(GestureSignal.VerticalDrag(20f)), drag)
    }

    private fun tracker(edge: EdgeSide): GestureTracker {
        return GestureTracker(
            GestureConfig(
                touchSlopPx = 8f,
                swipeThresholdPx = 48f,
                longPressTimeoutMs = 400L,
                isInward = { dx ->
                    when (edge) {
                        EdgeSide.Right -> dx < 0f
                        EdgeSide.Left -> dx > 0f
                    }
                },
            ),
        )
    }
}
