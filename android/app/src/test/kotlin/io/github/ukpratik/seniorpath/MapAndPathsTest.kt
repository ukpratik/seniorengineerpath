package io.github.ukpratik.seniorpath

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Map, coming-soon sheet and learning paths (A2) on the real bundled content. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MapAndPathsTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container
    private val manifest get() = container.content.manifest

    private fun waitFor(text: String) =
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }

    private fun scrollTo(text: String) = rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text))

    @Test
    fun `a written topic on the map opens its lesson`() {
        waitFor("Map")
        rule.onNodeWithText("Map").performClick()
        val track = manifest.tracks.first { t -> t.modules.any { m -> m.topics.any { manifest.topics[it]!!.written } } }
        val id = track.modules.flatMap { it.topics }.first { manifest.topics[it]!!.written }
        val title = manifest.topics.getValue(id).title
        waitFor(track.title)
        scrollTo(title)
        rule.onNodeWithText(title).performClick()
        waitFor(container.content.lesson(id).subtitle)
    }

    @Test
    fun `turning off written-only shows coming-soon topics, which open the sheet`() {
        waitFor("Map")
        rule.onNodeWithText("Map").performClick()
        waitFor("Written only")
        rule.onNodeWithText("Written only").performClick()
        rule.waitUntil(10_000) { runBlocking { container.progress.state.first().mapAll == true } }
        // A coming-soon topic in a track that starts open (it has written lessons).
        val track = manifest.tracks.first { t -> t.modules.any { m -> m.topics.any { manifest.topics[it]!!.written } } }
        val soon = manifest.topics.getValue(track.modules.flatMap { it.topics }.first { !manifest.topics[it]!!.written })
        scrollTo(soon.title)
        rule.onNodeWithText(soon.title).performClick()
        waitFor("We have not written this lesson yet")
        rule.onNodeWithText(soon.oneLiner).assertExists()
    }

    @Test
    fun `home links to the paths, and a path can become the reader's path`() {
        val other = manifest.paths.first { !it.default }
        waitFor("All learning paths")
        scrollTo("All learning paths")
        rule.onNodeWithText("All learning paths").performClick()
        waitFor(other.title)
        scrollTo(other.title)
        rule.onNodeWithText(other.title).performClick()
        waitFor("Make this my path")
        scrollTo("Make this my path")
        rule.onNodeWithText("Make this my path").performClick()
        waitFor("This is your path ✓")
        assertThat(runBlocking { container.progress.state.first() }.activePath).isEqualTo(other.id)
        // Home now offers the chosen path.
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitFor("Your path")
        scrollTo(other.title)
        rule.onNodeWithText(other.title).assertExists()
        runBlocking { container.progress.setActivePath(manifest.paths.first { it.default }.id) }
    }
}
