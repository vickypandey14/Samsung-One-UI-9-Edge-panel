package dev.edgepanel.app.overlay.panel

import dev.edgepanel.app.domain.DisplayFrame
import dev.edgepanel.app.domain.EdgeSide

object PanelGeometry {
    fun widthPx(desiredPx: Int, frame: DisplayFrame, handleWidthPx: Int): Int {
        val maxWidth = (frame.widthPx - handleWidthPx).coerceAtLeast(1)
        return desiredPx.coerceIn(1, maxWidth)
    }

    fun topPx(frame: DisplayFrame): Int {
        return maxOf(frame.statusTopPx, frame.cutoutTopPx, frame.gestureTopPx)
    }

    fun heightPx(frame: DisplayFrame): Int {
        val top = topPx(frame)
        val bottom = maxOf(frame.navBottomPx, frame.cutoutBottomPx, frame.gestureBottomPx)
        return (frame.heightPx - bottom - top).coerceAtLeast(1)
    }

    fun restX(edge: EdgeSide, frame: DisplayFrame, panelWidthPx: Int): Int {
        return when (edge) {
            EdgeSide.Left -> frame.cutoutLeftPx.coerceAtLeast(0)
            EdgeSide.Right -> (frame.widthPx - panelWidthPx - frame.cutoutRightPx).coerceAtLeast(0)
        }
    }

    fun offscreenX(edge: EdgeSide, frame: DisplayFrame, panelWidthPx: Int): Int {
        return when (edge) {
            EdgeSide.Left -> -panelWidthPx
            EdgeSide.Right -> frame.widthPx
        }
    }

    fun dockedHandleX(
        edge: EdgeSide,
        panelRestX: Int,
        panelWidthPx: Int,
        handleWidthPx: Int,
    ): Int {
        return when (edge) {
            EdgeSide.Left -> panelRestX + panelWidthPx
            EdgeSide.Right -> panelRestX - handleWidthPx
        }
    }
}
