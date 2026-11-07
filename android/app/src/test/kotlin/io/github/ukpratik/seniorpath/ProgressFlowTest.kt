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
import io.github.ukpratik.seniorpath.data.ImportResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import io.github.ukpratik.seniorpath.data.ProgressState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** A5: mark as done, Continue, the Progress tab, import of a website file, and What's new. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProgressFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val container get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container
    private val manifest get() = container.content.manifest
    private val state get() = runBlocking { container.progress.state.first() }

    private fun waitFor(text: String) =
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }

    private fun scrollTo(text: String) = rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text, substring = true))

    @Before fun clean() = runBlocking {
        container.progress.replace(ProgressState())
        container.settings.markLessonsSeen(manifest.lessons)
        container.settings.clearReadingSpot()
    }

    private fun back() = rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    @Test
    fun `marking a lesson done shows on Home and in Progress`() {
        val path = manifest.paths.first { it.default }
        val (first, second) = path.steps.filter { manifest.topics[it]!!.written }.take(2)
        val lesson = container.content.lesson(first)
        rule.activityRule.scenario.onActivity { }
        waitFor("All learning paths")
        runBlocking { container.progress.setLastOpened(first) }
        waitFor("Continue lesson")
        rule.onNodeWithText("Continue lesson").performClick()
        waitFor(lesson.subtitle)
        scrollTo("Mark as done")
        rule.onNodeWithText("Mark as done").performClick()
        rule.waitUntil(10_000) { first in state.done }
        waitFor("Done ✓ (undo)")
        waitFor("Next on your path: ${manifest.topics.getValue(second).title}")

        back()
        // The last lesson is done, so Continue offers the next one on the path.
        waitFor("Start next lesson")
        rule.onNodeWithText(manifest.topics.getValue(second).title).assertExists()

        rule.onNodeWithText("Progress").performClick()
        waitFor("1 of ${manifest.lessonCount} lessons done")
        scrollTo(lesson.title)
        rule.onNodeWithText(lesson.title).assertExists()
    }

    @Test
    fun `undo marks the lesson not done`() {
        val id = manifest.lessons.first()
        runBlocking { container.progress.markDone(id) }
        waitFor("Progress")
        rule.onNodeWithText("Progress").performClick()
        waitFor(manifest.topics.getValue(id).title)
        scrollTo(manifest.topics.getValue(id).title)
        rule.onNodeWithText(manifest.topics.getValue(id).title).performClick()
        waitFor(container.content.lesson(id).subtitle)
        scrollTo("Done ✓ (undo)")
        rule.onNodeWithText("Done ✓ (undo)").performClick()
        rule.waitUntil(10_000) { id !in state.done }
    }

    @Test
    fun `a file exported from the website imports and shows as done`() {
        val text = File("../../tests/fixtures/progress/web-export.json").readText()
        val r = runBlocking { container.progress.importJson(text, manifest.lessons.toSet()) }
        assertThat(r).isEqualTo(ImportResult.Ok(added = 2, ignored = 1))
        waitFor("Progress")
        rule.onNodeWithText("Progress").performClick()
        waitFor("2 of ${manifest.lessonCount} lessons done")
        scrollTo(manifest.topics.getValue("authn-authz").title)
    }

    @Test
    fun `what's new lists lessons the reader hasn't been told about, until dismissed`() {
        waitFor("All learning paths")
        // A new install sees no card: everything counts as seen.
        assertThat(rule.onAllNodes(hasText("New since your last visit", substring = true)).fetchSemanticsNodes()).isEmpty()
        val newOnes = manifest.lessons.takeLast(2)
        runBlocking { container.settings.markLessonsSeen(manifest.lessons - newOnes.toSet()) }
        rule.activityRule.scenario.recreate()
        waitFor("New since your last visit: 2 lessons")
        rule.onNodeWithText(manifest.topics.getValue(newOnes[0]).title).assertExists()
        rule.onNodeWithText("Hide what’s new", useUnmergedTree = true).assertDoesNotExist()
        rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Hide what’s new")).onFirst().performClick()
        rule.waitUntil(10_000) { runBlocking { container.settings.unseenLessons(manifest.lessons) }.isEmpty() }
    }
}
