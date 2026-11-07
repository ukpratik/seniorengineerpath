package io.github.ukpratik.seniorpath.feature.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.CaseStudy
import io.github.ukpratik.seniorpath.content.GlossaryEntry
import io.github.ukpratik.seniorpath.content.Incident
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.ui.components.BackBar
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val day = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK)

/** The site's formatDay: "28 Feb 2017". */
fun formatDay(iso: String): String = runCatching { day.format(LocalDate.parse(iso)) }.getOrDefault(iso)

/** Incident library (US-019): what broke, why and the lesson, filtered by tag; each links to the official postmortem. */
@Composable
fun IncidentsScreen(container: AppContainer, focus: String?, onLesson: (String) -> Unit, onBack: () -> Unit) {
    val loaded = rememberLoaded(Unit) { container.content.incidents to container.content.manifest }
    Library(
        title = stringResource(R.string.library_incidents),
        lede = stringResource(R.string.library_incidents_lede),
        loaded = loaded,
        focus = focus,
        id = Incident::id,
        tags = Incident::tags,
        count = { pluralStringResource(R.plurals.library_incident_count, it, it) },
        onBack = onBack,
    ) { i, manifest, focused ->
        EntryCard(
            focused = focused,
            meta = "${i.company} · ${formatDay(i.date)}",
            metaIcon = R.drawable.ic_alert,
            title = i.title,
            parts = listOf(R.string.incident_broke to i.broke, R.string.incident_why to i.why, R.string.incident_lesson to i.lesson),
            tags = i.tags,
            link = stringResource(R.string.incident_link) to i.url,
            lessons = i.lessons,
            manifest = manifest,
            onLesson = onLesson,
        )
    }
}

/** Case-study library (US-020): how real companies built things, filtered by tag. */
@Composable
fun CasesScreen(container: AppContainer, focus: String?, onLesson: (String) -> Unit, onBack: () -> Unit) {
    val loaded = rememberLoaded(Unit) { container.content.cases to container.content.manifest }
    Library(
        title = stringResource(R.string.library_cases),
        lede = stringResource(R.string.library_cases_lede),
        loaded = loaded,
        focus = focus,
        id = CaseStudy::id,
        tags = CaseStudy::tags,
        count = { pluralStringResource(R.plurals.library_case_count, it, it) },
        onBack = onBack,
    ) { c, manifest, focused ->
        EntryCard(
            focused = focused,
            meta = c.company,
            metaIcon = R.drawable.ic_briefcase,
            title = c.title,
            parts = listOf(R.string.case_lesson to c.lesson),
            tags = c.tags,
            link = stringResource(R.string.case_link) to c.url,
            lessons = c.lessons,
            manifest = manifest,
            onLesson = onLesson,
        )
    }
}

@Composable
private fun <T> Library(
    title: String,
    lede: String,
    loaded: Result<Pair<List<T>, Manifest>>?,
    focus: String?,
    id: (T) -> String,
    tags: (T) -> List<String>,
    count: @Composable (Int) -> String,
    onBack: () -> Unit,
    card: @Composable (T, Manifest, Boolean) -> Unit,
) {
    var tag by rememberSaveable { mutableStateOf<String?>(null) }
    val list = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        BackBar(title, onBack)
        val (all, manifest) = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
        val allTags = all.flatMap(tags).distinct().sorted()
        val shown = if (tag == null) all else all.filter { tag in tags(it) }
        // Opened from search or a lesson: scroll to that entry once.
        LaunchedEffect(focus, all.size) {
            val i = all.indexOfFirst { id(it) == focus }
            if (i >= 0) list.scrollToItem(i + 3)
        }
        LazyColumn(state = list, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(key = "lede") { Text(lede, style = MaterialTheme.typography.bodyLarge) }
            item(key = "tags") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(selected = tag == null, onClick = { tag = null }, label = { Text(stringResource(R.string.library_all)) })
                    for (t in allTags) FilterChip(selected = tag == t, onClick = { tag = if (tag == t) null else t }, label = { Text(t) })
                }
            }
            item(key = "count") {
                Text(
                    count(shown.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            items(shown, key = id) { card(it, manifest, id(it) == focus) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntryCard(
    focused: Boolean,
    meta: String,
    metaIcon: Int,
    title: String,
    parts: List<Pair<Int, String>>,
    tags: List<String>,
    link: Pair<String, String>,
    lessons: List<String>,
    manifest: Manifest,
    onLesson: (String) -> Unit,
) {
    val uri = LocalUriHandler.current
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = if (focused) BorderStroke(2.dp, scheme.primary) else CardDefaults.outlinedCardBorder(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(painterResource(metaIcon), null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                Text(meta, style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            for ((label, text) in parts) {
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = scheme.primary, modifier = Modifier.padding(top = 4.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                for (t in tags) {
                    Text(
                        t,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSecondaryContainer,
                        modifier = Modifier.background(scheme.secondaryContainer, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            LinkRow(R.drawable.ic_ext, link.first) { uri.openUri(link.second) }
            for (l in lessons) {
                val t = manifest.topics[l]?.takeIf { it.written } ?: continue
                LinkRow(R.drawable.ic_book, t.title) { onLesson(l) }
            }
        }
    }
}

@Composable
private fun LinkRow(icon: Int, text: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

/** The glossary, A to Z (the site's /glossary). */
@Composable
fun GlossaryScreen(container: AppContainer, onBack: () -> Unit) {
    val loaded = rememberLoaded(Unit) { container.content.glossary.sortedBy { it.term.lowercase() } }
    Column(Modifier.fillMaxSize()) {
        BackBar(stringResource(R.string.glossary_title), onBack)
        val terms: List<GlossaryEntry> = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            item(key = "lede") {
                Text(
                    stringResource(R.string.glossary_lede),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            var letter: Char? = null
            for (g in terms) {
                val first = g.term.first().uppercaseChar().takeIf { it.isLetter() } ?: '#'
                if (first != letter) {
                    letter = first
                    item(key = "letter-$first") {
                        Text(
                            first.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).semantics { heading() },
                        )
                    }
                }
                item(key = g.id) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(g.term, style = MaterialTheme.typography.titleMedium)
                        Text(g.short, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
