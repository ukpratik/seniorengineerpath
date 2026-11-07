package io.github.ukpratik.seniorpath

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onFirst
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.ukpratik.seniorpath.feature.about.Licences
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The app starts from the real bundled content and the four tabs and the licences screen work. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val content get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container.content

    /** The first frame waits for the saved theme, so wait for the bottom bar before tapping it. */
    private fun tab(label: String) {
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(label)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(label).performClick()
    }

    @Test
    fun `home shows the lesson count from the bundled content`() {
        val m = content.manifest
        rule.waitUntil(10_000) { rule.onAllNodes(hasText("${m.lessonCount} lessons ready", substring = true)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Start here").assertExists()
    }

    @Test
    fun `map tab lists every track`() {
        tab("Map")
        rule.onNodeWithText("Map").assertIsSelected()
        val tracks = content.manifest.tracks
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(tracks.first().title)).fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(tracks.last().title))
        rule.onNodeWithText(tracks.last().title).assertExists()
    }

    @Test
    fun `progress tab opens licences, and back returns`() {
        tab("Progress")
        rule.waitUntil(10_000) { rule.onAllNodes(hasText("Keep your progress safe")).fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Licences and credits"))
        rule.onNodeWithText("Licences and credits").performClick()
        rule.onNodeWithText("Lessons and diagrams").assertExists()
        rule.onNodeWithText("Open-source libraries").assertExists()
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Follow the phone"))
    }

    @Test
    fun `licence report lists the bundled font and icons`() {
        val names = Licences.read(ApplicationProvider.getApplicationContext<SeniorPathApp>().resources).map { it.name }
        assertThat(names).containsAtLeast("Plus Jakarta Sans", "Lucide icons")
    }
}
