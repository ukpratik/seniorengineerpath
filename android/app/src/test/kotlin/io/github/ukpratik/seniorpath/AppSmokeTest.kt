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

    @Test
    fun `home shows the lesson count from the bundled content`() {
        val m = content.manifest
        rule.waitUntil(5_000) { rule.onAllNodes(hasText("${m.lessonCount} lessons ready", substring = true)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Start here").assertExists()
    }

    @Test
    fun `map tab lists every track`() {
        rule.onNodeWithText("Map").performClick()
        rule.onNodeWithText("Map").assertIsSelected()
        val tracks = content.manifest.tracks
        rule.waitUntil(5_000) { rule.onAllNodes(hasText(tracks.first().title)).fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(tracks.last().title))
        rule.onNodeWithText(tracks.last().title).assertExists()
    }

    @Test
    fun `progress tab opens licences, and back returns`() {
        rule.onNodeWithText("Progress").performClick()
        rule.onNodeWithText("Licences and credits").performClick()
        rule.onNodeWithText("Lessons and diagrams").assertExists()
        rule.onNodeWithText("Open-source libraries").assertExists()
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Follow the phone").assertExists()
    }

    @Test
    fun `licence report lists the bundled font and icons`() {
        val names = Licences.read(ApplicationProvider.getApplicationContext<SeniorPathApp>().resources).map { it.name }
        assertThat(names).containsAtLeast("Plus Jakarta Sans", "Lucide icons")
    }
}
