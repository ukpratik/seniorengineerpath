package io.github.ukpratik.seniorpath.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.time.Instant

/** The app half of the progress-file contract (tests/fixtures/progress/README.md); the web half is store.test.ts. */
class ProgressIoTest {
    private val fixtures = File("../../tests/fixtures/progress")
    private val known = setOf("api-gateway", "architecture-tradeoffs", "authn-authz", "btree-lsm")
    private fun at(s: String) = Instant.parse(s)

    @Test
    fun `imports the file the website exports`() {
        val (r, s) = ProgressState().importJson(File(fixtures, "web-export.json").readText(), known)
        assertThat(r).isEqualTo(ImportResult.Ok(added = 2, ignored = 1))
        assertThat(s.done).containsExactly("api-gateway", "2026-10-01T09:00:00.000Z", "authn-authz", "2026-10-03T18:30:00.000Z")
        assertThat(s.lastOpened).isEqualTo("btree-lsm")
        assertThat(s.ever).containsExactly("api-gateway", "authn-authz")
    }

    @Test
    fun `writes exactly the file the website imports`() {
        val s = ProgressState()
            .markDone("architecture-tradeoffs", at("2026-11-01T07:15:00Z"))
            .markDone("api-gateway", at("2026-11-02T10:00:00Z"))
            .opened("btree-lsm", at("2026-11-02T10:05:00Z"))
        assertThat(s.exportJson(at("2026-11-03T00:00:00Z"))).isEqualTo(File(fixtures, "app-export.json").readText())
    }

    @Test
    fun `round-trips through its own export`() {
        val a = ProgressState().markDone("api-gateway", at("2026-11-01T00:00:00Z")).opened("authn-authz", at("2026-11-01T01:00:00Z"))
        val (r, b) = ProgressState().importJson(a.exportJson(at("2026-11-02T00:00:00Z")))
        assertThat(r).isEqualTo(ImportResult.Ok(1, 0))
        assertThat(b.done).isEqualTo(a.done)
        assertThat(b.lastOpened).isEqualTo("authn-authz")
        assertThat(b.lastOpenedAt).isEqualTo(a.lastOpenedAt)
    }

    @Test
    fun `merging keeps every lesson, the earlier finish date and the newer last-opened`() {
        val here = ProgressState(
            done = mapOf("api-gateway" to "2026-10-05T00:00:00.000Z", "btree-lsm" to "2026-10-02T00:00:00.000Z"),
            lastOpened = "btree-lsm", lastOpenedAt = "2026-10-06T00:00:00.000Z",
        )
        val (r, s) = here.importJson(File(fixtures, "web-export.json").readText(), known)
        assertThat(r).isEqualTo(ImportResult.Ok(added = 1, ignored = 1))
        assertThat(s.done).containsExactly(
            "api-gateway", "2026-10-01T09:00:00.000Z", // the file's earlier date wins
            "authn-authz", "2026-10-03T18:30:00.000Z",
            "btree-lsm", "2026-10-02T00:00:00.000Z", // nothing is removed
        )
        assertThat(s.lastOpenedAt).isEqualTo("2026-10-06T00:00:00.000Z") // ours is newer
    }

    @Test
    fun `rejects what the website rejects and changes nothing`() {
        val here = ProgressState(done = mapOf("a" to "2026-10-01T00:00:00.000Z"))
        val cases = mapOf(
            "nope" to ImportResult.Failed.INVALID,
            "[]" to ImportResult.Failed.INVALID,
            "{}" to ImportResult.Failed.INVALID,
            """{"schema_version":"1","done":{}}""" to ImportResult.Failed.INVALID,
            """{"schema_version":1,"done":[]}""" to ImportResult.Failed.INVALID,
            """{"schema_version":2,"done":{}}""" to ImportResult.Failed.NEWER,
            "x".repeat(MAX_IMPORT_BYTES + 1) to ImportResult.Failed.TOO_BIG,
        )
        for ((text, want) in cases) {
            val (r, s) = here.importJson(text)
            assertThat(r).isEqualTo(want)
            assertThat(s).isSameInstanceAs(here)
        }
    }

    @Test
    fun `skips bad ids and dates, as the website does`() {
        val (r, s) = ProgressState().importJson(
            """{"schema_version":1,"done":{"good":"2026-10-01T00:00:00Z","Bad Id":"2026-10-01","worse":"yesterday","plain":"2026-10-01"}}""",
        )
        assertThat(r).isEqualTo(ImportResult.Ok(2, 0))
        assertThat(s.done.keys).containsExactly("good", "plain")
    }

    @Test
    fun `times are written like JavaScript's toISOString`() {
        assertThat(isoTime(at("2026-11-01T07:15:00Z"))).isEqualTo("2026-11-01T07:15:00.000Z")
        assertThat(isoTime(at("2026-11-01T07:15:00.123456Z"))).isEqualTo("2026-11-01T07:15:00.123Z")
    }

    @Test
    fun `mark done keeps the first-ever list and undo keeps it too`() {
        val s = ProgressState().markDone("a", at("2026-11-01T00:00:00Z")).undoDone("a").markDone("a", at("2026-11-02T00:00:00Z"))
        assertThat(s.ever).containsExactly("a")
        assertThat(s.done).containsExactly("a", "2026-11-02T00:00:00.000Z")
    }
}
