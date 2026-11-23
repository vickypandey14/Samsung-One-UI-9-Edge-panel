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
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var tracking: Boolean = false

    fun onDown(x: Float, y: Float, timeMs: Long) {
        downX = x
        downY = y
        lastX = x
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
        lastX = x
        if (!isDragging) {
            lastY = y
            return emptyList()
        }
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
        val inward = inwardOpen(dx, dy)
        val tap = !consumedSwipe && !isDragging && !inward &&
            abs(dy) < config.touchSlopPx &&
            abs(dx) < config.swipeThresholdPx
        tracking = false
        isDragging = false
        return when {
            inward -> listOf(GestureSignal.InwardSwipe)
            tap -> listOf(GestureSignal.Tap)
            else -> emptyList()
        }
    }

    fun onCancel(): List<GestureSignal> {
        val dx = lastX - downX
        val dy = lastY - downY
        val inward = tracking && inwardOpen(dx, dy)
        val open = tracking && !consumedSwipe && !isDragging && !inward
        tracking = false
        isDragging = false
        consumedSwipe = false
        return when {
            inward -> listOf(GestureSignal.InwardSwipe)
            open -> listOf(GestureSignal.Tap)
            else -> emptyList()
        }
    }

    private fun inwardOpen(dx: Float, dy: Float): Boolean {
        return !consumedSwipe &&
            config.isInward(dx) &&
            abs(dx) >= config.touchSlopPx &&
            abs(dx) >= abs(dy)
    }
}
