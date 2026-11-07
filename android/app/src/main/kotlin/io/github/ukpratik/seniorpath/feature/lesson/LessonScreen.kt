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
import io.github.ukpratik.seniorpath.feature.library.formatDay
import io.github.ukpratik.seniorpath.ui.components.GlossarySheet
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.levelLabel
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import io.github.ukpratik.seniorpath.data.ReadingSpot
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

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
    data object Done : Item { override val key = "done" }
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
    add(Item.Done)
    add(Item.Footer)
}

private class Loaded(val lesson: Lesson, val manifest: Manifest, val repo: ContentRepository)

/**
 * The lesson reader (US-005): the fixed 8 sections, tappable glossary terms (US-011), citations that jump to the
 * sources (US-013), links that open in the browser (US-014), a contents sheet, and Previous / Next on the reader's path.
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun LessonScreen(container: AppContainer, id: String, onLesson: (String) -> Unit, onBack: () -> Unit, onProgress: () -> Unit = {}, onIncident: (String) -> Unit = {}, onCase: (String) -> Unit = {}) {
    val loaded = rememberLoaded(id) {
        val repo = container.content
        Loaded(repo.lesson(id), repo.manifest, repo).also { container.diagrams; repo.glossary; repo.incidents; repo.cases; it.lesson.sections.forEach { s -> s.blocks.forEach { b -> if (b is Block.Diagram) repo.diagram(b.id) } } }
    }
    val progress = rememberProgress(container)
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var term by rememberSaveable { mutableStateOf<String?>(null) }
    var contents by rememberSaveable { mutableStateOf(false) }
    var soon by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    // US-002: remember this lesson as the one in progress, and come back to the same place in it.
    LaunchedEffect(id) { container.progress.setLastOpened(id) }
    var restored by rememberSaveable(id) { mutableStateOf(false) }
    LaunchedEffect(id, loaded?.isSuccess) {
        if (loaded?.isSuccess != true) return@LaunchedEffect
        if (!restored) {
            container.settings.readingSpot()?.takeIf { it.lesson == id }?.let { list.scrollToItem(it.index, it.offset) }
            restored = true
        }
        snapshotFlow { list.firstVisibleItemIndex to list.firstVisibleItemScrollOffset }
            .debounce(400)
            .collect { (i, o) -> container.settings.setReadingSpot(ReadingSpot(id, i, o)) }
    }

    Box(Modifier.fillMaxSize()) {
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

        CompositionLocalProvider(LocalInlineActions provides actions, LocalDiagramRenderer provides container.diagrams) {
            LazyColumn(
                state = list,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(rows, key = { it.key }) { row ->
                    when (row) {
                        Item.Header -> Header(lesson, data.manifest, updatedSinceDone(lesson, progress))
                        is Item.SectionHead -> SectionHeader(row.section, row.total)
                        is Item.BlockRow -> LessonBlock(row.block, { data.repo.diagram(it) })
                        is Item.Quiz -> QuizCard(row.index, row.q)
                        Item.SourcesHead -> Text(
                            stringResource(R.string.lesson_sources),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                        )
                        is Item.SourceRow -> SourceRow(row.n, row.source)
                        Item.Library -> Library(lesson, data.repo, onIncident, onCase)
                        Item.Related -> RelatedTopics(lesson, data.manifest, progress, openTopic)
                        Item.Done -> DoneButton(lesson, data.manifest, progress, container, snackbar, onProgress)
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
        term?.let { t -> GlossarySheet(data.repo.glossaryById[t], t, onDismiss = { term = null }) }
        soon?.let { s ->
            ComingSoonSheet(s, data.manifest, { progress.statusOf(it, data.manifest) }, onDismiss = { soon = null }, onOpen = { p ->
                soon = null
                openTopic(p)
            })
        }
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

/** The site's "Updated since you finished it": the lesson had a major revision after the reader marked it done. */
private fun updatedSinceDone(lesson: Lesson, progress: ProgressState): Boolean {
    val doneAt = progress.done[lesson.id] ?: return false
    val revised = lesson.majorRevisedOn ?: return false
    return revised > doneAt.take(10)
}

/** Mark as done (US-009), at the end of the lesson as on the site. The fifth lesson done suggests an export, once. */
@Composable
private fun DoneButton(
    lesson: Lesson,
    manifest: Manifest,
    progress: ProgressState,
    container: AppContainer,
    snackbar: SnackbarHostState,
    onProgress: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val done = lesson.id in progress.done
    val next = progress.activePath(manifest)?.let { p ->
        val steps = p.steps.filter { manifest.topics[it]?.written == true }
        steps.indexOf(lesson.id).takeIf { it >= 0 }?.let { steps.getOrNull(it + 1) }
    }?.let { manifest.topics[it]?.title }
    val doneMsg = if (next != null) stringResource(R.string.done_toast_next, next) else stringResource(R.string.done_toast)
    val undoneMsg = stringResource(R.string.done_undone)
    val promptMsg = stringResource(R.string.done_export_prompt)
    val promptAction = stringResource(R.string.done_export_action)
    val label = stringResource(if (done) R.string.done_undo else R.string.done_mark)
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        val onClick: () -> Unit = {
            scope.launch {
                if (done) {
                    container.progress.undoDone(lesson.id)
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar(undoneMsg)
                } else {
                    val s = container.progress.markDone(lesson.id)
                    snackbar.currentSnackbarData?.dismiss()
                    if (s.done.size >= 5 && s.exportPrompted != true) {
                        container.progress.setExportPrompted()
                        val r = snackbar.showSnackbar(promptMsg, actionLabel = promptAction, duration = SnackbarDuration.Long)
                        if (r == SnackbarResult.ActionPerformed) onProgress()
                    } else {
                        snackbar.showSnackbar(doneMsg)
                    }
                }
            }
        }
        val icon = @Composable { Icon(painterResource(if (done) R.drawable.ic_check else R.drawable.ic_check_circle), null, modifier = Modifier.size(20.dp)) }
        val text = @Composable { Text(label, modifier = Modifier.padding(start = 8.dp)) }
        if (done) {
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { icon(); text() }
        } else {
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { icon(); text() }
        }
    }
}

@Composable
private fun Header(lesson: Lesson, manifest: Manifest, updated: Boolean) {
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
        if (updated) {
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp)).padding(12.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Text(stringResource(R.string.lesson_updated_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(stringResource(R.string.lesson_updated_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
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

/** The lesson's real incidents and case studies; each opens its card in the library, as on the site. */
@Composable
private fun Library(lesson: Lesson, repo: ContentRepository, onIncident: (String) -> Unit, onCase: (String) -> Unit) {
    val incidents = repo.incidents.filter { it.id in lesson.incidents }
    val cases = repo.cases.filter { it.id in lesson.cases }
    @Composable
    fun Entry(label: String, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onClick).padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_chev_right), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
    }
    Column {
        if (incidents.isNotEmpty()) {
            Text(stringResource(R.string.lesson_incidents), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            for (i in incidents) Entry("${i.company}, ${formatDay(i.date)}: ${i.title}") { onIncident(i.id) }
        }
        if (cases.isNotEmpty()) {
            Text(stringResource(R.string.lesson_cases), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            for (c in cases) Entry("${c.company}: ${c.title}") { onCase(c.id) }
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
