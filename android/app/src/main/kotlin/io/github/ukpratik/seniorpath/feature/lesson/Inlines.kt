package io.github.ukpratik.seniorpath.feature.lesson

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import io.github.ukpratik.seniorpath.content.Inline

/** What tapping inside lesson text does (US-011 terms, US-013 citations); external links open in the browser. */
@Immutable
data class InlineActions(val onTerm: (String) -> Unit, val onCite: (Int) -> Unit)

val LocalInlineActions = compositionLocalOf { InlineActions(onTerm = {}, onCite = {}) }

@Immutable
private data class InlineColors(val link: Color, val term: Color, val codeBg: Color)

/** Lesson inline runs as one AnnotatedString: bold, italic, code, and tappable terms, citations and links. */
@Composable
fun rememberInlines(runs: List<Inline>): AnnotatedString {
    val actions = LocalInlineActions.current
    val scheme = MaterialTheme.colorScheme
    val colors = InlineColors(link = scheme.primary, term = scheme.onSurface, codeBg = scheme.surfaceVariant)
    return buildAnnotatedString { append(runs, colors, actions) }
}

private fun AnnotatedString.Builder.append(runs: List<Inline>, colors: InlineColors, actions: InlineActions) {
    for (r in runs) {
        when (r) {
            is Inline.Text -> append(r.v)
            is Inline.Break -> append('\n')
            is Inline.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(r.c, colors, actions) }
            is Inline.Em -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(r.c, colors, actions) }
            is Inline.Code -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = colors.codeBg, fontSize = 0.9.em)) {
                append(" ${r.v} ")
            }
            is Inline.Link -> withLink(
                LinkAnnotation.Url(r.url, TextLinkStyles(SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline))),
            ) { append(r.c, colors, actions) }
            // A glossary term: dotted on the site; here underlined in the text colour so it doesn't look like a web link.
            is Inline.Term -> withLink(
                LinkAnnotation.Clickable(
                    "term:${r.id}",
                    TextLinkStyles(SpanStyle(color = colors.term, textDecoration = TextDecoration.Underline)),
                ) { actions.onTerm(r.id) },
            ) { append(r.v) }
            is Inline.Cite -> withLink(
                LinkAnnotation.Clickable(
                    "cite:${r.n}",
                    TextLinkStyles(SpanStyle(color = colors.link, fontSize = 0.75.em, baselineShift = BaselineShift.Superscript, fontWeight = FontWeight.Bold)),
                ) { actions.onCite(r.n) },
            ) { append("[${r.n}]") }
        }
    }
}
