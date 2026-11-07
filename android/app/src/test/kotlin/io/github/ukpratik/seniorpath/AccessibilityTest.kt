package io.github.ukpratik.seniorpath

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What TalkBack needs from every screen (A8): each control a reader can tap or toggle has a name (text or a content
 * description) and is at least 32 dp on each side, Material's smallest control (a chip); Compose and
 * minimumInteractiveComponentSize make the touch area 48 × 48 dp around it. Checked over the semantics tree TalkBack reads.
 */
fun ComposeTestRule.accessibilityProblems(): List<String> {
    val root = onRoot().fetchSemanticsNode()
    val minPx = with(density) { 32.dp.toPx() } - 1f
    val out = mutableListOf<String>()
    fun name(n: SemanticsNode): String? =
        n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()?.takeIf { it.isNotBlank() }
            ?: n.config.getOrNull(SemanticsProperties.Text)?.joinToString()?.takeIf { it.isNotBlank() }
            ?: n.config.getOrNull(SemanticsProperties.EditableText)?.text
            ?: n.children.firstNotNullOfOrNull { c -> name(c) }
    fun walk(n: SemanticsNode) {
        val actionable = n.config.getOrNull(SemanticsActions.OnClick) != null && n.config.getOrNull(SemanticsProperties.Disabled) == null
        // Inline links inside a paragraph are not separate nodes; they ride on the paragraph's text.
        // Zero-size nodes are lazy-list items composed ahead of time, not on screen yet.
        val laidOut = n.size.width > 0 && n.size.height > 0
        if (laidOut && (actionable && n.config.getOrNull(SemanticsProperties.Text) == null || actionable && n.config.contains(SemanticsProperties.Role))) {
            val label = name(n)
            // The laid-out size, not clipped by the screen edge (Compose also widens small touch areas, but TalkBack's
            // focus box and Accessibility Scanner use this).
            val size = n.size
            if (label == null) out += "no name: node ${n.id} at ${n.boundsInRoot}"
            if (size.width < minPx || size.height < minPx) out += "small target (${size.width}×${size.height} px): ${label ?: n.id}"
        }
        n.children.forEach(::walk)
    }
    walk(root)
    return out
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AccessibilityTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container

    private fun waitFor(text: String) =
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }

    private fun check(where: String) = assertWithMessage(where).that(rule.accessibilityProblems()).isEmpty()

    private fun tab(label: String, ready: String) {
        rule.onAllNodes(hasText(label)).onFirst().performClick()
        waitFor(ready)
        check(label)
    }

    @Test fun `main screens have named controls with big enough targets`() {
        waitFor("All learning paths")
        check("Home")
        tab("Map", container.content.manifest.tracks.first().title)
        tab("Search", "Incident library")
        tab("Progress", "Keep your progress safe")
    }

    @Test fun `a lesson with an animation has named controls with big enough targets`() {
        val title = container.content.manifest.topics.getValue("raft").title
        waitFor("All learning paths")
        tab("Search", "Incident library")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("raft")
        waitFor(title)
        check("Search results")
        rule.onAllNodes(hasText(title)).onFirst().performClick()
        waitFor(container.content.lesson("raft").subtitle)
        check("Lesson top")
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Stop the leader"))
        check("Raft animation")
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Mark as done"))
        check("Lesson end")
    }
}

/** The check itself catches a tiny, unnamed button. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AccessibilityCheckTest {
    @get:Rule val rule = createComposeRule()

    @Test fun `flags a small unnamed control`() {
        rule.setContent { Box(Modifier.size(10.dp).clickable { }) }
        assertThat(rule.accessibilityProblems()).hasSize(2)
    }
}
