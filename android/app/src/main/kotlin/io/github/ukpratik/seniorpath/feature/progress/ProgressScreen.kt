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
import kotlinx.coroutines.launch

private data class ThemeOption(val mode: ThemeMode, val label: Int, @param:DrawableRes val icon: Int)

private val themeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, R.string.theme_system, R.drawable.ic_monitor),
    ThemeOption(ThemeMode.LIGHT, R.string.theme_light, R.drawable.ic_sun),
    ThemeOption(ThemeMode.DARK, R.string.theme_dark, R.drawable.ic_moon),
)

/** Progress tab. A1: theme (US-021) and licences (A-005); done lessons and export/import arrive in A5. */
@Composable
fun ProgressScreen(container: AppContainer, onLicences: () -> Unit) {
    val theme by container.settings.theme.collectAsStateWithLifecycle(initialValue = null)
    val hash = rememberLoaded(Unit) { container.content.manifest.contentHash }?.getOrNull().orEmpty()
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle(stringResource(R.string.progress_title))
        Text(stringResource(R.string.progress_soon), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 16.dp))

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
            stringResource(R.string.version, BuildConfig.VERSION_NAME, hash),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}
