package dev.edgepanel.app.core.datastore

import dev.edgepanel.app.domain.EdgeSide

data class EdgePreferences(
    val onboardingComplete: Boolean = false,
    val edge: EdgeSide = EdgeSide.Right,
    val handleYFraction: Float = 0.5f,
)
