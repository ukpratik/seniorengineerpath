package io.github.ukpratik.seniorpath.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.data.activePath
import io.github.ukpratik.seniorpath.data.nextStep
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import io.github.ukpratik.seniorpath.ui.components.levelLabel
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.data.ProgressState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch

/**
 * "Start here" (US-001): Continue where you left off (US-002), what's new since the last update (A-004),
 * what's in the build, and the reader's path with its next lesson.
 */
@Composable
fun HomeScreen(container: AppContainer, onLesson: (String) -> Unit, onPath: (String) -> Unit, onPaths: () -> Unit) {
    val loaded = rememberLoaded(Unit) { container.content.manifest }
    val progress = rememberProgress(container)
    val scope = rememberCoroutineScope()
    var fresh by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(loaded?.isSuccess) {
        loaded?.getOrNull()?.let { m -> fresh = container.settings.unseenLessons(m.lessons) }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle(stringResource(R.string.home_title))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.home_intro), style = MaterialTheme.typography.bodyLarge)
            val manifest = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
            val path = progress.activePath(manifest)
            continueCard(progress, manifest)?.let { c -> ContinueCard(c, manifest, onLesson) }
            if (fresh.isNotEmpty()) {
                WhatsNewCard(fresh, manifest, onLesson, onDismiss = {
                    val seen = manifest.lessons
                    fresh = emptyList()
                    scope.launch { container.settings.markLessonsSeen(seen) }
                })
            }
            SpCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_book), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        pluralStringResource(R.plurals.home_lessons, manifest.lessonCount, manifest.lessonCount) + " · " +
                            pluralStringResource(R.plurals.home_topics, manifest.topics.size, manifest.topics.size),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
            if (path != null) {
                SpCard(Modifier.clickable(role = Role.Button) { onPath(path.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_route), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            stringResource(if (progress.activePath == path.id) R.string.home_your_path else R.string.home_path_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                    Text(path.title, style = MaterialTheme.typography.titleLarge)
                    Text(path.description, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        pluralStringResource(R.plurals.home_path_steps, path.steps.size, path.steps.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val next = progress.nextStep(path, manifest)
                    if (next != null) {
                        Button(onClick = { onLesson(next.value) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.home_next_lesson, next.index + 1, manifest.topics.getValue(next.value).title))
                        }
                    }
                }
            }
            OutlinedButton(onClick = onPaths, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.home_all_paths)) }
            Text(
                stringResource(R.string.home_offline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

/** What the Continue card shows; the same rules as the site's home page. */
data class ContinueInfo(val id: String, val next: Boolean, val pathTitle: String, val position: Int?, val steps: Int, val doneOnPath: Int)

fun continueCard(progress: ProgressState, manifest: Manifest): ContinueInfo? {
    val last = progress.lastOpened?.takeIf { manifest.topics[it]?.written == true } ?: return null
    val path = progress.activePath(manifest) ?: return null
    val idx = path.steps.indexOf(last)
    // If the last lesson is done, offer the next written lesson on the path instead.
    val nextId = if (last in progress.done && idx >= 0) path.steps.drop(idx + 1).firstOrNull { manifest.topics[it]?.written == true } else null
    val show = nextId ?: last
    val pos = path.steps.indexOf(show)
    return ContinueInfo(show, nextId != null, path.title, pos.takeIf { it >= 0 }?.plus(1), path.steps.size, path.steps.count { it in progress.done })
}

@Composable
private fun ContinueCard(c: ContinueInfo, manifest: Manifest, onLesson: (String) -> Unit) {
    val t = manifest.topics.getValue(c.id)
    val track = manifest.tracks.firstOrNull { it.id == t.track }
    SpCard {
        Text(
            stringResource(R.string.continue_eyebrow),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            if (c.position != null) stringResource(R.string.continue_meta, c.position, c.steps, c.pathTitle) else stringResource(R.string.continue_last_opened),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(t.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Text(
            listOfNotNull(track?.title, levelLabel(t.level)).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = { onLesson(c.id) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (c.next) R.string.continue_start_next else R.string.continue_lesson))
            Icon(painterResource(R.drawable.ic_arrow_right), null, modifier = Modifier.padding(start = 8.dp).size(18.dp))
        }
        Text(
            pluralStringResource(R.plurals.continue_path_done, c.steps, c.pathTitle, c.doneOnPath, c.steps),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(progress = { c.doneOnPath / c.steps.coerceAtLeast(1).toFloat() }, modifier = Modifier.fillMaxWidth())
    }
}

/** A-004: lessons added since the reader last dismissed this card. */
@Composable
private fun WhatsNewCard(ids: List<String>, manifest: Manifest, onLesson: (String) -> Unit, onDismiss: () -> Unit) {
    SpCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                pluralStringResource(R.plurals.whats_new_title, ids.size, ids.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            IconButton(onClick = onDismiss) {
                Icon(painterResource(R.drawable.ic_x), contentDescription = stringResource(R.string.whats_new_dismiss))
            }
        }
        for (id in ids.take(8)) {
            val t = manifest.topics[id] ?: continue
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(role = Role.Button) { onLesson(id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(t.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_chev_right), null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (ids.size > 8) {
            Text(stringResource(R.string.whats_new_more, ids.size - 8), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
