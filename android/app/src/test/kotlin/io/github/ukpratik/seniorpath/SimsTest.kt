package io.github.ukpratik.seniorpath

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.ukpratik.seniorpath.content.Block
import io.github.ukpratik.seniorpath.feature.sims.SIM_IDS
import io.github.ukpratik.seniorpath.feature.sims.SimView
import io.github.ukpratik.seniorpath.ui.theme.SeniorPathTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A7: each of the six animations draws, and its controls change the result sentence. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SimsTest {
    @get:Rule val rule = createComposeRule()

    private fun show(id: String) = rule.setContent {
        SeniorPathTheme(dark = false) { Column(Modifier.verticalScroll(rememberScrollState())) { SimView(id) } }
    }

    private fun setSlider(label: String, value: Float) =
        rule.onNode(hasContentDescription(label)).performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(value) }

    private fun exists(text: String) = assertThat(rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes()).isNotEmpty()

    @Test fun `every animation a lesson uses can be drawn`() {
        val content = ApplicationProvider.getApplicationContext<SeniorPathApp>().container.content
        val used = content.manifest.lessons.flatMap { id ->
            content.lesson(id).sections.flatMap { s -> s.blocks.filterIsInstance<Block.Anim>().map { it.id } }
        }.toSet()
        assertThat(SIM_IDS).containsAtLeastElementsIn(used)
        assertThat(SIM_IDS).containsExactlyElementsIn(content.manifest.anims)
    }

    @Test fun `cache stampede`() {
        show("cache-stampede")
        exists("Without coalescing: 240 database queries")
        rule.onNodeWithText("Request coalescing", substring = true).performClick()
        exists("With coalescing: 1 database query from this server. 239 requests wait")
    }

    @Test fun `replication lag`() {
        show("replication-lag")
        exists("Stale read")
        setSlider("You read again after:", 900f)
        exists("Fresh read from the follower")
        setSlider("You read again after:", 300f)
        rule.onNodeWithText("Read-your-writes", substring = true).performClick()
        exists("Fresh read from the leader")
    }

    @Test fun rollout() {
        show("rollout")
        exists("Peak: 100% of users get errors. Total harm: 1,000")
        rule.onNodeWithText("In stages", substring = true).performClick()
        exists("Peak: 1% of users get errors.")
    }

    @Test fun `token bucket`() {
        show("token-bucket")
        exists("rejected with “429 Too Many Requests”")
        setSlider("Burst size at 1 second:", 0f)
        exists("Steady traffic stays under the refill rate")
    }

    @Test fun `hash ring`() {
        show("hash-ring")
        exists("Adding server 5")
        setSlider("Servers now:", 9f)
        exists("Adding server 10")
        setSlider("Virtual nodes per server (ring only):", 3f)
        exists("With few virtual nodes")
    }

    @Test fun `raft election`() {
        show("raft-election")
        exists("S1 is the leader of term 1")
        rule.onNodeWithText("Stop the leader").performClick()
        exists("New leader:")
        rule.onNodeWithText("Stop one more server").performClick()
        exists("New leader:")
        rule.onNodeWithText("Stop one more server").performClick()
        exists("No leader. Only 2 of 5 servers are running")
        // As on the site, one server always keeps running.
        rule.onNodeWithText("Stop one more server").performClick()
        exists("Only 1 of 5 servers")
        rule.onNodeWithText("Stop one more server").assertIsNotEnabled()
    }
}
