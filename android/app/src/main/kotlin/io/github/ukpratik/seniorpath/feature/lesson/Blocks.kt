package io.github.ukpratik.seniorpath.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Block
import io.github.ukpratik.seniorpath.content.Cell
import io.github.ukpratik.seniorpath.content.Diagram
import io.github.ukpratik.seniorpath.content.Inline
import io.github.ukpratik.seniorpath.ui.components.SpCard

/** Draws one lesson block (the closed set in app-export.ts). [diagram] looks up a diagram by id. */
@Composable
fun LessonBlock(block: Block, diagram: (String) -> Diagram?, modifier: Modifier = Modifier) {
    when (block) {
        is Block.Paragraph -> Text(rememberInlines(block.c), style = MaterialTheme.typography.bodyLarge, modifier = modifier)
        is Block.Heading -> Text(
            rememberInlines(block.c),
            style = if (block.depth <= 3) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(top = 8.dp).semantics { heading() },
        )
        is Block.ListBlock -> Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.items.forEachIndexed { i, item ->
                Row {
                    Text(
                        if (block.ordered) "${(block.start ?: 1) + i}." else "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(28.dp),
                    )
                    Blocks(item, diagram, Modifier.weight(1f))
                }
            }
        }
        is Block.Code -> Box(
            modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).horizontalScroll(rememberScrollState()),
        ) {
            SelectionContainer {
                Text(block.v, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }
        }
        is Block.Table -> TableBlock(block, modifier)
        is Block.Callout -> Row(
            modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(R.drawable.ic_bulb), null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
            Blocks(block.blocks, diagram, Modifier.weight(1f))
        }
        is Block.Quote -> Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)))
            Blocks(block.blocks, diagram, Modifier.padding(start = 12.dp).weight(1f))
        }
        is Block.Diagram -> DiagramPlaceholder(diagram(block.id), modifier)
        is Block.Anim -> SpCard(modifier) {
            Text(stringResource(R.string.lesson_anim_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.lesson_anim_soon), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        is Block.Prompt -> SelectionContainer(modifier) {
            Text(
                rememberInlines(block.c),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(12.dp),
            )
        }
        is Block.Checks -> Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (item in block.items) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(painterResource(R.drawable.ic_check_circle), null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
                    Blocks(item, diagram, Modifier.weight(1f))
                }
            }
        }
        is Block.Ai -> Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (col in block.cols) {
                SpCard {
                    Text(rememberInlines(col.title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                    Blocks(col.blocks, diagram)
                }
            }
        }
    }
}

@Composable
fun Blocks(blocks: List<Block>, diagram: (String) -> Diagram?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (b in blocks) LessonBlock(b, diagram)
    }
}

/**
 * Tables as one card per row, like the site's phone layout: the first cell is the row's title and every other
 * cell is shown under its column name. Works at any font size without sideways scrolling (A-003).
 */
@Composable
private fun TableBlock(table: Block.Table, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in table.rows) {
            SpCard {
                row.firstOrNull()?.let { Text(rememberInlines(it.c), style = MaterialTheme.typography.titleMedium) }
                row.drop(1).forEachIndexed { i, cell ->
                    if (i > 0) HorizontalDivider(Modifier.padding(vertical = 2.dp))
                    Text(
                        rememberInlines(table.head.getOrNull(i + 1).orEmpty()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TableCell(cell)
                }
            }
        }
    }
}

@Composable
private fun TableCell(cell: Cell) {
    val mark = cell.mark
    if (mark == null) {
        Text(rememberInlines(cell.c), style = MaterialTheme.typography.bodyMedium)
        return
    }
    // The site's Yes / No marks: icon and word, then the rest of the cell.
    val yes = mark == "yes"
    val color = if (yes) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(if (yes) R.drawable.ic_check else R.drawable.ic_x), null, tint = color, modifier = Modifier.padding(top = 2.dp).size(16.dp))
        Text(
            rememberInlines(listOf(Inline.Strong(listOf(Inline.Text(stringResource(if (yes) R.string.mark_yes else R.string.mark_no) + " ")))) + cell.c),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Until the step-through viewer (A4): the diagram's title and its numbered steps, which carry the explanation. */
@Composable
private fun DiagramPlaceholder(d: Diagram?, modifier: Modifier) {
    if (d == null) return
    Column(
        modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.lesson_diagram_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(d.title, style = MaterialTheme.typography.titleMedium)
        d.captions.forEachIndexed { i, c ->
            Row {
                Text("${i + 1}.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
                Text(c, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
        Text(stringResource(R.string.lesson_diagram_soon), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
