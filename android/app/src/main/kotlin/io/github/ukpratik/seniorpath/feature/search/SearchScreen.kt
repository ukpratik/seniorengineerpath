package io.github.ukpratik.seniorpath.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.ui.components.ScreenTitle

/** Placeholder until A6 (US-012). */
@Composable
fun SearchScreen() {
    Column(Modifier.fillMaxSize()) {
        ScreenTitle(stringResource(R.string.search_title))
        Row(Modifier.padding(horizontal = 16.dp)) {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.search_soon), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
        }
    }
}
