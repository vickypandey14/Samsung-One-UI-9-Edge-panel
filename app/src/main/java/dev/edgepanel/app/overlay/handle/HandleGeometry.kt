package dev.edgepanel.app.overlay.handle

import dev.edgepanel.app.domain.DisplayFrame
import dev.edgepanel.app.domain.EdgeSide
import kotlin.math.roundToInt

data class VerticalRange(
    val minTop: Int,
    val maxTop: Int,
)

object HandleGeometry {
    fun verticalRange(frame: DisplayFrame, handleHeightPx: Int): VerticalRange {
        val minTop = maxOf(frame.statusTopPx, frame.cutoutTopPx, frame.gestureTopPx)
        val bottomObstacle = maxOf(frame.navBottomPx, frame.cutoutBottomPx, frame.gestureBottomPx)
        val maxTop = frame.heightPx - bottomObstacle - handleHeightPx
        return if (maxTop >= minTop) {
            VerticalRange(minTop, maxTop)
        } else {
            VerticalRange(minTop.coerceAtLeast(0), minTop.coerceAtLeast(0))
        }
    }

    fun clampTop(desiredTop: Int, range: VerticalRange): Int {
        if (range.maxTop <= range.minTop) return range.minTop
        return desiredTop.coerceIn(range.minTop, range.maxTop)
    }

    fun topForFraction(fraction: Float, range: VerticalRange): Int {
        val clamped = fraction.coerceIn(0f, 1f)
        if (range.maxTop <= range.minTop) return range.minTop
        val span = range.maxTop - range.minTop
        return range.minTop + (span * clamped).roundToInt()
    }

    fun fractionForTop(top: Int, range: VerticalRange): Float {
        if (range.maxTop <= range.minTop) return 0f
        val clamped = clampTop(top, range)
        return (clamped - range.minTop).toFloat() / (range.maxTop - range.minTop).toFloat()
    }

    /**
     * Visible pill position. The pill sits just inside the side gesture
     * inset so a tap is delivered to this overlay instead of the system
     * back gesture or Samsung's edge grab. Clearance is capped so a very
     * large inset cannot park the handle in the middle of the screen.
     * A cutout can push it further inward.
     */
    fun restX(edge: EdgeSide, frame: DisplayFrame, handleWidthPx: Int): Int {
        val clearance = when (edge) {
            EdgeSide.Left -> clearancePx(frame.gestureLeftPx, frame.density)
            EdgeSide.Right -> clearancePx(frame.gestureRightPx, frame.density)
        }
        return when (edge) {
            EdgeSide.Left -> maxOf(frame.cutoutLeftPx, clearance).coerceAtLeast(0)
            EdgeSide.Right -> (frame.widthPx - handleWidthPx - maxOf(frame.cutoutRightPx, clearance)).coerceAtLeast(0)
        }
    }

    /**
     * Closed-handle window. Wider than the pill and extended inward, so a
     * finger can land on it. The pill stays on the outer side of this window.
     */
    fun touchWidthPx(visualWidthPx: Int, density: Float): Int {
        val finger = (TOUCH_WIDTH_DP * density).roundToInt()
        return maxOf(visualWidthPx, finger)
    }

    fun touchX(edge: EdgeSide, frame: DisplayFrame, visualWidthPx: Int, touchWidthPx: Int): Int {
        val visualX = restX(edge, frame, visualWidthPx)
        return when (edge) {
            EdgeSide.Left -> visualX
            EdgeSide.Right -> visualX + visualWidthPx - touchWidthPx
        }
    }

    fun clearancePx(gestureInsetPx: Int, density: Float): Int {
        val minimum = (MIN_CLEARANCE_DP * density).roundToInt()
        val maximum = (MAX_CLEARANCE_DP * density).roundToInt()
        return gestureInsetPx.coerceIn(minimum, maximum)
    }

    private const val MIN_CLEARANCE_DP = 20f
    private const val MAX_CLEARANCE_DP = 48f
    private const val TOUCH_WIDTH_DP = 48f
}
