package dev.edgepanel.app.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import dev.edgepanel.app.domain.EdgeSkin

fun edgeColorScheme(skin: EdgeSkin, dark: Boolean): ColorScheme {
    return when (skin) {
        EdgeSkin.Classic -> if (dark) classicDark else classicLight
        EdgeSkin.Bubblegum -> if (dark) bubbleDark else bubbleLight
        EdgeSkin.Midnight -> midnight
        EdgeSkin.Terminal -> terminal
        EdgeSkin.Party -> if (dark) partyDark else partyLight
    }
}

fun handleColors(skin: EdgeSkin, dark: Boolean): Pair<Color, Color> {
    val fill = when (skin) {
        EdgeSkin.Classic -> if (dark) EdgePalette.handleDark else EdgePalette.handleLight
        EdgeSkin.Bubblegum -> Color(0xFFFF5C8A)
        EdgeSkin.Midnight -> Color(0xFF8EB4FF)
        EdgeSkin.Terminal -> Color(0xFF3DDC84)
        EdgeSkin.Party -> Color(0xFFFFB020)
    }
    val stroke = if (dark) EdgePalette.handleStrokeDark else EdgePalette.handleStrokeLight
    return fill to stroke
}

private val classicLight = lightColorScheme(
    primary = Color(0xFF2F5D9F),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5C6B80),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF3F4F6),
    onBackground = Color(0xFF17181A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17181A),
    surfaceVariant = Color(0xFFE7E9EE),
    onSurfaceVariant = Color(0xFF5C616B),
    outline = Color(0xFFD5D8E0),
)

private val classicDark = darkColorScheme(
    primary = Color(0xFF9CB6DC),
    onPrimary = Color(0xFF102033),
    secondary = Color(0xFFB7C0CE),
    onSecondary = Color(0xFF1A1C21),
    background = Color(0xFF101114),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF1C1E24),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF262830),
    onSurfaceVariant = Color(0xFFB4B8C2),
    outline = Color(0xFF343842),
)

private val bubbleLight = lightColorScheme(
    primary = Color(0xFFD63A78),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF1F6),
    onBackground = Color(0xFF3A1022),
    surface = Color(0xFFFFF7FA),
    onSurface = Color(0xFF3A1022),
    surfaceVariant = Color(0xFFFFD6E6),
    onSurfaceVariant = Color(0xFF7A3A56),
    outline = Color(0xFFFFB3CF),
)

private val bubbleDark = darkColorScheme(
    primary = Color(0xFFFF8AB4),
    onPrimary = Color(0xFF3A1022),
    background = Color(0xFF241018),
    onBackground = Color(0xFFFFF1F6),
    surface = Color(0xFF3A1826),
    onSurface = Color(0xFFFFF1F6),
    surfaceVariant = Color(0xFF522033),
    onSurfaceVariant = Color(0xFFFFC2D6),
    outline = Color(0xFF7A3A56),
)

private val midnight = darkColorScheme(
    primary = Color(0xFF9EBAFF),
    onPrimary = Color(0xFF0E1830),
    background = Color(0xFF070B16),
    onBackground = Color(0xFFE6ECFF),
    surface = Color(0xFF12182A),
    onSurface = Color(0xFFE6ECFF),
    surfaceVariant = Color(0xFF1C2742),
    onSurfaceVariant = Color(0xFFB7C4E6),
    outline = Color(0xFF2C3B5C),
)

private val terminal = darkColorScheme(
    primary = Color(0xFF3DDC84),
    onPrimary = Color(0xFF04210F),
    background = Color(0xFF041208),
    onBackground = Color(0xFFB8F5C8),
    surface = Color(0xFF062012),
    onSurface = Color(0xFFD6FFE4),
    surfaceVariant = Color(0xFF0B3320),
    onSurfaceVariant = Color(0xFF8FDBA8),
    outline = Color(0xFF1E5C38),
)

private val partyLight = lightColorScheme(
    primary = Color(0xFF7A3CFF),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF6E8),
    onBackground = Color(0xFF24124A),
    surface = Color(0xFFFFFBF3),
    onSurface = Color(0xFF24124A),
    surfaceVariant = Color(0xFFFFE1A8),
    onSurfaceVariant = Color(0xFF6A4A16),
    outline = Color(0xFFFFC56A),
)

private val partyDark = darkColorScheme(
    primary = Color(0xFFFFB020),
    onPrimary = Color(0xFF2A1600),
    background = Color(0xFF1A1030),
    onBackground = Color(0xFFFFF1D2),
    surface = Color(0xFF2A1848),
    onSurface = Color(0xFFFFF6E4),
    surfaceVariant = Color(0xFF3D2464),
    onSurfaceVariant = Color(0xFFFFD48A),
    outline = Color(0xFF6A48A0),
)
