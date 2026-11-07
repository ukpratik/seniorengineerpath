package io.github.ukpratik.seniorpath.content

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class SearchIndexTest {
    private fun e(type: String, id: String, title: String, text: String, body: String? = null) = SearchEntry(type, id, title, text, body)

    private val small = SearchIndex(
        listOf(
            e("lesson", "idempotency-retries", "Idempotency, retries and timeouts", "Make retries safe.", "A retry storm can take a service down. Use jittered backoff."),
            e("glossary", "idempotent", "idempotent", "Doing it twice has the same effect as once."),
            e("topic", "retries-deep", "Retry budgets", "How many retries a fleet can afford."),
            e("incident", "x", "Acme: the café outage", "A cache expired and the database fell over."),
        ),
    )

    @Test fun `every word must match the start of a word`() {
        assertThat(small.search("idem").map { it.entry.id }).containsExactly("idempotency-retries", "idempotent")
        assertThat(small.search("idem storm").map { it.entry.id }).containsExactly("idempotency-retries")
        assertThat(small.search("potent")).isEmpty()
    }

    @Test fun `exact title first, then title words, then text`() {
        assertThat(small.search("idempotent").first().entry.id).isEqualTo("idempotent")
        assertThat(small.search("retries").map { it.entry.id }).containsExactly("idempotency-retries", "retries-deep").inOrder()
    }

    @Test fun `one typo is forgiven in longer words`() {
        assertThat(small.search("idempotnt").map { it.entry.id }).contains("idempotent")
        assertThat(small.search("jitterd").map { it.entry.id }).containsExactly("idempotency-retries")
        assertThat(small.search("xyzzyq")).isEmpty()
    }

    @Test fun `plural and -ing forms find each other, and beat typo matches`() {
        val index = SearchIndex(
            listOf(
                e("lesson", "rag", "RAG: retrieval-augmented generation", "Ground answers in your documents."),
                e("lesson", "idem", "Idempotency, retries and timeouts", "Why one retry can charge twice."),
                e("lesson", "q", "Queues", "Messages waiting.", "The consumer is retrying."),
            ),
        )
        assertThat(index.search("retry").map { it.entry.id }).containsExactly("idem", "q", "rag").inOrder()
        val hit = index.search("retries").first { it.entry.id == "idem" }
        assertThat(hit.titleMarks.map { hit.entry.title.substring(it) }).containsExactly("retries")
    }

    @Test fun `accents and case don't matter`() {
        assertThat(small.search("CAFE").map { it.entry.id }).containsExactly("x")
    }

    @Test fun `a body-only match shows the words around it, marked`() {
        val hit = small.search("storm").single()
        assertThat(hit.snippet).contains("retry storm")
        assertThat(hit.marks.map { hit.snippet.substring(it) }).containsExactly("storm")
    }

    @Test fun `queries shorter than two letters find nothing`() {
        assertThat(small.search("i")).isEmpty()
        assertThat(small.search("  ")).isEmpty()
    }

    @Test fun `the real export answers the site's suggested searches`() {
        val dir = File(System.getProperty("contentDir") ?: "app/src/main/assets/content")
        assumeTrue(File(dir, "search.json").exists())
        val repo = ContentRepository({ File(dir, it).readText() })
        val t0 = System.nanoTime()
        val index = repo.searchIndex
        val ms = (System.nanoTime() - t0) / 1_000_000
        for (q in listOf("idempotency", "retry", "outage", "senior", "timeout", "DNS")) {
            assertThat(index.search(q)).isNotEmpty()
        }
        assertThat(index.search("idempotency").first().entry.type).isEqualTo("lesson")
        assertThat(index.search("DNS").map { it.entry.type }).contains("incident")
        println("search index built in $ms ms over ${repo.search.size} entries")
    }
}
