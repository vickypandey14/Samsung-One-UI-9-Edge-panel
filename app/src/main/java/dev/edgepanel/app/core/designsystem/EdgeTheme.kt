package dev.edgepanel.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFF2F5D9F)
private val AccentDark = Color(0xFF9CB6DC)
private val OnAccent = Color(0xFFFFFFFF)
private val OnAccentDark = Color(0xFF102033)

private val LightScheme = lightColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
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

private val DarkScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = OnAccentDark,
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

private val BaseTypography = Typography()

private val EdgeTypography = Typography(
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.Medium),
    bodyLarge = BaseTypography.bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = BaseTypography.bodyMedium.copy(lineHeight = 22.sp),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

private val EdgeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp),
)

object EdgePalette {
    val handleLight = Color(0xFF2C2E33)
    val handleDark = Color(0xFFF2F3F5)
    val handleStrokeLight = Color(0x66000000)
    val handleStrokeDark = Color(0x66FFFFFF)
}

@Composable
fun EdgeTheme(content: @Composable () -> Unit) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = EdgeTypography,
        shapes = EdgeShapes,
        content = content,
    )
}
