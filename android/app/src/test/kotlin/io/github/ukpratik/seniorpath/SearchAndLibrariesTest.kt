package io.github.ukpratik.seniorpath

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A6: search across everything, the incident and case libraries with tag filters, and the glossary. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchAndLibrariesTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val content get() = ApplicationProvider.getApplicationContext<SeniorPathApp>().container.content

    private fun waitFor(text: String, substring: Boolean = true) =
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty() }

    private fun scrollTo(text: String) = rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text, substring = true))

    private fun openSearch() {
        waitFor("Search")
        rule.onAllNodes(hasText("Search")).onFirst().performClick()
        rule.waitUntil(10_000) { rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `a search finds the lesson and opens it`() {
        openSearch()
        rule.onNode(hasSetTextAction()).performTextInput("idempotency")
        waitFor("Lessons")
        val id = "idempotency-retries"
        val title = content.manifest.topics.getValue(id).title
        waitFor(title)
        rule.onAllNodes(hasText(title)).onFirst().performClick()
        waitFor(content.lesson(id).subtitle)
    }

    @Test
    fun `a search with no match says so`() {
        openSearch()
        rule.onNode(hasSetTextAction()).performTextInput("zzqqxxyy")
        waitFor("No results for")
    }

    @Test
    fun `a glossary result opens its meaning in a sheet`() {
        openSearch()
        val g = content.glossary.first { it.term.length > 6 && it.term.all { c -> c.isLetter() } }
        rule.onNode(hasSetTextAction()).performTextInput(g.term)
        waitFor("results")
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Glossary terms"))
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(g.term))
        rule.onAllNodes(hasText(g.term)).onFirst().performClick()
        waitFor(g.short)
    }

    @Test
    fun `the incident library filters by tag`() {
        openSearch()
        waitFor("Incident library")
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Incident library"))
        rule.onNodeWithText("Incident library").performClick()
        val all = content.incidents
        waitFor("${all.size} incidents")
        val tag = "dns"
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(tag))
        rule.onNodeWithText(tag).performClick()
        waitFor("${all.count { tag in it.tags }} incidents")
        val first = all.first { tag in it.tags }
        scrollTo(first.title)
        rule.onNodeWithText(first.title).assertExists()
    }

    @Test
    fun `case studies and the glossary open from search`() {
        openSearch()
        waitFor("Case studies")
        rule.onNodeWithText("Case studies").performClick()
        waitFor("${content.cases.size} case studies")
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitFor("Glossary")
        rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("Glossary"))
        rule.onNodeWithText("Glossary").performClick()
        waitFor("Every term the lessons use")
        val first = content.glossary.minBy { it.term.lowercase() }
        waitFor(first.term)
    }
}
