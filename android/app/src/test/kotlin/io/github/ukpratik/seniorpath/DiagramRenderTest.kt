package io.github.ukpratik.seniorpath

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.ukpratik.seniorpath.feature.diagram.DiagramRenderer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Every step of every bundled diagram draws with AndroidSVG in both themes (A4), with real pixels:
 * a step shows more than the empty background, and the last step differs from the first (later steps start hidden).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DiagramRenderTest {
    private val container get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container

    @Test
    fun `every step of every diagram draws in both themes`() {
        val repo = container.content
        val renderer = DiagramRenderer(ApplicationProvider.getApplicationContext(), repo.diagramStyle)
        val ids = repo.manifest.lessons.flatMap { id ->
            repo.lesson(id).sections.flatMap { s -> s.blocks.filterIsInstance<io.github.ukpratik.seniorpath.content.Block.Diagram>() }.map { it.id }
        }
        assertThat(ids).isNotEmpty()
        for (id in ids) {
            val d = repo.diagram(id)
            for (dark in listOf(false, true)) {
                val frames = (1..d.frames).map { k -> renderer.render(d, k, dark, 360) }
                for ((i, b) in frames.withIndex()) {
                    val bg = b.getPixel(0, 0)
                    val px = IntArray(b.width * b.height).also { b.getPixels(it, 0, b.width, 0, 0, b.width, b.height) }
                    assertWithId(px.any { it != bg }, "$id step ${i + 1} (dark=$dark) is blank")
                }
                if (d.frames > 1) assertWithId(!frames.first().sameAs(frames.last()), "$id (dark=$dark): first and last steps look the same")
            }
        }
    }

    private fun assertWithId(ok: Boolean, message: String) = check(ok) { message }
}
