package io.github.ukpratik.seniorpath.feature.map

import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.content.Track
import io.github.ukpratik.seniorpath.data.ProgressState
import io.github.ukpratik.seniorpath.data.activePath
import io.github.ukpratik.seniorpath.data.continueTarget
import io.github.ukpratik.seniorpath.data.statusOf
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import io.github.ukpratik.seniorpath.ui.theme.LocalSpColors
import kotlinx.coroutines.launch

/**
 * Curriculum map (US-003): each track is a line you open to see its modules and topics, as on the site's /map.
 * "Written only" (on by default) hides coming-soon topics; tapping one of those opens [ComingSoonSheet] (US-004).
 */
@Composable
fun MapScreen(container: AppContainer, onLesson: (String) -> Unit, onPath: (String) -> Unit) {
    val loaded = rememberLoaded(Unit) { container.content.manifest }
    val progress = rememberProgress(container)
    val scope = rememberCoroutineScope()
    var sheet by rememberSaveable { mutableStateOf<String?>(null) }
    // Tracks the reader opened or closed; until then a track is open if it has written lessons.
    var toggled by rememberSaveable { mutableStateOf(emptyList<String>()) }

    Column(Modifier.fillMaxSize()) {
        val manifest = loaded?.getOrElse { ScreenTitle(stringResource(R.string.map_title)); LoadError(it); return@Column } ?: return@Column
        val showAll = progress.mapAll == true
        val open = { t: Track -> (t.id in toggled) != t.modules.any { m -> m.topics.any { manifest.topics[it]?.written == true } } }
        val openTopic = { id: String -> if (manifest.topics[id]?.written == true) onLesson(id) else sheet = id }

        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                ScreenTitle(stringResource(R.string.map_title))
                Text(
                    stringResource(R.string.map_lede, manifest.lessonCount),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { YouCard(manifest, progress, onLesson, onPath) }
            item {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .toggleable(value = !showAll, role = Role.Switch) { on -> scope.launch { container.progress.setMapAll(!on) } }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.map_written_only), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = !showAll, onCheckedChange = null)
                }
            }
            items(manifest.tracks, key = { it.id }) { track ->
                TrackCard(
                    track = track,
                    manifest = manifest,
                    progress = progress,
                    showAll = showAll,
                    open = open(track),
                    onToggle = { toggled = if (track.id in toggled) toggled - track.id else toggled + track.id },
                    onTopic = openTopic,
                )
            }
        }
        sheet?.let { id ->
            ComingSoonSheet(id, manifest, { progress.statusOf(it, manifest) }, onDismiss = { sheet = null }, onOpen = { p ->
                sheet = null
                openTopic(p)
            })
        }
    }
}

/** "New here?" with the default path, or progress with Continue once the reader has started. */
@Composable
private fun YouCard(manifest: Manifest, progress: ProgressState, onLesson: (String) -> Unit, onPath: (String) -> Unit) {
    val written = manifest.lessonCount
    val done = progress.done.keys.count { manifest.topics[it]?.written == true }
    Box(Modifier.padding(horizontal = 16.dp)) {
        SpCard {
            if (done == 0 && progress.lastOpened == null) {
                val path = progress.activePath(manifest) ?: return@SpCard
                Text(stringResource(R.string.map_new_here), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.map_new_here_body, path.title), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { onPath(path.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.map_open_path, path.title))
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.map_your_progress), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.map_done_count, done, written),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(progress = { if (written == 0) 0f else done / written.toFloat() }, modifier = Modifier.fillMaxWidth())
                val next = progress.continueTarget(manifest)
                if (next != null) {
                    Button(onClick = { onLesson(next) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.map_continue, manifest.topics.getValue(next).title))
                    }
                }
                val path = progress.activePath(manifest)
                if (path != null) {
                    OutlinedButton(onClick = { onPath(path.id) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.map_open_path, path.title)) }
                }
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: Track,
    manifest: Manifest,
    progress: ProgressState,
    showAll: Boolean,
    open: Boolean,
    onToggle: () -> Unit,
    onTopic: (String) -> Unit,
) {
    val line = LocalSpColors.current.track(track.index)
    val ids = track.modules.flatMap { it.topics }
    val written = ids.count { manifest.topics[it]?.written == true }
    val stateText = stringResource(if (open) R.string.state_open else R.string.state_closed)
    Box(Modifier.padding(horizontal = 16.dp)) {
        SpCard {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onToggle)
                    .semantics { stateDescription = stateText },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(32.dp).background(line, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${track.index}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.surface)
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(track.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                    Text(
                        stringResource(R.string.map_track_count, written, ids.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(painterResource(R.drawable.ic_chev_down), null, modifier = Modifier.rotate(if (open) 180f else 0f))
            }
            if (open) {
                for (module in track.modules) {
                    val mw = module.topics.count { manifest.topics[it]?.written == true }
                    val shown = module.topics.filter { showAll || manifest.topics[it]?.written == true }
                    if (shown.isEmpty()) continue
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Text(
                        stringResource(R.string.map_module_count, module.title, mw, module.topics.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp).semantics { heading() },
                    )
                    for (id in shown) {
                        val t = manifest.topics.getValue(id)
                        TopicRow(t.title, t.level, progress.statusOf(id, manifest), track.index, onClick = { onTopic(id) })
                    }
                }
                val soon = ids.size - written
                if (!showAll && soon > 0) {
                    Text(
                        pluralStringResource(R.plurals.map_more_soon, soon, soon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
