package io.github.ukpratik.seniorpath.feature.map

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.data.TopicStatus
import io.github.ukpratik.seniorpath.ui.components.levelLabel
import io.github.ukpratik.seniorpath.ui.theme.LocalSpColors

private data class StatusLook(@param:StringRes val label: Int, @param:DrawableRes val icon: Int)

private fun look(s: TopicStatus) = when (s) {
    TopicStatus.DONE -> StatusLook(R.string.status_done, R.drawable.ic_check_circle)
    TopicStatus.HERE -> StatusLook(R.string.status_here, R.drawable.ic_target)
    TopicStatus.WRITTEN -> StatusLook(R.string.status_written, R.drawable.ic_book)
    TopicStatus.SOON -> StatusLook(R.string.status_soon, R.drawable.ic_dashed)
}

@Composable
private fun statusColor(s: TopicStatus): Color = when (s) {
    TopicStatus.DONE -> MaterialTheme.colorScheme.tertiary
    TopicStatus.HERE -> MaterialTheme.colorScheme.primary
    TopicStatus.WRITTEN -> MaterialTheme.colorScheme.onSurface
    TopicStatus.SOON -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** "Done ✓", "In progress", "Written" or "Coming soon", with the site's icon. */
@Composable
fun StatusLabel(status: TopicStatus, modifier: Modifier = Modifier) {
    val l = look(status)
    val c = statusColor(status)
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(l.icon), contentDescription = null, tint = c, modifier = Modifier.size(14.dp))
        Text(stringResource(l.label), style = MaterialTheme.typography.labelMedium, color = c)
    }
}

/**
 * One stop on a line: a dot in the track colour (or a step number in paths), the title, the level and the status.
 * Written topics open the lesson; coming-soon topics open [ComingSoonSheet].
 */
@Composable
fun TopicRow(
    title: String,
    level: String,
    status: TopicStatus,
    trackIndex: Int,
    onClick: () -> Unit,
    number: Int? = null,
    meta: String? = null,
) {
    val line = LocalSpColors.current.track(trackIndex)
    val soon = status == TopicStatus.SOON
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val dot = Modifier.size(28.dp).let {
            when {
                soon -> it.border(2.dp, line.copy(alpha = 0.45f), CircleShape)
                status == TopicStatus.DONE -> it.background(line, CircleShape)
                else -> it.background(MaterialTheme.colorScheme.surface, CircleShape).border(3.dp, line, CircleShape)
            }
        }
        Box(dot, contentAlignment = Alignment.Center) {
            when {
                number != null -> Text(
                    "$number",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = if (status == TopicStatus.DONE) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                )
                status == TopicStatus.DONE -> Icon(painterResource(R.drawable.ic_check), null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                status == TopicStatus.HERE -> Icon(painterResource(R.drawable.ic_target), null, tint = line, modifier = Modifier.size(16.dp))
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (soon) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(meta ?: levelLabel(level), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatusLabel(status)
            }
        }
    }
}

/**
 * A topic we haven't written yet (US-004): what it will cover, where it sits, and what to read first.
 * Prerequisites open their lesson, or this sheet again for another coming-soon topic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonSheet(
    id: String,
    manifest: Manifest,
    statusOf: (String) -> TopicStatus,
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val topic = manifest.topics[id] ?: return
    val track = manifest.tracks.firstOrNull { it.id == topic.track }
    val module = track?.modules?.firstOrNull { it.id == topic.module }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusLabel(TopicStatus.SOON)
            Text(topic.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            Text(topic.oneLiner, style = MaterialTheme.typography.bodyLarge)
            Text(
                listOfNotNull(levelLabel(topic.level), track?.title, module?.title).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(painterResource(R.drawable.ic_info), null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.soon_notice), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            val prereqs = topic.prereqs.mapNotNull { p -> manifest.topics[p]?.let { p to it } }
            if (prereqs.isNotEmpty()) {
                Text(stringResource(R.string.soon_prereqs), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                for ((pid, p) in prereqs) {
                    TopicRow(
                        title = p.title,
                        level = p.level,
                        status = statusOf(pid),
                        trackIndex = manifest.tracks.firstOrNull { it.id == p.track }?.index ?: 1,
                        onClick = { onOpen(pid) },
                    )
                }
            }
        }
    }
}
