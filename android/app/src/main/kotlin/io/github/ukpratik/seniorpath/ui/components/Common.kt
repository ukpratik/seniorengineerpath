package io.github.ukpratik.seniorpath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads [load] off the main thread (content reads do I/O the first time). Null while loading. */
@Composable
fun <T> rememberLoaded(key: Any?, load: () -> T): Result<T>? =
    produceState<Result<T>?>(null, key) { value = withContext(Dispatchers.IO) { runCatching(load) } }.value

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp).semantics { heading() },
    )
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
