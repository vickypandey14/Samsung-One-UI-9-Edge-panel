package dev.edgepanel.app.overlay.gestures

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import dev.edgepanel.app.domain.EdgeSide

class HandleTouchCallbacks(
    val panelWasOpenAtDown: () -> Boolean,
    val onDown: () -> Unit = {},
    val onTap: (panelWasOpen: Boolean) -> Unit,
    val onInwardSwipe: () -> Unit,
    val onDrag: (deltaY: Float) -> Unit,
    val onDragFinished: () -> Unit,
    val onLongPress: () -> Unit,
)

class HandleTouchBinder(
    private val callbacks: HandleTouchCallbacks,
    private val longPressTimeoutMs: Long,
) : View.OnTouchListener {
    private val handler = Handler(Looper.getMainLooper())
    private var tracker = trackerFor(EdgeSide.Right, slopPx = 16f, swipePx = 64f)
    private var panelWasOpen = false
    private var sawDrag = false
    private var pointerDown = false
    private var pendingConfig: Triple<EdgeSide, Float, Float>? = null

    fun updateConfig(edge: EdgeSide, touchSlopPx: Float, swipeThresholdPx: Float) {
        if (pointerDown) {
            pendingConfig = Triple(edge, touchSlopPx, swipeThresholdPx)
            return
        }
        tracker = trackerFor(edge, touchSlopPx, swipeThresholdPx)
    }

    fun detach() {
        handler.removeCallbacksAndMessages(null)
        tracker.onCancel()
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                panelWasOpen = callbacks.panelWasOpenAtDown()
                sawDrag = false
                pointerDown = true
                callbacks.onDown()
                tracker.onDown(event.rawX, event.rawY, event.eventTime)
                handler.postDelayed(longPressRunnable, longPressTimeoutMs)
            }

            MotionEvent.ACTION_MOVE -> {
                dispatch(tracker.onMove(event.rawX, event.rawY, event.eventTime))
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                val dragging = tracker.isDragging || sawDrag
                dispatch(tracker.onUp(event.rawX, event.rawY, event.eventTime))
                finishPointer(dragging)
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                val dragging = tracker.isDragging || sawDrag
                dispatch(tracker.onCancel())
                finishPointer(dragging)
            }
        }
        return true
    }

    private val longPressRunnable = Runnable {
        dispatch(tracker.onLongPressTimeout(SystemClock.uptimeMillis()))
    }

    private fun finishPointer(dragging: Boolean) {
        pointerDown = false
        pendingConfig?.let { (edge, slop, swipe) ->
            tracker = trackerFor(edge, slop, swipe)
        }
        pendingConfig = null
        if (dragging) callbacks.onDragFinished()
    }

    private fun dispatch(signals: List<GestureSignal>) {
        signals.forEach { signal ->
            when (signal) {
                GestureSignal.Tap -> callbacks.onTap(panelWasOpen)
                GestureSignal.InwardSwipe -> {
                    handler.removeCallbacks(longPressRunnable)
                    callbacks.onInwardSwipe()
                }
                GestureSignal.LongPress -> callbacks.onLongPress()
                is GestureSignal.VerticalDrag -> {
                    sawDrag = true
                    handler.removeCallbacks(longPressRunnable)
                    callbacks.onDrag(signal.deltaY)
                }
            }
        }
    }

    private fun trackerFor(edge: EdgeSide, slopPx: Float, swipePx: Float): GestureTracker {
        return GestureTracker(
            GestureConfig(
                touchSlopPx = slopPx,
                swipeThresholdPx = swipePx,
                longPressTimeoutMs = longPressTimeoutMs,
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
