package io.github.ukpratik.seniorpath.feature.paths

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.data.activePath
import io.github.ukpratik.seniorpath.data.nextStep
import io.github.ukpratik.seniorpath.data.statusOf
import io.github.ukpratik.seniorpath.feature.map.ComingSoonSheet
import io.github.ukpratik.seniorpath.feature.map.TopicRow
import io.github.ukpratik.seniorpath.ui.components.BackBar
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import kotlinx.coroutines.launch

/** Every learning path (US-018), with the reader's own marked. */
@Composable
fun PathsScreen(container: AppContainer, onPath: (String) -> Unit, onBack: () -> Unit) {
    val loaded = rememberLoaded(Unit) { container.content.manifest }
    val progress = rememberProgress(container)
    Column(Modifier.fillMaxSize()) {
        BackBar(stringResource(R.string.paths_title), onBack)
        val manifest = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
        val active = progress.activePath(manifest)?.id
        LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text(stringResource(R.string.paths_lede), style = MaterialTheme.typography.bodyLarge) }
            items(manifest.paths, key = { it.id }) { path ->
                val done = path.steps.count { it in progress.done }
                SpCard(Modifier.clickable(role = Role.Button) { onPath(path.id) }) {
                    if (path.id == active) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(painterResource(R.drawable.ic_route), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.path_yours_badge), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(path.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    Text(path.description, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        stringResource(R.string.path_card_meta, pluralStringResource(R.plurals.home_path_steps, path.steps.size, path.steps.size), done),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** One path in order (US-018): progress, the next lesson, and "Make this my path", as on the site's /paths/<slug>. */
@Composable
fun PathScreen(container: AppContainer, id: String, onLesson: (String) -> Unit, onBack: () -> Unit) {
    val loaded = rememberLoaded(id) { container.content.manifest }
    val progress = rememberProgress(container)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var sheet by rememberSaveable { mutableStateOf<String?>(null) }
    val madeActive = stringResource(R.string.path_made_active)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BackBar(null, onBack)
            val manifest = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
            val path = manifest.paths.firstOrNull { it.id == id } ?: run { LoadError(IllegalArgumentException(id)); return@Column }
            val openTopic = { t: String -> if (manifest.topics[t]?.written == true) onLesson(t) else sheet = t }
            val written = path.steps.count { manifest.topics[it]?.written == true }
            val done = path.steps.count { it in progress.done }
            val next = progress.nextStep(path, manifest)
            val isActive = progress.activePath(manifest)?.id == path.id

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.path_eyebrow), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(path.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                    Text(stringResource(R.string.path_lede, path.description), style = MaterialTheme.typography.bodyLarge)
                    SpCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.map_your_progress), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(
                                stringResource(R.string.path_done_count, done, path.steps.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        LinearProgressIndicator(progress = { done / path.steps.size.coerceAtLeast(1).toFloat() }, modifier = Modifier.fillMaxWidth())
                        Text(
                            stringResource(R.string.path_written_count, path.steps.size, written, path.steps.size - written),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (next != null) {
                            Button(onClick = { onLesson(next.value) }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(if (done == 0) R.string.path_start else R.string.path_next, next.index + 1))
                            }
                        }
                        if (isActive) {
                            // A label, not a disabled button: disabled text is too faint to read.
                            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(painterResource(R.drawable.ic_route), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    stringResource(R.string.path_is_active),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        container.progress.setActivePath(path.id)
                                        snackbar.showSnackbar(madeActive)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(painterResource(R.drawable.ic_route), null, modifier = Modifier.size(18.dp))
                                Text(stringResource(R.string.path_make_active), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        Text(stringResource(R.string.path_active_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    stringResource(R.string.path_steps_heading),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp).semantics { heading() },
                )
                Column(Modifier.padding(start = 4.dp, end = 4.dp, bottom = 16.dp)) {
                    path.steps.forEachIndexed { i, step ->
                        val t = manifest.topics[step] ?: return@forEachIndexed
                        val track = manifest.tracks.firstOrNull { it.id == t.track }
                        TopicRow(
                            title = t.title,
                            level = t.level,
                            status = progress.statusOf(step, manifest),
                            trackIndex = track?.index ?: 1,
                            onClick = { openTopic(step) },
                            number = i + 1,
                            meta = track?.short,
                        )
                    }
                }
            }
            sheet?.let { s ->
                ComingSoonSheet(s, manifest, { progress.statusOf(it, manifest) }, onDismiss = { sheet = null }, onOpen = { p ->
                    sheet = null
                    openTopic(p)
                })
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
