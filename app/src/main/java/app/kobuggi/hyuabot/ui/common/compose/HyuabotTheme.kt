package app.kobuggi.hyuabot.ui.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.kobuggi.hyuabot.R

private val Godo = FontFamily(
    Font(R.font.godo_medium, FontWeight.Normal),
    Font(R.font.godo_bold, FontWeight.Bold),
)

private fun godo(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, spacing: Float = 0f) = TextStyle(
    fontFamily = Godo,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = spacing.sp,
)

/** 4dp 그리드에 맞춘 글자 크기 (Godo · 모두 4dp 행간). */
private val HyuabotTypography = Typography(
    headlineMedium = godo(28, 36, FontWeight.Bold, -0.25f),
    titleLarge = godo(22, 28, FontWeight.Bold),
    titleMedium = godo(20, 28, FontWeight.Bold),
    titleSmall = godo(15, 20, FontWeight.Bold),
    bodyLarge = godo(16, 24),
    bodyMedium = godo(14, 20),
    bodySmall = godo(13, 18),
    labelLarge = godo(14, 20, FontWeight.Bold),
    labelMedium = godo(12, 16),
)

/** 값 리소스(values / values-night)의 `md_*` 색 역할로 M3 ColorScheme을 만든다. */
@Composable
fun HyuabotTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val primary = colorResource(R.color.md_primary)
    val onPrimary = colorResource(R.color.md_on_primary)
    val primaryContainer = colorResource(R.color.md_primary_container)
    val onPrimaryContainer = colorResource(R.color.md_on_primary_container)
    val surface = colorResource(R.color.md_surface)
    val lowest = colorResource(R.color.md_surface_container_lowest)
    val highest = colorResource(R.color.md_surface_container_highest)
    val container = colorResource(R.color.md_surface_container)
    val onSurface = colorResource(R.color.md_on_surface)
    val onSurfaceVariant = colorResource(R.color.md_on_surface_variant)
    val outline = colorResource(R.color.md_outline)
    val outlineVariant = colorResource(R.color.md_outline_variant)
    val tertiaryContainer = colorResource(R.color.md_tertiary_container)
    val onTertiaryContainer = colorResource(R.color.md_on_tertiary_container)
    val secondaryContainer = colorResource(R.color.md_secondary_container)
    val onSecondaryContainer = colorResource(R.color.md_on_secondary_container)
    val inverseSurface = colorResource(R.color.md_inverse_surface)
    val inverseOnSurface = colorResource(R.color.md_inverse_on_surface)
    val scheme = if (dark) {
        darkColorScheme(
            primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer, background = surface, surface = surface,
            surfaceContainerLowest = lowest, surfaceContainerHighest = highest, surfaceContainer = container,
            onBackground = onSurface, onSurface = onSurface, onSurfaceVariant = onSurfaceVariant,
            outline = outline, outlineVariant = outlineVariant, tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer, secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer, inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
        )
    } else {
        lightColorScheme(
            primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer, background = surface, surface = surface,
            surfaceContainerLowest = lowest, surfaceContainerHighest = highest, surfaceContainer = container,
            onBackground = onSurface, onSurface = onSurface, onSurfaceVariant = onSurfaceVariant,
            outline = outline, outlineVariant = outlineVariant, tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer, secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer, inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
        )
    }
    MaterialTheme(colorScheme = scheme, typography = HyuabotTypography) {
        // 글자·아이콘 기본색(LocalContentColor)이 야간 모드에서도 onSurface를 따르도록 Surface로 감싼다.
        Surface(color = scheme.surface, contentColor = scheme.onSurface, content = content)
    }
}
