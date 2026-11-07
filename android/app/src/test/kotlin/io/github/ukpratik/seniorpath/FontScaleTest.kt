package io.github.ukpratik.seniorpath

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import io.github.ukpratik.seniorpath.feature.lesson.Blocks
import io.github.ukpratik.seniorpath.feature.sims.SIM_IDS
import io.github.ukpratik.seniorpath.feature.sims.SimView
import io.github.ukpratik.seniorpath.ui.theme.SeniorPathTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A-003: at font scale 200 % on a 360 dp wide phone, no text in any lesson or animation runs off the right edge.
 * Text inside a sideways-scrolling row (code, tag chips) is allowed to be wider than the screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h740dp", fontScale = 2.0f)
class FontScaleTest {
    @get:Rule val rule = createComposeRule()

    private val content get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container.content

    private fun overflow(): List<String> {
        val root = rule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val width = root.boundsInRoot.right
        val bad = mutableListOf<String>()
        fun walk(n: SemanticsNode, inScroller: Boolean) {
            val scroller = inScroller || n.config.getOrNull(SemanticsProperties.HorizontalScrollAxisRange) != null
            val text = n.config.getOrNull(SemanticsProperties.Text)?.joinToString()
            if (!scroller && text != null && n.boundsInRoot.right > width + 1f) bad += "${text.take(50)} (right ${n.boundsInRoot.right} > $width)"
            n.children.forEach { walk(it, scroller) }
        }
        walk(root, false)
        return bad
    }

    @Test fun `lessons fit the screen at 200 percent`() {
        val repo = content
        var id by mutableStateOf(repo.manifest.lessons.first())
        rule.setContent {
            SeniorPathTheme(dark = false) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    for (s in repo.lesson(id).sections) Blocks(s.blocks, { repo.diagram(it) })
                }
            }
        }
        for (lesson in repo.manifest.lessons) {
            id = lesson
            rule.waitForIdle()
            assertWithMessage("lesson $lesson").that(overflow()).isEmpty()
        }
    }

    @Test fun `animations fit the screen at 200 percent`() {
        var id by mutableStateOf(SIM_IDS.first())
        rule.setContent {
            SeniorPathTheme(dark = false) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) { SimView(id) }
            }
        }
        for (sim in SIM_IDS) {
            id = sim
            rule.waitForIdle()
            assertWithMessage("animation $sim").that(overflow()).isEmpty()
        }
    }
}
