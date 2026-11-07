package io.github.ukpratik.seniorpath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.data.ProgressState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads [load] off the main thread (content reads do I/O the first time). Null while loading. */
@Composable
fun <T> rememberLoaded(key: Any?, load: () -> T): Result<T>? =
    produceState<Result<T>?>(null, key) { value = withContext(Dispatchers.IO) { runCatching(load) } }.value

/** The reader's progress; empty until DataStore has been read once. */
@Composable
fun rememberProgress(container: AppContainer): ProgressState =
    container.progress.state.collectAsStateWithLifecycle(ProgressState()).value

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp).semantics { heading() },
    )
}

/** Top bar for screens stacked above the tabs: back arrow, an optional title and optional actions on the right. */
@Composable
fun BackBar(title: String?, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_arrow_left), contentDescription = stringResource(R.string.back))
        }
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 4.dp).semantics { heading() },
            )
        }
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/** The site's level names (LEVEL_LABEL in src/lib/site-data.ts). */
@Composable
fun levelLabel(level: String): String = when (level) {
    "L1" -> stringResource(R.string.level_l1)
    "L2" -> stringResource(R.string.level_l2)
    "L3" -> stringResource(R.string.level_l3)
    else -> level
}

@Composable
fun SpCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

/** Shown if the bundled content can't be read (for example a newer schema than this app knows). */
@Composable
fun LoadError(error: Throwable, modifier: Modifier = Modifier) {
    Text(
        "Couldn’t read the lessons in this build: ${error.message}",
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(16.dp),
    )
}
