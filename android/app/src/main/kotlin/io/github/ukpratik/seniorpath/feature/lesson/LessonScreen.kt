package io.github.ukpratik.seniorpath.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Block
import io.github.ukpratik.seniorpath.content.ContentRepository
import io.github.ukpratik.seniorpath.content.Lesson
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.content.QuizQuestion
import io.github.ukpratik.seniorpath.content.Section
import io.github.ukpratik.seniorpath.content.Source
import io.github.ukpratik.seniorpath.data.ProgressState
import io.github.ukpratik.seniorpath.data.activePath
import io.github.ukpratik.seniorpath.data.statusOf
import io.github.ukpratik.seniorpath.feature.map.ComingSoonSheet
import io.github.ukpratik.seniorpath.feature.map.TopicRow
import io.github.ukpratik.seniorpath.ui.components.BackBar
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.levelLabel
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import kotlinx.coroutines.launch

/** One row of the lesson list; each top-level block is its own row so long lessons scroll smoothly. */
private sealed interface Item {
    val key: String

    data object Header : Item { override val key = "header" }
    data class SectionHead(val section: Section, val total: Int) : Item { override val key = "s${section.n}" }
    data class BlockRow(override val key: String, val block: Block) : Item
    data class Quiz(val index: Int, val q: QuizQuestion) : Item { override val key = "q$index" }
    data object SourcesHead : Item { override val key = "sources" }
    data class SourceRow(val n: Int, val source: Source) : Item { override val key = "src$n" }
    data object Library : Item { override val key = "library" }
    data object Related : Item { override val key = "related" }
    data object Footer : Item { override val key = "footer" }
}

private fun items(lesson: Lesson): List<Item> = buildList {
    add(Item.Header)
    for (s in lesson.sections) {
        add(Item.SectionHead(s, lesson.sections.size))
        s.blocks.forEachIndexed { i, b -> add(Item.BlockRow("s${s.n}b$i", b)) }
        // The quiz lives in "Check yourself"; sources close "Go deeper", as on the site.
        if (s.name == "Check yourself") lesson.quiz.forEachIndexed { i, q -> add(Item.Quiz(i, q)) }
        if (s.name == "Go deeper") {
            if (lesson.sources.isNotEmpty()) {
                add(Item.SourcesHead)
                lesson.sources.forEachIndexed { i, src -> add(Item.SourceRow(i + 1, src)) }
            }
            if (lesson.incidents.isNotEmpty() || lesson.cases.isNotEmpty()) add(Item.Library)
            if (lesson.related.isNotEmpty()) add(Item.Related)
        }
    }
    add(Item.Footer)
}

private class Loaded(val lesson: Lesson, val manifest: Manifest, val repo: ContentRepository)

/**
 * The lesson reader (US-005): the fixed 8 sections, tappable glossary terms (US-011), citations that jump to the
 * sources (US-013), links that open in the browser (US-014), a contents sheet, and Previous / Next on the reader's path.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(container: AppContainer, id: String, onLesson: (String) -> Unit, onBack: () -> Unit) {
    val loaded = rememberLoaded(id) {
        val repo = container.content
        Loaded(repo.lesson(id), repo.manifest, repo).also { repo.glossary; repo.incidents; repo.cases; it.lesson.sections.forEach { s -> s.blocks.forEach { b -> if (b is Block.Diagram) repo.diagram(b.id) } } }
    }
    val progress = rememberProgress(container)
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var term by rememberSaveable { mutableStateOf<String?>(null) }
    var contents by rememberSaveable { mutableStateOf(false) }
    var soon by rememberSaveable { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        BackBar(null, onBack) {
            if (loaded?.isSuccess == true) {
                IconButton(onClick = { contents = true }) {
                    Icon(painterResource(R.drawable.ic_list), contentDescription = stringResource(R.string.lesson_contents))
                }
            }
        }
        val data = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
        val lesson = data.lesson
        val rows = remember(lesson) { items(lesson) }
        val indexOf = { key: String -> rows.indexOfFirst { it.key == key }.coerceAtLeast(0) }
        val openTopic = { t: String -> if (data.manifest.topics[t]?.written == true) onLesson(t) else soon = t }
        val actions = InlineActions(
            onTerm = { term = it },
            onCite = { n -> scope.launch { list.animateScrollToItem(indexOf("src$n")) } },
        )

        CompositionLocalProvider(LocalInlineActions provides actions) {
            LazyColumn(
                state = list,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(rows, key = { it.key }) { row ->
                    when (row) {
                        Item.Header -> Header(lesson, data.manifest)
                        is Item.SectionHead -> SectionHeader(row.section, row.total)
                        is Item.BlockRow -> LessonBlock(row.block, { data.repo.diagram(it) })
                        is Item.Quiz -> QuizCard(row.index, row.q)
                        Item.SourcesHead -> Text(
                            stringResource(R.string.lesson_sources),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                        )
                        is Item.SourceRow -> SourceRow(row.n, row.source)
                        Item.Library -> Library(lesson, data.repo)
                        Item.Related -> RelatedTopics(lesson, data.manifest, progress, openTopic)
                        Item.Footer -> Footer(lesson, data.manifest, progress, onLesson)
                    }
                }
            }
        }

        if (contents) {
            ModalBottomSheet(onDismissRequest = { contents = false }) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp).navigationBarsPadding()) {
                    Text(
                        stringResource(R.string.lesson_contents),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).semantics { heading() },
                    )
                    for (s in lesson.sections) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button) {
                                contents = false
                                scope.launch { list.animateScrollToItem(indexOf("s${s.n}")) }
                            }.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${s.n}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp))
                            Text(s.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        term?.let { t ->
            val entry = data.repo.glossaryById[t]
            ModalBottomSheet(onDismissRequest = { term = null }) {
                Column(
                    Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp).navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.lesson_glossary), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(entry?.term ?: t, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    Text(entry?.short.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        soon?.let { s ->
            ComingSoonSheet(s, data.manifest, { progress.statusOf(it, data.manifest) }, onDismiss = { soon = null }, onOpen = { p ->
                soon = null
                openTopic(p)
            })
        }
    }
}

@Composable
private fun Header(lesson: Lesson, manifest: Manifest) {
    val track = manifest.tracks.firstOrNull { it.id == lesson.track }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            listOfNotNull(track?.title, levelLabel(lesson.level), stringResource(R.string.lesson_minutes, lesson.readingMinutes)).joinToString(" · "),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(lesson.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(lesson.subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (lesson.illustrativeStory) {
            Text(stringResource(R.string.lesson_illustrative), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(section: Section, total: Int) {
    Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HorizontalDivider(Modifier.padding(bottom = 12.dp))
        Text(
            stringResource(R.string.lesson_section_chip, section.n, total),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
        )
        Text(section.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
    }
}

/** Check yourself (US-008): pick an answer, see whether it's right and why. Nothing is stored. */
@Composable
private fun QuizCard(index: Int, q: QuizQuestion) {
    var picked by rememberSaveable(index) { mutableStateOf<Int?>(null) }
    SpCard {
        Text(stringResource(R.string.quiz_question, index + 1), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(rememberInlines(q.q), style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            q.options.forEachIndexed { i, opt ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected = picked == i, role = Role.RadioButton) { picked = i }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = picked == i, onClick = null)
                    Text(rememberInlines(opt), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        picked?.let { p ->
            val right = p == q.answer
            Column(
                Modifier.fillMaxWidth()
                    .background(if (right) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(if (right) R.string.quiz_right else R.string.quiz_wrong),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (right) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(rememberInlines(q.why), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SourceRow(n: Int, source: Source) {
    val uri = LocalUriHandler.current
    val url = source.url
    Row(
        Modifier.fillMaxWidth().then(if (url != null) Modifier.clickable(role = Role.Button) { uri.openUri(url) } else Modifier).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("[$n]", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(36.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(source.title, style = MaterialTheme.typography.bodyMedium, color = if (url != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            listOfNotNull(source.where, source.note).forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (url != null) Icon(painterResource(R.drawable.ic_ext), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
    }
}

/** The lesson's real incidents and case studies; each opens the original write-up. The libraries come in A6. */
@Composable
private fun Library(lesson: Lesson, repo: ContentRepository) {
    val uri = LocalUriHandler.current
    val incidents = repo.incidents.filter { it.id in lesson.incidents }
    val cases = repo.cases.filter { it.id in lesson.cases }
    @Composable
    fun Entry(label: String, url: String) {
        Row(
            Modifier.fillMaxWidth().clickable(role = Role.Button) { uri.openUri(url) }.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_ext), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
    }
    Column {
        if (incidents.isNotEmpty()) {
            Text(stringResource(R.string.lesson_incidents), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            for (i in incidents) Entry("${i.company}, ${i.date}: ${i.title}", i.url)
        }
        if (cases.isNotEmpty()) {
            Text(stringResource(R.string.lesson_cases), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            for (c in cases) Entry("${c.company}: ${c.title}", c.url)
        }
    }
}

@Composable
private fun RelatedTopics(lesson: Lesson, manifest: Manifest, progress: ProgressState, onTopic: (String) -> Unit) {
    Column {
        Text(
            stringResource(R.string.lesson_related),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).semantics { heading() },
        )
        for (id in lesson.related) {
            val t = manifest.topics[id] ?: continue
            TopicRow(t.title, t.level, progress.statusOf(id, manifest), manifest.tracks.firstOrNull { it.id == t.track }?.index ?: 1, onClick = { onTopic(id) })
        }
    }
}

/** Previous / Next from the reader's path (US-018), skipping lessons not written yet, as on the site. */
@Composable
private fun Footer(lesson: Lesson, manifest: Manifest, progress: ProgressState, onLesson: (String) -> Unit) {
    val path = progress.activePath(manifest) ?: return
    val steps = path.steps.filter { manifest.topics[it]?.written == true }
    val i = steps.indexOf(lesson.id)
    if (i < 0) return
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(
            stringResource(R.string.lesson_on_path, path.title, i + 1, steps.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        steps.getOrNull(i - 1)?.let { prev ->
            OutlinedButton(onClick = { onLesson(prev) }, modifier = Modifier.fillMaxWidth()) {
                Icon(painterResource(R.drawable.ic_arrow_left), null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.lesson_prev, manifest.topics.getValue(prev).title), modifier = Modifier.padding(start = 8.dp).weight(1f, fill = false))
            }
        }
        steps.getOrNull(i + 1)?.let { next ->
            OutlinedButton(onClick = { onLesson(next) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.lesson_next, manifest.topics.getValue(next).title), modifier = Modifier.padding(end = 8.dp).weight(1f, fill = false))
                Icon(painterResource(R.drawable.ic_arrow_right), null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
