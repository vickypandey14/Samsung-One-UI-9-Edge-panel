package dev.edgepanel.app.domain

/**
 * Pixel measurements for the current display. Produced by the overlay layer
 * and consumed by pure layout math.
 */
data class DisplayFrame(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val statusTopPx: Int,
    val navBottomPx: Int,
    val gestureLeftPx: Int,
    val gestureRightPx: Int,
    val gestureTopPx: Int,
    val gestureBottomPx: Int,
    val cutoutTopPx: Int,
    val cutoutBottomPx: Int,
    val cutoutLeftPx: Int,
    val cutoutRightPx: Int,
)
