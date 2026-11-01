package io.github.ukpratik.seniorpath.data

import com.google.common.truth.Truth.assertThat
import io.github.ukpratik.seniorpath.content.LearningPath
import io.github.ukpratik.seniorpath.content.Manifest
import io.github.ukpratik.seniorpath.content.Topic
import org.junit.Test

class ProgressTest {
    private fun topic(written: Boolean) = Topic("s", "T", "one", "L1", "t", "m", written = written)

    private val manifest = Manifest(
        schema = 1, contentHash = "x", lessonCount = 3, lessons = listOf("a", "c", "d"), tracks = emptyList(), anims = emptyList(),
        topics = mapOf("a" to topic(true), "b" to topic(false), "c" to topic(true), "d" to topic(true)),
        paths = listOf(
            LearningPath("main", "main", "Main", "", default = true, steps = listOf("a", "b", "c")),
            LearningPath("other", "other", "Other", "", steps = listOf("d", "a")),
        ),
    )

    @Test
    fun `reads the website's localStorage value, ignoring fields it doesn't know`() {
        val s = ProgressState.parse(
            """{"v":1,"done":{"a":"2026-10-01T10:00:00.000Z"},"ever":["a"],"lastOpened":"c","activePath":"other","mapAll":true,"future":1}""",
        )
        assertThat(s.done.keys).containsExactly("a")
        assertThat(s.lastOpened).isEqualTo("c")
        assertThat(s.activePath).isEqualTo("other")
        assertThat(s.mapAll).isTrue()
    }

    @Test
    fun `broken, empty or other-version values start empty, as on the web`() {
        for (raw in listOf(null, "", "not json", """{"v":2,"done":{"a":"x"}}""")) assertThat(ProgressState.parse(raw)).isEqualTo(ProgressState())
    }

    @Test
    fun `encode leaves unset fields out and round-trips`() {
        val s = ProgressState(activePath = "other")
        assertThat(s.encode()).isEqualTo("""{"v":1,"done":{},"ever":[],"activePath":"other"}""")
        assertThat(ProgressState.parse(s.encode())).isEqualTo(s)
    }

    @Test
    fun `status is soon, done, in progress or written`() {
        val s = ProgressState(done = mapOf("a" to "t"), lastOpened = "c")
        assertThat(s.statusOf("b", manifest)).isEqualTo(TopicStatus.SOON)
        assertThat(s.statusOf("a", manifest)).isEqualTo(TopicStatus.DONE)
        assertThat(s.statusOf("c", manifest)).isEqualTo(TopicStatus.HERE)
        assertThat(s.statusOf("d", manifest)).isEqualTo(TopicStatus.WRITTEN)
    }

    @Test
    fun `active path falls back to the default when unset or removed`() {
        assertThat(ProgressState().activePath(manifest)?.id).isEqualTo("main")
        assertThat(ProgressState(activePath = "gone").activePath(manifest)?.id).isEqualTo("main")
        assertThat(ProgressState(activePath = "other").activePath(manifest)?.id).isEqualTo("other")
    }

    @Test
    fun `next step skips coming-soon and done lessons`() {
        val path = manifest.paths[0]
        assertThat(ProgressState().nextStep(path, manifest)).isEqualTo(IndexedValue(0, "a"))
        assertThat(ProgressState(done = mapOf("a" to "t")).nextStep(path, manifest)).isEqualTo(IndexedValue(2, "c"))
        assertThat(ProgressState(done = mapOf("a" to "t", "c" to "t")).nextStep(path, manifest)).isNull()
    }

    @Test
    fun `continue prefers the lesson in progress, then the path's next lesson`() {
        assertThat(ProgressState(lastOpened = "d").continueTarget(manifest)).isEqualTo("d")
        assertThat(ProgressState(lastOpened = "d", done = mapOf("d" to "t")).continueTarget(manifest)).isEqualTo("a")
        assertThat(ProgressState(lastOpened = "b").continueTarget(manifest)).isEqualTo("a")
    }
}
