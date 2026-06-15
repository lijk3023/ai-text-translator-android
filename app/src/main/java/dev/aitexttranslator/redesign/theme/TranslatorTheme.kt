package dev.aitexttranslator.redesign.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * 全新设计系统 —— "Paper & Ink"
 *
 * 设计取向（综合 getdesign.md 里的 Linear / Claude / Raycast 三套参考，详见 design-references/）：
 *  - 单一强调色：靛蓝紫 accent，不引入第二个彩色，不用渐变、不用毛玻璃。
 *  - 浅色用「暖纸」底色而非纯白，深色用「近黑」而非纯黑 —— 长时间读译文不刺眼，避免光污染。
 *  - 用 1px 发丝边框 + 表面层级（canvas → surface → surfaceMuted）表达层次，几乎不用阴影。
 *  - 译文阅读区用更大字号、更高行高，因为这才是用户真正盯着看的内容。
 *  - 圆角克制（卡片 18dp、按钮/输入 12dp），不再堆 24~28dp 的「玩具感」大圆角。
 *
 * 实现说明：
 *  - Material3 的 ColorScheme 只覆盖了部分槽位，额外的语义色（成功/危险、表面层级、发丝线、
 *    弱化文本等）放进 [TranslatorPalette]，通过 [LocalTranslatorPalette] 下发。
 *  - 字体族默认走系统字体；推荐适配方接入 Inter（见 README 的字体说明）。
 */

@Immutable
data class TranslatorPalette(
    val isDark: Boolean,
    // 表面层级（从最底到最上）
    val canvas: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val hairline: Color,
    // 文本层级
    val ink: Color,
    val inkMuted: Color,
    val inkSubtle: Color,
    // 强调色
    val accent: Color,
    val onAccent: Color,
    val accentText: Color,
    val accentSoft: Color,
    // 语义色
    val success: Color,
    val successSoft: Color,
    val onSuccessSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val onDangerSoft: Color,
)

/** 浅色：暖纸底 + 暖墨色文字 + 靛蓝紫强调。 */
val LightPalette = TranslatorPalette(
    isDark = false,
    canvas = Color(0xFFF3F1EC),
    surface = Color(0xFFFBFAF7),
    surfaceMuted = Color(0xFFECEAE3),
    hairline = Color(0xFFE1DED5),
    ink = Color(0xFF1B1B19),
    inkMuted = Color(0xFF6A675F),
    inkSubtle = Color(0xFF9A968B),
    accent = Color(0xFF5B57D1),
    onAccent = Color(0xFFFFFFFF),
    accentText = Color(0xFF4F4BC4),
    accentSoft = Color(0xFFECEBFB),
    success = Color(0xFF2E9E54),
    successSoft = Color(0xFFE6F4EC),
    onSuccessSoft = Color(0xFF1E7B3E),
    danger = Color(0xFFC5443B),
    dangerSoft = Color(0xFFFBEAE7),
    onDangerSoft = Color(0xFFB23A32),
)

/** 深色：近黑底（带极淡冷调，不是纯黑）+ 近白文字 + 略提亮的强调色。 */
val DarkPalette = TranslatorPalette(
    isDark = true,
    canvas = Color(0xFF0E0E11),
    surface = Color(0xFF17171B),
    surfaceMuted = Color(0xFF1F1F25),
    hairline = Color(0xFF2B2B33),
    ink = Color(0xFFECECEF),
    inkMuted = Color(0xFF9F9FA8),
    inkSubtle = Color(0xFF6C6C76),
    accent = Color(0xFF6C66E0),
    onAccent = Color(0xFFFFFFFF),
    accentText = Color(0xFFA6A2FF),
    accentSoft = Color(0xFF211F38),
    success = Color(0xFF46C46B),
    successSoft = Color(0xFF14271B),
    onSuccessSoft = Color(0xFF6FD98E),
    danger = Color(0xFFE5675C),
    dangerSoft = Color(0xFF2B1715),
    onDangerSoft = Color(0xFFF0897F),
)

val LocalTranslatorPalette: ProvidableCompositionLocal<TranslatorPalette> =
    staticCompositionLocalOf { LightPalette }

/** 全局取色入口：`TranslatorTheme.colors.accent`。 */
object TranslatorTheme {
    val colors: TranslatorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalTranslatorPalette.current
}

private fun translatorTypography(): Typography {
    // 字体族留空走系统默认；适配方可整体替换为 Inter / 思源黑体。
    val family = FontFamily.Default
    return Typography(
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.sp,
        ),
    )
}

private val TranslatorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * 应用主题。默认跟随系统深浅色；也可用 [darkTheme] 强制指定（弹窗里可能想固定）。
 */
@Composable
fun TranslatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    // 把语义色映射进 Material3 ColorScheme，保证内置组件（如 Switch）也跟随主题。
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            background = palette.canvas,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.inkMuted,
            outline = palette.hairline,
            error = palette.danger,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            background = palette.canvas,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.inkMuted,
            outline = palette.hairline,
            error = palette.danger,
        )
    }

    CompositionLocalProvider(LocalTranslatorPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = translatorTypography(),
            shapes = TranslatorShapes,
            content = content,
        )
    }
}
