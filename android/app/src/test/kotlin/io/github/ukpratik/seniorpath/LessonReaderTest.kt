package io.github.ukpratik.seniorpath

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import io.github.ukpratik.seniorpath.content.Block
import io.github.ukpratik.seniorpath.content.Inline
import io.github.ukpratik.seniorpath.feature.lesson.Blocks
import io.github.ukpratik.seniorpath.feature.lesson.LessonScreen
import io.github.ukpratik.seniorpath.ui.theme.SeniorPathTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The lesson reader (A3) on the real bundled lessons. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LessonReaderTest {
    @get:Rule val rule = createComposeRule()

    private val content get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container.content

    private fun waitFor(text: String) =
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }

    @Test
    fun `every block of every lesson draws`() {
        val repo = content
        var id by mutableStateOf(repo.manifest.lessons.first())
        rule.setContent {
            SeniorPathTheme(dark = false) {
                val lesson = repo.lesson(id)
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (s in lesson.sections) Blocks(s.blocks, { repo.diagram(it) })
                }
            }
        }
        for (lesson in repo.manifest.lessons) {
            id = lesson
            rule.waitForIdle()
        }
    }

    @Test
    fun `a lesson shows its sections, glossary terms, citations and quiz`() {
        val container = ApplicationProvider.getApplicationContext<SeniorPathApp>().container
        // A lesson with a term, a citation and a quiz.
        val lesson = content.manifest.lessons.map { content.lesson(it) }.first { l ->
            l.quiz.isNotEmpty() && l.sections[0].blocks.any { b -> b is Block.Paragraph && b.c.any { it is Inline.Cite } }
        }
        rule.setContent { SeniorPathTheme(dark = false) { LessonScreen(container, lesson.id, onLesson = {}, onBack = {}) } }
        waitFor(lesson.subtitle)
        rule.onNodeWithText("1 / 8").assertExists()

        // Contents jumps to a section.
        rule.onNodeWithContentDescription("Contents").performClick()
        rule.onNodeWithText("Check yourself").performClick()
        waitFor("Question 1")

        // A quiz answer gets feedback.
        val q = lesson.quiz[0]
        val wrong = q.options.indices.first { it != q.answer }
        val wrongText = (q.options[wrong].first() as Inline.Text).v
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(wrongText))
        rule.onNodeWithText(wrongText).performClick()
        waitFor("Not quite.")

        // Sources are listed under Go deeper.
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(lesson.sources[0].title))
    }
}
