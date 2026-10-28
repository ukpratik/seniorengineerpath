package io.github.ukpratik.seniorpath.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded
import io.github.ukpratik.seniorpath.ui.theme.LocalSpColors

/** Curriculum map (US-003): A1 lists the tracks with how much is written; modules and topics open in A2. */
@Composable
fun MapScreen(container: AppContainer) {
    val loaded = rememberLoaded(Unit) { container.content.manifest }
    val colors = LocalSpColors.current
    Column(Modifier.fillMaxSize()) {
        ScreenTitle(stringResource(R.string.map_title))
        val manifest = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
        LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(manifest.tracks, key = { it.id }) { track ->
                val ids = track.modules.flatMap { it.topics }
                val written = ids.count { manifest.topics[it]?.written == true }
                SpCard {
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            Modifier.width(6.dp).fillMaxHeight()
                                .background(colors.track(track.index), RoundedCornerShape(3.dp)),
                        )
                        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(track.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.map_track_count, written, ids.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
