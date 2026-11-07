package io.github.ukpratik.seniorpath.feature.diagram

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Diagram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

/**
 * A step-through diagram (US-006, US-015): Back / Next or a swipe changes the step, the caption says what happens,
 * tapping the picture opens it full screen with pinch to zoom, and Share sends the current step as a PNG.
 */
@Composable
fun DiagramView(d: Diagram, renderer: DiagramRenderer, modifier: Modifier = Modifier) {
    var k by rememberSaveable(d.id) { mutableStateOf(1) }
    var zoom by rememberSaveable(d.id) { mutableStateOf(false) }
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val step = { to: Int -> k = to.coerceIn(1, d.frames) }

    Column(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(d.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            ShareButton(d, k, dark, renderer)
        }
        // The step's caption names the picture for TalkBack, also while it is still being drawn.
        val described = stringResource(R.string.diagram_step_of, k, d.frames) + ": " + d.captions[k - 1]
        Frame(
            d, k, dark, renderer,
            Modifier.semantics { contentDescription = described }
                .clickable(onClickLabel = stringResource(R.string.diagram_zoom)) { zoom = true }
                .pointerInput(d.id, k) { swipes { dir -> step(k + dir) } },
        )
        Steps(d, k, step)
    }

    if (zoom) {
        Dialog(onDismissRequest = { zoom = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            ZoomView(d, k, dark, renderer, step, onClose = { zoom = false })
        }
    }
}

/** The picture for step [k]; keeps the previous step on screen while the next one draws, then cross-fades. */
@Composable
private fun Frame(d: Diagram, k: Int, dark: Boolean, renderer: DiagramRenderer, modifier: Modifier = Modifier, scale: Float = 1f) {
    val (_, _, vw, vh) = remember(d.viewBox) { d.viewBox.trim().split(Regex("[ ,]+")).map { it.toFloat() } }
    BoxWithConstraints(modifier.fillMaxWidth().aspectRatio(vw / vh)) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx().toInt() }
        val caption = stringResource(R.string.diagram_step_of, k, d.frames) + ": " + d.captions[k - 1]
        val bitmap by produceState<Pair<Int, Bitmap?>?>(null, d.id, k, dark, widthPx, scale) {
            value = k to withContext(Dispatchers.Default) { renderer.renderOrNull(d, k, dark, widthPx) }
            // A sharper copy for zooming replaces it once drawn.
            if (scale > 1f) value = k to withContext(Dispatchers.Default) { renderer.renderOrNull(d, k, dark, (widthPx * scale).toInt()) }
        }
        val b = bitmap
        val image = b?.second
        when {
            // Inside the zoom view this names the picture; in the lesson the clickable frame already does.
            image != null -> Image(image.asImageBitmap(), contentDescription = if (scale > 1f) caption else null, modifier = Modifier.fillMaxSize())
            b != null -> Text(stringResource(R.string.diagram_failed), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
            else -> CircularProgressIndicator(Modifier.align(Alignment.Center).size(28.dp), strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun Steps(d: Diagram, k: Int, step: (Int) -> Unit, onDark: Boolean = false) {
    val textColor = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface
    Text(
        stringResource(R.string.diagram_step_of, k, d.frames) + ". " + d.captions[k - 1],
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { step(k - 1) }, enabled = k > 1, modifier = Modifier.weight(1f)) {
            Icon(painterResource(R.drawable.ic_arrow_left), null, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.diagram_back), modifier = Modifier.padding(start = 6.dp))
        }
        Text("$k / ${d.frames}", style = MaterialTheme.typography.labelLarge, color = textColor)
        Button(onClick = { step(k + 1) }, enabled = k < d.frames, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.diagram_next), modifier = Modifier.padding(end = 6.dp))
            Icon(painterResource(R.drawable.ic_arrow_right), null, modifier = Modifier.size(18.dp))
        }
    }
}

/** Full screen, pinch to zoom and drag; steps still change with the buttons. */
@Composable
private fun ZoomView(d: Diagram, k: Int, dark: Boolean, renderer: DiagramRenderer, step: (Int) -> Unit, onClose: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transform = rememberTransformableState { zoomBy, panBy, _ ->
        scale = (scale * zoomBy).coerceIn(1f, 5f)
        offset = if (scale == 1f) Offset.Zero else offset + panBy
    }
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().navigationBarsPadding().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(d.title, style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_x), contentDescription = stringResource(R.string.diagram_close), tint = Color.White)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().transformable(transform), contentAlignment = Alignment.Center) {
            // Drawn at twice the screen width so zoomed text stays sharp.
            Frame(
                d, k, dark, renderer, scale = 2f,
                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Steps(d, k, step, onDark = true) }
    }
}

@Composable
private fun ShareButton(d: Diagram, k: Int, dark: Boolean, renderer: DiagramRenderer) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chooser = stringResource(R.string.diagram_share)
    IconButton(onClick = { scope.launch { shareFrame(context, d, k, dark, renderer, chooser) } }) {
        Icon(painterResource(R.drawable.ic_upload), contentDescription = chooser)
    }
}

private suspend fun shareFrame(context: Context, d: Diagram, k: Int, dark: Boolean, renderer: DiagramRenderer, chooser: String) {
    val file = withContext(Dispatchers.IO) {
        val png = renderer.renderOrNull(d, k, dark, 1600) ?: return@withContext null
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() } // keep only the latest share
        File(dir, "${d.id}-step-$k.png").also { f -> f.outputStream().use { png.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    } ?: return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(null, uri) // lets the share sheet show the picture
        putExtra(Intent.EXTRA_TEXT, "${d.title} (${k}/${d.frames}): ${d.captions[k - 1]}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, chooser))
}

/** Calls [onSwipe] with +1 (swipe left: next) or -1 (swipe right: back) after a clear horizontal drag. */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.swipes(onSwipe: (Int) -> Unit) {
    var dx = 0f
    val threshold = 48.dp.toPx()
    detectHorizontalDragGestures(
        onDragStart = { dx = 0f },
        onDragEnd = { if (abs(dx) > threshold) onSwipe(if (dx < 0) 1 else -1) },
        onHorizontalDrag = { change, amount ->
            change.consume()
            dx += amount
        },
    )
}
