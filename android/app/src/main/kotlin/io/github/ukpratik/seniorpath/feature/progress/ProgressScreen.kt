package io.github.ukpratik.seniorpath.feature.progress

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.BuildConfig
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.data.ThemeMode
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.components.rememberProgress
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.levelLabel
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.data.ImportResult
import io.github.ukpratik.seniorpath.data.MAX_IMPORT_BYTES
import io.github.ukpratik.seniorpath.data.ProgressState
import io.github.ukpratik.seniorpath.data.parseTime
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private data class ThemeOption(val mode: ThemeMode, val label: Int, @param:DrawableRes val icon: Int)

private val themeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, R.string.theme_system, R.drawable.ic_monitor),
    ThemeOption(ThemeMode.LIGHT, R.string.theme_light, R.drawable.ic_sun),
    ThemeOption(ThemeMode.DARK, R.string.theme_dark, R.drawable.ic_moon),
)

/**
 * Progress tab: what's done (US-009) by track and as a list, export and import of the website's file (US-010),
 * theme (US-021) and licences (A-005).
 */
@Composable
fun ProgressScreen(container: AppContainer, onLicences: () -> Unit, onLesson: (String) -> Unit = {}) {
    val theme by container.settings.theme.collectAsStateWithLifecycle(initialValue = null)
    val loaded = rememberLoaded(Unit) { container.content.manifest }
    val manifest = loaded?.getOrNull()
    val progress = rememberProgress(container)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val snackbar = remember { SnackbarHostState() }
    val res = LocalResources.current
    val written = manifest?.lessons.orEmpty().toSet()

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = runCatching {
                val text = container.progress.exportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(text.toByteArray()) }
                }
            }.isSuccess
            snackbar.showSnackbar(res.getString(if (ok) R.string.export_ok else R.string.export_failed))
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)!!.use { input ->
                        // Read one byte past the limit so a big file is rejected without loading all of it.
                        val buf = input.readNBytesCompat(MAX_IMPORT_BYTES + 1)
                        String(buf, Charsets.UTF_8)
                    }
                }.getOrNull()
            }
            val message = if (text == null) {
                res.getString(R.string.import_unreadable)
            } else {
                when (val r = container.progress.importJson(text, written)) {
                    is ImportResult.Ok -> buildString {
                        append(if (r.added > 0) res.getQuantityString(R.plurals.import_added, r.added, r.added) else res.getString(R.string.import_nothing_new))
                        if (r.ignored > 0) append(" ").append(res.getQuantityString(R.plurals.import_skipped, r.ignored, r.ignored))
                    }
                    ImportResult.Failed.NEWER -> res.getString(R.string.import_newer)
                    ImportResult.Failed.TOO_BIG -> res.getString(R.string.import_too_big)
                    ImportResult.Failed.INVALID -> res.getString(R.string.import_invalid)
                }
            }
            snackbar.showSnackbar(message, duration = SnackbarDuration.Long)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ScreenTitle(stringResource(R.string.progress_title))
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (manifest != null) {
                    val doneIds = progress.done.keys.filter { it in written }
                    if (doneIds.isEmpty()) {
                        SpCard {
                            Text(stringResource(R.string.progress_empty_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                            Text(stringResource(R.string.progress_empty_body), style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Summary(doneIds.size, manifest)
                        ByTrack(progress, manifest)
                        DoneList(progress, manifest, onLesson)
                    }
                }
                SpCard {
                    Text(stringResource(R.string.progress_safe_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                    Text(stringResource(R.string.progress_safe_body), style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { exporter.launch("seniorpath-progress-${LocalDate.now()}.json") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(painterResource(R.drawable.ic_download), null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.export_button), modifier = Modifier.padding(start = 8.dp))
                    }
                    OutlinedButton(
                        // Some file managers label .json files as plain text or octet-stream, so accept those too.
                        onClick = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = manifest != null,
                    ) {
                        Icon(painterResource(R.drawable.ic_upload), null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.import_button), modifier = Modifier.padding(start = 8.dp))
                    }
                    Text(stringResource(R.string.import_merges), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }

            Text(
                stringResource(R.string.theme_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp).semantics { heading() },
            )
            Column(Modifier.selectableGroup()) {
                for (o in themeOptions) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .selectable(selected = theme == o.mode, role = Role.RadioButton) { scope.launch { container.settings.setTheme(o.mode) } }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(o.icon), contentDescription = null, tint = scheme.onSurfaceVariant)
                        Text(stringResource(o.label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 16.dp))
                        RadioButton(selected = theme == o.mode, onClick = null)
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = scheme.outlineVariant)
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onLicences).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = scheme.onSurfaceVariant)
                Text(stringResource(R.string.about_row), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 16.dp))
                Icon(painterResource(R.drawable.ic_chev_right), contentDescription = null, tint = scheme.onSurfaceVariant)
            }
            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME, manifest?.contentHash.orEmpty()),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Summary(done: Int, manifest: Manifest) {
    SpCard {
        Text(
            pluralStringResource(R.plurals.progress_summary, manifest.lessonCount, done, manifest.lessonCount),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        LinearProgressIndicator(progress = { done / manifest.lessonCount.coerceAtLeast(1).toFloat() }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ByTrack(progress: ProgressState, manifest: Manifest) {
    SpCard {
        Text(stringResource(R.string.progress_by_track), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        for (track in manifest.tracks) {
            val ids = track.modules.flatMap { it.topics }.filter { manifest.topics[it]?.written == true }
            if (ids.isEmpty()) continue
            val n = ids.count { it in progress.done }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(track.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.progress_n_of, n, ids.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(progress = { n / ids.size.toFloat() }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DoneList(progress: ProgressState, manifest: Manifest, onLesson: (String) -> Unit) {
    val fmt = remember { DateTimeFormatter.ofPattern("d MMM", Locale.UK).withZone(ZoneId.systemDefault()) }
    SpCard {
        Text(stringResource(R.string.progress_done_heading), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        // Newest first, as on the site.
        for ((id, at) in progress.done.entries.sortedByDescending { it.value }) {
            val t = manifest.topics[id]?.takeIf { it.written } ?: continue
            val track = manifest.tracks.firstOrNull { it.id == t.track }
            val date = parseTime(at)?.let { fmt.format(it) }.orEmpty()
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { onLesson(id) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_check_circle), null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(t.title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        listOfNotNull(track?.short, levelLabel(t.level), stringResource(R.string.progress_done_on, date)).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** InputStream.readNBytes is API 33+; this reads up to [max] bytes on every supported version. */
private fun java.io.InputStream.readNBytesCompat(max: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buf = ByteArray(8192)
    while (out.size() < max) {
        val n = read(buf, 0, minOf(buf.size, max - out.size()))
        if (n < 0) break
        out.write(buf, 0, n)
    }
    return out.toByteArray()
}
