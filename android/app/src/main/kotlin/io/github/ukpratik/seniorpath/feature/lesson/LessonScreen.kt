package io.github.ukpratik.seniorpath.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.ui.components.BackBar
import io.github.ukpratik.seniorpath.ui.components.LoadError
import io.github.ukpratik.seniorpath.ui.components.levelLabel
import io.github.ukpratik.seniorpath.ui.components.rememberLoaded

/** Lesson screen. A2 shows the lesson's header so map and paths have somewhere to go; the reader (US-005) comes in A3. */
@Composable
fun LessonScreen(container: AppContainer, id: String, onBack: () -> Unit) {
    val loaded = rememberLoaded(id) { container.content.lesson(id) }
    Column(Modifier.fillMaxSize()) {
        BackBar(null, onBack)
        val lesson = loaded?.getOrElse { LoadError(it); return@Column } ?: return@Column
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(lesson.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            Text(lesson.subtitle, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.lesson_meta, levelLabel(lesson.level), lesson.readingMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(R.string.lesson_soon), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
