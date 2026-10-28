package io.github.ukpratik.seniorpath.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle
import io.github.ukpratik.seniorpath.ui.components.SpCard
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded

/** "Start here" (US-001). A1 shows what's in the build; the path steps and Continue card come in A2 and A5. */
@Composable
fun HomeScreen(container: AppContainer) {
    val loaded = rememberLoaded(Unit) { container.content.manifest to container.content.defaultPath() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle(stringResource(R.string.home_title))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.home_intro), style = MaterialTheme.typography.bodyLarge)
            val (manifest, path) = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
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
                SpCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_route), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            stringResource(R.string.home_path_label),
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
                }
            }
            Text(
                stringResource(R.string.home_offline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}
