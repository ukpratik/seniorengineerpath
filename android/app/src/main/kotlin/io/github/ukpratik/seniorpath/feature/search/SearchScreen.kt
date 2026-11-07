package io.github.ukpratik.seniorpath.feature.search

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.content.SearchHit
import io.github.ukpratik.seniorpath.content.SearchIndex
import io.github.ukpratik.seniorpath.data.statusOf
import io.github.ukpratik.seniorpath.feature.map.ComingSoonSheet
import io.github.ukpratik.seniorpath.ui.components.GlossarySheet
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Where a search result or a library link leads. */
data class SearchTargets(
    val onLesson: (String) -> Unit,
    val onIncident: (String?) -> Unit,
    val onCase: (String?) -> Unit,
    val onGlossary: () -> Unit,
)

private class Loaded(val index: SearchIndex, val manifest: Manifest, val counts: Triple<Int, Int, Int>)

private val SUGGESTIONS = listOf("idempotency", "retry", "outage", "senior", "timeout", "DNS") // as on the site

/** Search tab (US-012): every word, offline, grouped like the site's results; libraries to browse when empty. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(container: AppContainer, go: SearchTargets) {
    val loaded = rememberLoaded(Unit) {
        val repo = container.content
        Loaded(repo.searchIndex, repo.manifest, Triple(repo.incidents.size, repo.cases.size, repo.glossary.size))
    }
    val progress = rememberProgress(container)
    var query by rememberSaveable { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<SearchHit>?>(null) }
    var term by rememberSaveable { mutableStateOf<String?>(null) }
    var soon by rememberSaveable { mutableStateOf<String?>(null) }
    val focus = LocalFocusManager.current

    LaunchedEffect(query, loaded) {
        val data = loaded?.getOrNull() ?: return@LaunchedEffect
        if (query.trim().length < 2) { hits = null; return@LaunchedEffect }
        delay(150) // typing quickly doesn't search every letter
        hits = withContext(Dispatchers.Default) { data.index.search(query) }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle(stringResource(R.string.search_title))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) { Icon(painterResource(R.drawable.ic_x), stringResource(R.string.search_clear)) }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        )
        val data = loaded?.getOrElse { LoadError(it); return@Column }
        if (data == null) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        val list = hits
        when {
            query.trim().length < 2 -> Browse(data, go, onSuggestion = { query = it })
            list == null -> Unit
            list.isEmpty() -> Column(Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                Text(stringResource(R.string.search_none, query.trim()), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.search_none_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> Results(list, data.manifest, onOpen = { hit ->
                focus.clearFocus()
                val e = hit.entry
                when (e.type) {
                    "lesson" -> go.onLesson(e.id)
                    "topic" -> soon = e.id
                    "glossary" -> term = e.id
                    "incident" -> go.onIncident(e.id)
                    "case" -> go.onCase(e.id)
                }
            })
        }
        term?.let { GlossarySheet(container.content.glossaryById[it], it, onDismiss = { term = null }) }
        soon?.let { s ->
            ComingSoonSheet(s, data.manifest, { progress.statusOf(it, data.manifest) }, onDismiss = { soon = null }, onOpen = { p ->
                soon = null
                if (data.manifest.topics[p]?.written == true) go.onLesson(p) else soon = p
            })
        }
    }
}

@Composable
private fun Browse(data: Loaded, go: SearchTargets, onSuggestion: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 16.dp, bottom = 16.dp)) {
        Text(
            stringResource(R.string.search_try),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (s in SUGGESTIONS) AssistChip(onClick = { onSuggestion(s) }, label = { Text(s) })
        }
        Text(
            stringResource(R.string.search_browse),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp).semantics { heading() },
        )
        val (inc, cases, terms) = data.counts
        BrowseRow(R.drawable.ic_alert, stringResource(R.string.library_incidents), pluralStringResource(R.plurals.library_incident_count, inc, inc)) { go.onIncident(null) }
        BrowseRow(R.drawable.ic_briefcase, stringResource(R.string.library_cases), pluralStringResource(R.plurals.library_case_count, cases, cases)) { go.onCase(null) }
        BrowseRow(R.drawable.ic_text, stringResource(R.string.glossary_title), pluralStringResource(R.plurals.glossary_count, terms, terms), go.onGlossary)
    }
}

@Composable
private fun BrowseRow(@DrawableRes icon: Int, title: String, meta: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(painterResource(R.drawable.ic_chev_right), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val GROUP_ICON = mapOf(
    "lesson" to R.drawable.ic_book, "glossary" to R.drawable.ic_text, "incident" to R.drawable.ic_alert,
    "case" to R.drawable.ic_briefcase, "topic" to R.drawable.ic_dashed,
)

@Composable
private fun groupLabel(type: String): String = stringResource(
    when (type) {
        "lesson" -> R.string.search_group_lessons
        "glossary" -> R.string.search_group_glossary
        "incident" -> R.string.search_group_incidents
        "case" -> R.string.search_group_cases
        else -> R.string.search_group_soon
    },
)

@Composable
private fun Results(hits: List<SearchHit>, manifest: Manifest, onOpen: (SearchHit) -> Unit) {
    // Grouped in the site's order; inside a group, best match first.
    val groups = SearchIndex.TYPE_ORDER.mapNotNull { t -> hits.filter { it.entry.type == t }.takeIf { it.isNotEmpty() }?.let { t to it } }
    val mark = SpanStyle(fontWeight = FontWeight.Bold, background = MaterialTheme.colorScheme.primaryContainer, color = MaterialTheme.colorScheme.onPrimaryContainer)
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item(key = "count") {
            Text(
                pluralStringResource(R.plurals.search_results, hits.size, hits.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        for ((type, items) in groups) {
            item(key = "h-$type") {
                Text(
                    groupLabel(type),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp).semantics { heading() },
                )
            }
            items(items, key = { "${it.entry.type}:${it.entry.id}" }) { hit ->
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { onOpen(hit) }.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(painterResource(GROUP_ICON.getValue(type)), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp).size(20.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(marked(hit.entry.title, hit.titleMarks, mark), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        if (type == "topic") {
                            manifest.topics[hit.entry.id]?.let { t ->
                                Text(stringResource(R.string.search_soon_label, manifest.tracks.firstOrNull { it.id == t.track }?.short.orEmpty()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            marked(hit.snippet, hit.marks, mark),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(start = 50.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

private fun marked(text: String, marks: List<IntRange>, style: SpanStyle): AnnotatedString = buildAnnotatedString {
    append(text)
    for (r in marks) if (r.last < text.length) addStyle(style, r.first, r.last + 1)
}
