package io.github.ukpratik.seniorpath.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.data.ThemeMode

private fun jakarta(weight: Int) =
    Font(R.font.plus_jakarta_sans, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

/** Plus Jakarta Sans, the site's font: one variable file, so every weight is real, not synthesised. */
val Jakarta: FontFamily = FontFamily(jakarta(400), jakarta(500), jakarta(600), jakarta(700), jakarta(800))

private fun TextStyle.jakarta(weight: FontWeight? = null) = copy(fontFamily = Jakarta, fontWeight = weight ?: fontWeight)

/** Material's type scale in Jakarta; text follows the system font scale (A-003). Headings are bolder, as on the site. */
private val SpTypography = Typography().run {
    Typography(
        displayLarge = displayLarge.jakarta(FontWeight.ExtraBold),
        displayMedium = displayMedium.jakarta(FontWeight.ExtraBold),
        displaySmall = displaySmall.jakarta(FontWeight.ExtraBold),
        headlineLarge = headlineLarge.jakarta(FontWeight.ExtraBold),
        headlineMedium = headlineMedium.jakarta(FontWeight.ExtraBold),
        headlineSmall = headlineSmall.jakarta(FontWeight.Bold),
        titleLarge = titleLarge.jakarta(FontWeight.Bold),
        titleMedium = titleMedium.jakarta(FontWeight.Bold),
        titleSmall = titleSmall.jakarta(FontWeight.Bold),
        bodyLarge = bodyLarge.jakarta(),
        bodyMedium = bodyMedium.jakarta(),
        bodySmall = bodySmall.jakarta(),
        labelLarge = labelLarge.jakarta(FontWeight.Bold),
        labelMedium = labelMedium.jakarta(FontWeight.Bold),
        labelSmall = labelSmall.jakarta(FontWeight.Bold),
    )
}

@Composable
@ReadOnlyComposable
fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** Brand colours in both themes; no dynamic colour, so the app looks like the site. */
@Composable
fun SeniorPathTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpColors provides if (dark) DarkSp else LightSp) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = SpTypography, content = content)
    }
}
