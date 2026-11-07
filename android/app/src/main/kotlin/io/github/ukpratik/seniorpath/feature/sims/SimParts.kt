package io.github.ukpratik.seniorpath.feature.sims

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

/** A labelled slider that moves in whole [step]s, like the site's range inputs; the value is part of the label. */
@Composable
fun SimSlider(label: String, value: Int, range: IntRange, step: Int, unit: String, onChange: (Int) -> Unit) {
    Column {
        Text("$label $value $unit", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value.toFloat(),
            onValueChange = { v -> onChange((((v - range.first) / step).roundToInt() * step + range.first).coerceIn(range)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = ((range.last - range.first) / step - 1).coerceAtLeast(0),
            modifier = Modifier.semantics { contentDescription = label; stateDescription = "$value $unit" },
        )
    }
}

@Composable
fun SimSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Switch(checked = checked, onCheckedChange = null)
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

/** The sentence that says what happened; announced politely, green when fine and red when not, as on the site. */
@Composable
fun SimResult(text: String, bad: Boolean? = null) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (bad) {
        true -> scheme.errorContainer to scheme.onErrorContainer
        false -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        null -> scheme.surface to scheme.onSurface
    }
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = fg,
        modifier = Modifier.fillMaxWidth().background(bg, RoundedCornerShape(10.dp)).padding(12.dp).semantics { liveRegion = LiveRegionMode.Polite },
    )
}

enum class BarTone { BAD, OK, PLAIN }

/**
 * The site's bar chart: one bar per value scaled to [max], an axis, a label under it, the peak at the top right and
 * an optional dashed marker. It is decoration for sighted readers; the result sentence carries the meaning.
 */
@Composable
fun BarChart(values: List<Number>, max: Double, label: String, peak: String, tone: BarTone, markAt: Int? = null, markLabel: String? = null) {
    val scheme = MaterialTheme.colorScheme
    val bar = when (tone) { BarTone.BAD -> scheme.error; BarTone.OK -> scheme.tertiary; BarTone.PLAIN -> scheme.primary }
    val ink = scheme.onSurfaceVariant
    val axis = scheme.outline
    val measurer = rememberTextMeasurer()
    val small = MaterialTheme.typography.labelSmall
    Canvas(Modifier.fillMaxWidth().height(150.dp).clearAndSetSemantics { }) {
        val pad = 22.dp.toPx()
        val w = size.width
        val h = size.height
        val bw = (w - 8.dp.toPx()) / values.size
        values.forEachIndexed { i, n ->
            val v = n.toDouble()
            if (v <= 0) return@forEachIndexed
            val bh = max(3.dp.toPx(), if (max > 0) (v / max * (h - pad - 8.dp.toPx())).toFloat() else 0f)
            drawRect(bar, Offset(4.dp.toPx() + i * bw, h - pad - bh), Size(max(1f, bw - 1.5.dp.toPx()), bh))
        }
        drawLine(axis, Offset(4.dp.toPx(), h - pad), Offset(w - 4.dp.toPx(), h - pad), strokeWidth = 1.dp.toPx())
        drawText(measurer, label, Offset(4.dp.toPx(), h - pad + 4.dp.toPx()), TextStyle(color = ink, fontSize = small.fontSize))
        val pk = measurer.measure(peak, TextStyle(color = ink, fontSize = small.fontSize, fontWeight = small.fontWeight))
        drawText(pk, topLeft = Offset(w - 4.dp.toPx() - pk.size.width, 0f))
        if (markAt != null) {
            val x = 4.dp.toPx() + markAt * bw
            drawLine(ink, Offset(x, 4.dp.toPx()), Offset(x, h - pad), strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
            if (markLabel != null) {
                val ml = measurer.measure(markLabel, TextStyle(color = ink, fontSize = small.fontSize))
                val nearRight = x > w - ml.size.width - 12.dp.toPx()
                drawText(ml, topLeft = Offset(if (nearRight) x - 4.dp.toPx() - ml.size.width else x + 4.dp.toPx(), 18.dp.toPx()))
            }
        }
    }
}

