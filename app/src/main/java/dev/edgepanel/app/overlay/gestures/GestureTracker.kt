package dev.edgepanel.app.overlay.gestures

import kotlin.math.abs

class GestureConfig(
    val touchSlopPx: Float,
    val swipeThresholdPx: Float,
    val longPressTimeoutMs: Long,
    val dominance: Float = 1.15f,
    val isInward: (Float) -> Boolean,
)

sealed interface GestureSignal {
    data object Tap : GestureSignal
    data object InwardSwipe : GestureSignal
    data object LongPress : GestureSignal
    data class VerticalDrag(val deltaY: Float) : GestureSignal
}

/**
 * Classifies pointer deltas. It does not see [android.view.MotionEvent], so
 * the rules can be tested without a device.
 */
class GestureTracker(private val config: GestureConfig) {
    var isDragging: Boolean = false
        private set

    private var consumedSwipe: Boolean = false
    private var longPressFired: Boolean = false
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var lastY: Float = 0f
    private var tracking: Boolean = false

    fun onDown(x: Float, y: Float, timeMs: Long) {
        downX = x
        downY = y
        lastY = y
        tracking = true
        isDragging = false
        consumedSwipe = false
        longPressFired = false
    }

    fun onMove(x: Float, y: Float, timeMs: Long): List<GestureSignal> {
        if (!tracking || consumedSwipe) return emptyList()
        val dx = x - downX
        val dy = y - downY
        val adx = abs(dx)
        val ady = abs(dy)
        if (!isDragging && !longPressFired) {
            if (adx >= config.swipeThresholdPx && adx > ady * config.dominance && config.isInward(dx)) {
                consumedSwipe = true
                tracking = false
                return listOf(GestureSignal.InwardSwipe)
            }
            if (ady >= config.touchSlopPx && ady >= adx) {
                isDragging = true
            }
        } else if (longPressFired && !isDragging && ady >= config.touchSlopPx) {
            isDragging = true
        }
        if (!isDragging) return emptyList()
        val delta = y - lastY
        lastY = y
        return if (delta == 0f) emptyList() else listOf(GestureSignal.VerticalDrag(delta))
    }

    fun onLongPressTimeout(timeMs: Long): List<GestureSignal> {
        if (!tracking || consumedSwipe || isDragging || longPressFired) return emptyList()
        longPressFired = true
        return listOf(GestureSignal.LongPress)
    }

    fun onUp(x: Float, y: Float, timeMs: Long): List<GestureSignal> {
        if (!tracking) {
            tracking = false
            isDragging = false
            return emptyList()
        }
        val dx = x - downX
        val dy = y - downY
        val tap = !consumedSwipe && !isDragging && !longPressFired &&
            abs(dy) < config.touchSlopPx &&
            abs(dx) < config.swipeThresholdPx
        tracking = false
        isDragging = false
        return if (tap) listOf(GestureSignal.Tap) else emptyList()
    }

    fun onCancel(): List<GestureSignal> {
        val open = tracking && !consumedSwipe && !isDragging && !longPressFired
        tracking = false
        isDragging = false
        consumedSwipe = false
        return if (open) listOf(GestureSignal.Tap) else emptyList()
    }
}
